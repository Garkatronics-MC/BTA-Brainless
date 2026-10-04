package deus.brainless.task.tasks;

import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskRun;
import deus.brainless.task.TaskStatus;

import java.util.List;
import java.util.function.Supplier;

public class Sequence implements Task {
	private final List<Supplier<Task>> steps;
	private int index;
	private TaskRun<Void> current;

	public Sequence(List<Supplier<Task>> steps) {
		this.steps = List.copyOf(steps);
	}

	@SafeVarargs
	public static Sequence of(Supplier<Task>... steps) {
		return new Sequence(List.of(steps));
	}

	@Override
	public void start() {
		index = 0;
		current = null;
		if (!steps.isEmpty()) startStep();
	}

	@Override
	public TaskStatus tick() {
		if (current == null) return TaskStatus.SUCCESS;   // sin pasos

		TaskStatus s = current.tick();
		if (s == TaskStatus.FAILURE) return TaskStatus.FAILURE;
		if (s == TaskStatus.SUCCESS) {
			if (++index >= steps.size()) return TaskStatus.SUCCESS;
			startStep();
		}
		return TaskStatus.RUNNING;
	}

	@Override
	public void stop(StopReason reason) {
		if (current != null) current.cancel();
	}

	@Override
	public boolean interruptible() {
		return current == null || current.canInterrupt();
	}

	private void startStep() {
		current = new TaskRun<>(steps.get(index).get(), null);
		current.start();
	}
}
