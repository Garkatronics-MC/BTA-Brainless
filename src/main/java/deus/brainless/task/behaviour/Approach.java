package deus.brainless.task.behaviour;

import deus.brainless.Brainless;
import deus.brainless.agent.AgentMob;
import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import deus.brainless.utils.Timer;
import net.minecraft.core.entity.Entity;

/**
 * Walks to the current target. Fails on timeout so the agent re-plans.
 */
public class Approach implements Task {

	private static final float REACH = 2.5f;
	private static final long TIMEOUT_TICKS = 160;

	private final AgentMob mob;
	private final Timer timeout = Timer.fromTicks(TIMEOUT_TICKS);

	public Approach(AgentMob mob) {
		this.mob = mob;
	}

	@Override
	public void start() {
		timeout.reset();
		Entity target = mob.getTarget();
		if (target == null || !target.isAlive()) {
			mob.stopMoving();
			return;
		}
		mob.navigateTo(target.x, target.y, target.z);
	}

	@Override
	public TaskStatus tick() {
		timeout.tick();

		Entity target = mob.getTarget();
		if (target == null || !target.isAlive()) {
			mob.stopMoving();
			return TaskStatus.SUCCESS;
		}
		if (mob.distanceTo(target) <= REACH) {
			mob.stopMoving();
			Brainless.LOGGER.debug("[brainless] approach reached target");
			return TaskStatus.SUCCESS;
		}

		// Re-aim as the target moves. Cheap: the navigator ignores a destination
		// that barely drifted, so this does not throw the path away every tick.
		mob.navigateTo(target.x, target.y, target.z);

		if (timeout.hasElapsed()) {
			mob.stopMoving();
			Brainless.LOGGER.debug("[brainless] approach gave up after {}", timeout);
			return TaskStatus.FAILURE;
		}
		return TaskStatus.RUNNING;
	}

	@Override
	public void stop(StopReason reason) {
		mob.stopMoving();
	}
}
