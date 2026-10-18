package deus.brainless.task.behaviour;

import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskStatus;
import deus.brainless.utils.Timer;

/**
 * Does nothing for a fixed number of ticks. Zero ticks succeeds on the first tick.
 */
public class Wait implements Task {

	private final Timer duration;

	public Wait(Timer duration) {
		this.duration = duration;
	}

	public static Wait ofTicks(long ticks) {
		return new Wait(Timer.fromTicks(ticks));
	}

	public static Wait ofSecs(double secs) {
		return new Wait(Timer.fromSecs(secs));
	}

	@Override
	public void start() {
		duration.reset();
	}

	@Override
	public TaskStatus tick() {
		duration.tick();
		return duration.hasElapsed() ? TaskStatus.SUCCESS : TaskStatus.RUNNING;
	}

	@Override
	public void stop(StopReason reason) {
		// nothing in flight, nothing to undo
	}
}
