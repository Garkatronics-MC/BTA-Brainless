package deus.brainless.task.tasks;

import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskRun;
import deus.brainless.task.TaskStatus;

import java.util.function.Supplier;

public class Retry implements Task {
	private final Supplier<Task> factory;
	private final int maxAttempts;
	private int attempt;
	private TaskRun<Void> current;

	public Retry(Supplier<Task> factory, int maxAttempts) {
		if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
		this.factory = factory;
		this.maxAttempts = maxAttempts;
	}

	@Override
	public void start() {
		attempt = 1;
		begin();
	}

	@Override
	public TaskStatus tick() {
		TaskStatus s = current.tick();
		if (s == TaskStatus.FAILURE) {
			if (attempt >= maxAttempts) return TaskStatus.FAILURE;
			attempt++;
			begin();
			return TaskStatus.RUNNING;
		}
		return s;
	}

	@Override
	public void stop(StopReason reason) {
		if (current != null) current.cancel();
	}

	@Override
	public boolean interruptible() {
		return current == null || current.canInterrupt();
	}

	private void begin() {
		current = new TaskRun<>(factory.get(), null);
		current.start();
	}
}
