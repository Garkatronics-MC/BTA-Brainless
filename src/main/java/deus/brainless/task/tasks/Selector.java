package deus.brainless.task.tasks;

import deus.brainless.task.StopReason;
import deus.brainless.task.Task;
import deus.brainless.task.TaskRun;
import deus.brainless.task.TaskStatus;

import java.util.List;
import java.util.function.Supplier;

public class Selector implements Task {
	private final List<Supplier<Task>> options;
	private int index;
	private TaskRun<Void> current;

	public Selector(List<Supplier<Task>> options) {
		this.options = List.copyOf(options);
	}

	@SafeVarargs
	public static Selector of(Supplier<Task>... options) {
		return new Selector(List.of(options));
	}

	@Override
	public void start() {
		index = 0;
		current = null;
		if (!options.isEmpty()) startOption();
	}

	@Override
	public TaskStatus tick() {
		if (current == null) return TaskStatus.FAILURE;

		TaskStatus s = current.tick();
		if (s == TaskStatus.SUCCESS) return TaskStatus.SUCCESS;
		if (s == TaskStatus.FAILURE) {
			if (++index >= options.size()) return TaskStatus.FAILURE;
			startOption();
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

	private void startOption() {
		current = new TaskRun<>(options.get(index).get(), null);
		current.start();
	}
}
