package deus.brainless.pathfinding;

import de.bsommerfeld.pathetic.api.pathing.INeighborStrategy;
import de.bsommerfeld.pathetic.api.pathing.NeighborStrategies;
import de.bsommerfeld.pathetic.api.pathing.Pathfinder;
import de.bsommerfeld.pathetic.api.pathing.configuration.PathfinderConfiguration;
import de.bsommerfeld.pathetic.api.pathing.processing.ValidationProcessor;
import de.bsommerfeld.pathetic.api.pathing.result.Path;
import de.bsommerfeld.pathetic.api.provider.NavigationPointProvider;
import de.bsommerfeld.pathetic.api.wrapper.PathPosition;
import de.bsommerfeld.pathetic.engine.factory.AStarPathfinderFactory;
import deus.brainless.Brainless;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Walks a {@link Mob} along a Pathetic path. The navigator only decides — the
 * mob's body applies the {@link Intent}. Path computation is asynchronous.
 */
public class MobNavigator {

	/** What the body should do this tick. {@code forward} is 0 (still) to 1 (full speed). */
	public record Intent(float yRot, float forward, boolean jumping) {}

	private final Mob mob;

	private final AtomicBoolean computing = new AtomicBoolean(false);
	protected int maxIterations = 8_000;
	protected int maxLength = 64;
	protected INeighborStrategy strategy = NeighborStrategies.DIAGONAL_3D;
	protected double arrivalThreshold = 1.0D;
	protected int recomputeInterval = 60;
	protected PathfinderConfiguration pathFinderConfig = null;
	protected NavigationPointProvider provider = (pos, ctx) -> () -> true;
	protected Pathfinder pathfinder = null;

	@Nullable
	private TilePos targetTilePos = null;
	private TilePos lastTargetTilePos = null;
	@Nullable
	private Path currentPath = null;
	@Nullable
	private List<PathPosition> nodes = null;
	private boolean pathValid = false;
	private int pathRetryTimer = 0;
	private int pathIndex = 0;

	private boolean jumpLatched = false;

	/** How much higher the next node has to be to count as a step. */
	private static final double JUMP_THRESHOLD = 0.5D;

	private static final double RETARGET_SLACK = 1.0D;

	private static final double MAX_CLIMB = 1.0D;

	public MobNavigator(Mob mob) {
		this.mob = mob;
	}

	// ------------------------------------------------------------------ target

	/** Sets the target; a goal that drifted less than {@link #RETARGET_SLACK} keeps its existing path. Null drops immediately. */
	public synchronized void setTarget(@Nullable TilePos target) {
		if (target == null) {
			if (this.targetTilePos == null) return;
			this.lastTargetTilePos = this.targetTilePos;
			this.targetTilePos = null;
			invalidatePath("target dropped");
			return;
		}

		boolean sameGoal = this.targetTilePos != null && this.targetTilePos.equals(target);
		boolean merelyDrifted = this.targetTilePos != null
			&& this.targetTilePos.distanceSquared(target) <= RETARGET_SLACK * RETARGET_SLACK;

		if (sameGoal || merelyDrifted) return;

		this.lastTargetTilePos = this.targetTilePos;
		this.targetTilePos = target;
		invalidatePath("target changed");
	}

	public synchronized Optional<TilePos> getTargetTilePos() {
		if (targetTilePos != null) {
			return Optional.of(new TilePos(targetTilePos));
		}
		return Optional.empty();
	}

	public synchronized void clearTarget() {
		setTarget(null);
	}

	public synchronized boolean hasPath() {
		return pathValid && currentPath != null;
	}

	public synchronized boolean isMoving() {
		return hasPath() && pathIndex < nodeCount();
	}

	// ------------------------------------------------------------------- think

	public synchronized Intent think() {
		pathThinking();
		return pathMotion();
	}

	protected void pathThinking() {
		if (!mob.onGround) return;
		if (targetTilePos == null) return;
		if (computing.get()) return;

		double dx = targetTilePos.x - mob.x;
		double dy = targetTilePos.y - mob.y;
		double dz = targetTilePos.z - mob.z;
		if (dx * dx + dy * dy + dz * dz <= arrivalThreshold * arrivalThreshold) return;

		pathRetryTimer++;

		boolean targetChanged =
			currentPath == null ||
				nodes == null ||
				!targetTilePos.equals(lastTargetTilePos);

		if (pathValid && pathRetryTimer < recomputeInterval && !targetChanged) return;

		// Floored, not truncated: (int) rounds towards zero, so a mob standing at
		// x = -3.5 starts its search from block -3 instead of -4 and the whole path
		// is computed one block off in every negative direction.
		PathPosition start = new PathPosition(Math.floor(mob.x), Math.floor(mob.y), Math.floor(mob.z));
		PathPosition target = new PathPosition(targetTilePos.x, targetTilePos.y, targetTilePos.z);

		computing.set(true);
		pathfinder().findPath(start, target)
			.ifPresent(result -> {
				computing.set(false);
				applyPath(result.getPath());
				Brainless.LOGGER.debug("[AI] PATH FOUND length={}", result.getPath().length());
			})
			.orElse(result -> {
				computing.set(false);
				Brainless.LOGGER.debug("[AI] PATH FAILED start={} target={} status={}", start, target, result);
				pathRetryTimer = recomputeInterval / 2;
			})
			.exceptionally(ex -> {
				computing.set(false);
				Brainless.LOGGER.error("[AI] PATH EXCEPTION", ex);
				pathRetryTimer = recomputeInterval / 2;
			});
	}

	private Intent pathMotion() {
		if (!pathValid || nodes == null || targetTilePos == null) {
			return straightLine();
		}

		if (pathIndex >= nodeCount()) {
			invalidatePath("path complete");
			return straightLine();
		}

		// Look one node ahead — but never straight up. A pure vertical step has no
		// horizontal component to steer towards, so aiming at it would turn the
		// mob towards wherever it happens to be facing and stall. If the climb is
		// vertical, keep aiming at the node that has direction, and jump on its
		// account: that is the node whose height the mob actually has to reach.
		int lookahead = Math.min(pathIndex + 1, nodeCount() - 1);
		PathPosition next = nodes.get(lookahead);
		while (isPurelyVertical(next) && lookahead < nodeCount() - 1) {
			lookahead++;
			next = nodes.get(lookahead);
		}

		double tx = next.getX() + 0.5;
		double tz = next.getZ() + 0.5;
		double dx = tx - mob.x;
		double dy = climbHeightOf(nodes, lookahead);
		double dz = tz - mob.z;
		double dist = Math.sqrt(dx * dx + dz * dz);

		float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float diff = wrapDegrees(targetYaw - mob.yRot);
		diff = Math.max(-30.0F, Math.min(30.0F, diff));

		float turnPenalty = Math.max(0.15F, 1.0F - (Math.abs(diff) / 30.0F));

		// A step up is only knowable while standing on something. Once airborne the
		// node is below the feet, so the terrain no longer says "jump" — and
		// dropping the request here is what made the mob stutter at every ledge:
		// it would ask for the jump, rise one tick, forget it, and land short.
		if (mob.onGround) jumpLatched = dy > JUMP_THRESHOLD;
		boolean jumping = jumpLatched;

		if (dist < arrivalThreshold) pathIndex++;

		return new Intent(mob.yRot + diff, turnPenalty, jumping);
	}

	private boolean isPurelyVertical(PathPosition node) {
		return Math.abs(node.getX() + 0.5 - mob.x) < 0.01D
			&& Math.abs(node.getZ() + 0.5 - mob.z) < 0.01D;
	}

	private double climbHeightOf(List<PathPosition> path, int from) {
		double highest = 0;
		for (int i = from; i < path.size(); i++) {
			highest = Math.max(highest, path.get(i).getY() - mob.y);
			if (highest >= MAX_CLIMB) break;
		}
		return highest;
	}

	private Intent straightLine() {
		if (targetTilePos == null) return new Intent(mob.yRot, 0f, false);

		double dx = targetTilePos.x + 0.5 - mob.x;
		double dz = targetTilePos.z + 0.5 - mob.z;
		if (dx == 0 && dz == 0) return new Intent(mob.yRot, 0f, false);

		float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;

		// Aim and hop when the destination is above us, even with no path: a wall
		// with nothing to walk around is precisely where a mob that only walks
		// stands still forever. Latched like the path case, so the hop survives
		// the tick the mob spends leaving the ground.
		if (mob.onGround) jumpLatched = targetTilePos.y - mob.y > JUMP_THRESHOLD;

		return new Intent(targetYaw, 1f, jumpLatched);
	}

	// ------------------------------------------------------------------ plumbing

	protected synchronized Pathfinder pathfinder() {
		if (pathfinder == null) {
			pathFinderConfig = configPathfinding();
			pathfinder = new AStarPathfinderFactory().createPathfinder(pathFinderConfig);
		}
		return pathfinder;
	}

	protected final PathfinderConfiguration configPathfinding() {
		return PathfinderConfiguration.builder()
			.async(true)
			.fallback(true)
			.maxIterations(maxIterations)
			.maxLength(maxLength)
			.neighborStrategy(strategy)
			.validationProcessors(buildValidators())
			.provider(provider)
			.build();
	}

	protected List<ValidationProcessor> buildValidators() {
		return List.of(new DefaultWalkValidator(mob.world));
	}

	private synchronized void applyPath(Path path) {
		this.currentPath = path;
		this.nodes = buildNodeList(path);
		this.pathValid = true;
		this.pathRetryTimer = 0;
		this.pathIndex = 0;
		this.lastTargetTilePos = this.targetTilePos;
	}

	private synchronized void invalidatePath(String reason) {
		Brainless.LOGGER.debug("[AI] PATH INVALIDATED reason={}", reason);
		pathValid = false;
		pathRetryTimer = recomputeInterval;
		currentPath = null;
		nodes = null;
		pathIndex = 0;
	}

	private List<PathPosition> buildNodeList(Path path) {
		List<PathPosition> list = new ArrayList<>();
		for (PathPosition p : path) list.add(p);
		return list;
	}

	private int nodeCount() {
		return nodes == null ? 0 : nodes.size();
	}

	private static float wrapDegrees(float deg) {
		while (deg < -180.0F) deg += 360.0F;
		while (deg >= 180.0F) deg -= 360.0F;
		return deg;
	}
}
