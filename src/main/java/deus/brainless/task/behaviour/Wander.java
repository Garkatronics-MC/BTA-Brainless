package deus.brainless.task.behaviour;

import deus.brainless.Brainless;
import deus.brainless.agent.AgentMob;
import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import deus.brainless.utils.Timer;

/**
 * Walks to a random nearby point for a while, then stops.
 */
public class Wander implements Task {

	private static final long DURATION_TICKS = 60;
	private static final double RADIUS = 8.0D;

	private final AgentMob mob;
	private final Timer duration = Timer.fromTicks(DURATION_TICKS);

	private double targetX;
	private double targetZ;

	public Wander(AgentMob mob) {
		this.mob = mob;
	}

	@Override
	public void start() {
		duration.reset();
		double angle = mob.random().nextDouble() * Math.PI * 2.0;
		targetX = mob.x + Math.cos(angle) * radius();
		targetZ = mob.z + Math.sin(angle) * radius();
		Brainless.LOGGER.debug("[brainless] wander -> ({}, {})", targetX, targetZ);
	}

	@Override
	public TaskStatus tick() {
		duration.tick();
		if (duration.hasElapsed()) {
			mob.stopMoving();
			return TaskStatus.SUCCESS;
		}
		mob.navigateTo(targetX, targetZ);
		return TaskStatus.RUNNING;
	}

	protected double radius() {
		return RADIUS;
	}

	@Override
	public void stop(StopReason reason) {
		mob.stopMoving();
	}
}
