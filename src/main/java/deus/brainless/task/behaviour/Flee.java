package deus.brainless.task.behaviour;

import deus.brainless.Brainless;
import deus.brainless.agent.AgentMob;
import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import deus.brainless.utils.Timer;
import net.minecraft.core.entity.Entity;

/**
 * Runs away from the current target. Destination is chosen once, not re-aimed.
 */
public class Flee implements Task {

	private static final long MAX_DURATION = 200;
	private static final float SAFE_DISTANCE = 10.0f;
	private static final double FLEE_DISTANCE = 12.0D;
	private static final float CLOSER_THAN = SAFE_DISTANCE * 0.6f;

	private final AgentMob mob;
	private final Timer duration = Timer.fromTicks(MAX_DURATION);

	private double destinationX;
	private double destinationZ;

	public Flee(AgentMob mob) {
		this.mob = mob;
	}

	@Override
	public void start() {
		duration.reset();
		Entity target = mob.getTarget();
		aimAwayFrom(target != null && target.isAlive() ? target : null);
		Brainless.LOGGER.debug("[brainless] flee start -> ({}, {})", destinationX, destinationZ);
	}

	@Override
	public TaskStatus tick() {
		duration.tick();

		Entity target = mob.getTarget();
		if (target == null || !target.isAlive()) {
			mob.stopMoving();
			return TaskStatus.SUCCESS;
		}

		if (mob.distanceTo(target) >= SAFE_DISTANCE) {
			mob.stopMoving();
			Brainless.LOGGER.debug("[brainless] flee done, target far");
			return TaskStatus.SUCCESS;
		}

		if (mob.distanceTo(target) < CLOSER_THAN || arrived()) {
			aimAwayFrom(target);
		}

		mob.navigateTo(destinationX, destinationZ);

		if (duration.hasElapsed()) {
			mob.stopMoving();
			Brainless.LOGGER.debug("[brainless] flee gave up after {}", duration);
			return TaskStatus.FAILURE;
		}
		return TaskStatus.RUNNING;
	}

	private boolean arrived() {
		double dx = mob.x - destinationX;
		double dz = mob.z - destinationZ;
		return dx * dx + dz * dz < 4.0D;
	}

	private void aimAwayFrom(Entity target) {
		if (target == null) {
			destinationX = mob.x;
			destinationZ = mob.z;
			return;
		}
		double dx = mob.x - target.x;
		double dz = mob.z - target.z;
		double length = Math.hypot(dx, dz);
		if (length < 1e-4) {
			// Standing on top of it: any direction will do.
			destinationX = mob.x + FLEE_DISTANCE;
			destinationZ = mob.z;
			return;
		}
		destinationX = mob.x + dx / length * FLEE_DISTANCE;
		destinationZ = mob.z + dz / length * FLEE_DISTANCE;
	}

	@Override
	public void stop(StopReason reason) {
		mob.stopMoving();
	}
}
