package deus.brainless.agent;

import deus.brainless.Brainless;
import deus.brainless.agent.Agent.Plan;
import deus.brainless.goap.GoapAction;
import deus.brainless.goap.GoapState;
import deus.brainless.pathfinding.MobNavigator;
import deus.brainless.task.TaskController;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.Optional;
import java.util.Random;

/**
 * A BTA mob driven by a Brainless {@link Agent}. Extend, supply an agent via
 * {@link #createAgent()}, and override {@link #acquireTarget()} / {@link #strike}.
 * Does not call {@code super.updateAI()} — the brain owns all behaviour.
 */
public abstract class AgentMob extends net.minecraft.core.entity.Mob {

	private Agent agent;
	private final TaskController controller = new TaskController();
	private final MobNavigator navigator = new MobNavigator(this);

	private Entity target;

	protected AgentMob(World world) {
		super(world);
	}

	/** The brain for this mob; created lazily on first use. */
	protected abstract Agent createAgent();

	protected Entity acquireTarget() {
		return null;
	}

	protected void strike(Entity target, float distance) {}

	// ------------------------------------------------------------------ the loop

	@Override
	protected void updateAI() {
		if (world == null || noAI) return;

		// Mob.updateAI() would do this too, and we skip its wandering on purpose.
		// Age still has to advance or every age-driven animation freezes.
		this.entityAge++;

		Agent brain = agent();
		sense(brain);
		onPlan(brain.update(worldState()));
		moveAsInstructed();
	}

	@Override
	public void tick() {
		// Advance the task first, so its movement intent is applied by super.tick().
		controller.tick();
		super.tick();
	}

	// ---------------------------------------------------------------- the hooks

	protected void sense(Agent agent) {
		agent.sense("health", healthFraction());
		agent.sense("targetDist", targetDistance());
	}

	protected GoapState worldState() {
		GoapState state = new GoapState();
		state.range("targetDist", 0f, 1f);
		state.set("targetDist", targetDistance());
		state.set("health", healthFraction());
		return state;
	}

	protected void onPlan(Optional<Plan> plan) {
		if (plan.isEmpty()) return;
		Brainless.LOGGER.debug("[brainless] {} decided drive={} actions={}",
			getClass().getSimpleName(),
			plan.get().drive().id(),
			plan.get().actions().stream().map(GoapAction::id).toList());
	}

	// ---------------------------------------------------------------- movement

	private void moveAsInstructed() {
		MobNavigator.Intent intent = navigator.think();
		this.yRot = intent.yRot();
		this.moveForward = this.moveSpeed * intent.forward();
		this.isJumping = intent.jumping();

		if (intent.jumping() && this.onGround) jump();
	}

	public void navigateTo(double targetX, double targetZ) {
		navigator.setTarget(new TilePos((int) targetX, (int) this.y, (int) targetZ));
	}

	public void navigateTo(double targetX, double targetY, double targetZ) {
		navigator.setTarget(new TilePos((int) targetX, (int) targetY, (int) targetZ));
	}

	public void stopMoving() {
		navigator.clearTarget();
	}

	public void face(Entity target) {
		if (target == null) return;
		double dx = target.x - this.x;
		double dz = target.z - this.z;
		this.yRot = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
	}

	public void attack(Entity target, float distance) {
		strike(target, distance);
	}

	// ------------------------------------------------------------------- state

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public Agent agent() {
		Agent a = this.agent;
		if (a == null) {
			a = createAgent();
			this.agent = a;
		}
		return a;
	}

	public TaskController controller() {
		return controller;
	}

	public Random random() {
		return this.random;
	}

	protected float healthFraction() {
		int max = getMaxHealth();
		return max <= 0 ? 1f : getHealth() / (float) max;
	}

	protected float targetDistance() {
		Entity current = getTarget();
		if (current == null || !current.isAlive()) return 1f;
		return (float) Math.min(1.0, current.distanceTo(this) / 16.0);
	}
}
