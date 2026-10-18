package deus.brainless.task;

import java.util.Optional;
import java.util.function.Consumer;

public class TaskController {

	private TaskRun<?> current = null;
	private Consumer<TaskRun<?>> onFinished = run -> {};

	public boolean setCurrent(TaskRun<?> next) {
		if (current != null && !current.canInterrupt()) return false;
		if (current != null) finish(current, true);

		current = next;
		current.start();
		return true;
	}

	public void tick() {
		if (current == null) return;
		current.tick();
		if (!current.isRunning()) finish(current, false);
	}

	public void clear() {
		if (current != null) finish(current, true);
	}

	public Optional<TaskRun<?>> current() {
		return Optional.ofNullable(current);
	}

	public void onFinished(Consumer<TaskRun<?>> listener) { this.onFinished = listener; }

	private void finish(TaskRun<?> run, boolean cancel) {
		if (cancel) run.cancel();
		if (current == run) current = null;
		onFinished.accept(run);
	}
}
