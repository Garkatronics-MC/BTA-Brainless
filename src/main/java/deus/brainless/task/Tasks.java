package deus.brainless.task;

import deus.brainless.task.tasks.Parallel;
import deus.brainless.task.tasks.Retry;
import deus.brainless.task.tasks.Selector;
import deus.brainless.task.tasks.Sequence;

import java.util.List;
import java.util.function.Supplier;

public final class Tasks {

	private Tasks() {}

	@SafeVarargs
	public static Parallel allParallel(Supplier<Task>... children) {
		return Parallel.all(children);
	}

	@SafeVarargs
	public static Parallel anyParallel(Supplier<Task>... children) {
		return Parallel.all(children);
	}

	public static Retry retry(Supplier<Task> factory, int maxAttempts) {
		return new Retry(factory, maxAttempts);
	}

	@SafeVarargs
	public static Selector selectorOf(Supplier<Task>... steps) {
		return Selector.of(steps);
	}

	@SafeVarargs
	public static Sequence sequenceOf(Supplier<Task>... steps) {
		return Sequence.of(steps);
	}

}
