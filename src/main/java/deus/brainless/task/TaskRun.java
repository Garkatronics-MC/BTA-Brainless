package deus.brainless.task;

import java.util.function.Consumer;

// Exceptions don't spread, get converted to cancelled inner state
public final class TaskRun<O> {

	public static volatile Consumer<Throwable> errorHandler = Throwable::printStackTrace;

	private final Task task;
	private final O origin;
	private long ticks = 0;
	private StopReason endedBy = null;   // Null while running

	public TaskRun(Task task, O origin) {
		this.task = task;
		this.origin = origin;
	}

	public void start() {
		try {
			task.start();
		} catch (RuntimeException e) {
			errorHandler.accept(e);
			finish(StopReason.FAILED);
		}
	}

	public TaskStatus tick() {
		if (!isRunning()) return resultOf(endedBy);

		ticks++;
		TaskStatus status;
		try {
			status = task.tick();
		} catch (RuntimeException e) {
			errorHandler.accept(e);
			finish(StopReason.FAILED);
			return TaskStatus.FAILURE;
		}

		if (status == TaskStatus.SUCCESS) finish(StopReason.COMPLETED);
		else if (status == TaskStatus.FAILURE) finish(StopReason.FAILED);
		return status;
	}

	public void cancel() {
		if (isRunning()) finish(StopReason.CANCELLED);
	}

	public boolean canInterrupt() {
		return !isRunning() || task.interruptible();
	}

	private void finish(StopReason reason) {
		endedBy = reason;   // antes de stop, por si stop reentra
		try {
			task.stop(reason);
		} catch (RuntimeException e) {
			errorHandler.accept(e);
		}
	}

	private static TaskStatus resultOf(StopReason reason) {
		return reason == StopReason.COMPLETED ? TaskStatus.SUCCESS : TaskStatus.FAILURE;
	}

	public boolean isRunning() { return endedBy == null; }
	public StopReason endedBy() { return endedBy; }
	public O origin() { return origin; }

	public long elapsed() { return ticks; }
}
