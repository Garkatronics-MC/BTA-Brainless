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
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MobPathfinder extends Mob {

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
	@Nullable
	private Path currentPath = null;
	@Nullable
	private List<PathPosition> nodes = null;
	private boolean pathValid = false;
	private int pathRetryTimer = 0;
	private int pathIndex = 0;

	public MobPathfinder(@NotNull World world) {
		super(world);
		pathFinderConfig = configPathfinding();
		pathfinder = new AStarPathfinderFactory().createPathfinder(pathFinderConfig);
	}

	private static float wrapDegrees(float deg) {
		while (deg < -180.0F) deg += 360.0F;
		while (deg >= 180.0F) deg -= 360.0F;
		return deg;
	}

	public void setTarget(@Nullable TilePos target) {
		if (target == null || target.equals(this.targetTilePos)) return;
		this.targetTilePos = target;
		invalidatePath("target changed");
	}


	public boolean hasPath() {
		return pathValid && currentPath != null;
	}

	public boolean isMoving() {
		return hasPath() && pathIndex < nodeCount();
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
		return List.of(new DefaultWalkValidator(world));
	}

	@Override
	protected void updateAI() {
		super.updateAI();
		pathThinking();
		pathMotion();
	}

	protected void pathThinking() {
		if (targetTilePos == null) return;
		if (computing.get()) return;

		double dx = targetTilePos.x - this.x;
		double dy = targetTilePos.y - this.y;
		double dz = targetTilePos.z - this.z;
		if (dx * dx + dy * dy + dz * dz <= arrivalThreshold * arrivalThreshold) return;

		pathRetryTimer++;
		if (pathValid && pathRetryTimer <= recomputeInterval) return;

		PathPosition start = new PathPosition((int) x, (int) y, (int) z);
		PathPosition target = new PathPosition(targetTilePos.x, targetTilePos.y, targetTilePos.z);

		computing.set(true);
		pathfinder.findPath(start, target).ifPresent(result -> {
			computing.set(false);
			if (result.successful()) {
				applyPath(result.getPath());
				Brainless.LOGGER.debug("[AI] PATH FOUND length={}", currentPath.length());
			} else {
				Brainless.LOGGER.debug("[AI] PATH FAILED start={} target={}", start, target);
				pathRetryTimer = recomputeInterval / 2;
			}
		});
	}

	private void applyPath(Path path) {
		this.currentPath = path;
		this.nodes = buildNodeList(path);
		this.pathValid = true;
		this.pathRetryTimer = 0;
		this.pathIndex = 0;
	}

	private void invalidatePath(String reason) {
		Brainless.LOGGER.debug("[AI] PATH INVALIDATED reason={}", reason);
		pathValid = false;
		pathRetryTimer = recomputeInterval;
		currentPath = null;
		nodes = null;
		pathIndex = 0;
	}

	protected void pathMotion() {
		if (!pathValid || nodes == null) return;
		if (pathIndex >= nodeCount()) {
			invalidatePath("path complete");
			return;
		}

		int lookahead = Math.min(pathIndex + 1, nodeCount() - 1);
		PathPosition next = nodes.get(lookahead);

		double tx = next.getX() + 0.5;
		double tz = next.getZ() + 0.5;
		double dx = tx - this.x;
		double dy = next.getY() - this.y;
		double dz = tz - this.z;
		double dist = Math.sqrt(dx * dx + dz * dz);

		float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float diff = wrapDegrees(targetYaw - this.yRot);
		diff = Math.max(-30.0F, Math.min(30.0F, diff));
		this.yRot += diff;

		float turnPenalty = Math.max(0.15F, 1.0F - (Math.abs(diff) / 30.0F));
		this.moveForward = this.moveSpeed * turnPenalty;

		if (dy > 0.5) this.isJumping = true;

		if (dist < arrivalThreshold) pathIndex++;
	}

	private List<PathPosition> buildNodeList(Path path) {
		List<PathPosition> list = new ArrayList<>();
		for (PathPosition p : path) list.add(p);
		return list;
	}

	private int nodeCount() {
		return nodes == null ? 0 : nodes.size();
	}
}
