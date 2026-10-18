package deus.brainless.task.behaviour;

import deus.brainless.Brainless;
import deus.brainless.agent.AgentMob;
import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import net.minecraft.core.entity.Entity;

/**
 * Swings at the current target once it is in reach. Fails if target walks out of range.
 */
public class Attack implements Task {

	private static final float REACH = 2.5f;
	private static final float GIVE_UP_BEYOND = 4.0f;

	private final AgentMob mob;

	public Attack(AgentMob mob) {
		this.mob = mob;
	}

	@Override
	public void start() {}

	@Override
	public TaskStatus tick() {
		Entity target = mob.getTarget();
		if (target == null || !target.isAlive()) return TaskStatus.SUCCESS;

		float distance = mob.distanceTo(target);
		mob.face(target);

		if (distance <= REACH) {
			mob.attack(target, distance);
			Brainless.LOGGER.debug("[brainless] strike hit {} dist={}",
				target.getClass().getSimpleName(), distance);
			return TaskStatus.SUCCESS;
		}

		if (distance > GIVE_UP_BEYOND) {
			Brainless.LOGGER.debug("[brainless] strike lost target dist={}", distance);
			return TaskStatus.FAILURE;
		}
		return TaskStatus.RUNNING;
	}

	@Override
	public void stop(StopReason reason) {}
}
