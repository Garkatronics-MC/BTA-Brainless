package deus.brainless.task.behaviour;

import deus.brainless.Brainless;
import deus.brainless.agent.AgentMob;
import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import deus.brainless.utils.Timer;
import net.minecraft.core.entity.Entity;

/**
 * Rests for a fixed duration. Aborts early if the target closes in.
 */
public class Recover implements Task {

	private final AgentMob mob;
	private final Timer duration;

	public Recover(AgentMob mob, long ticks) {
		this.mob = mob;
		this.duration = Timer.fromTicks(ticks);
	}

	@Override
	public void start() {
		duration.reset();
	}

	@Override
	public TaskStatus tick() {
		Entity target = mob.getTarget();
		if (target != null && target.isAlive() && mob.distanceTo(target) < dangerDistance()) {
			Brainless.LOGGER.debug("[brainless] recover cancelled, target close");
			return TaskStatus.FAILURE;
		}
		duration.tick();
		return duration.hasElapsed() ? TaskStatus.SUCCESS : TaskStatus.RUNNING;
	}

	/** How close is "not safe to rest". Override per mob. */
	protected float dangerDistance() {
		return 6.0f;
	}

	@Override
	public void stop(StopReason reason) {}
}
