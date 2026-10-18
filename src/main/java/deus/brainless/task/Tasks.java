package deus.brainless.task;

import deus.brainless.task.flow.Fallback;
import deus.brainless.task.flow.Parallel;
import deus.brainless.task.flow.Retry;
import deus.brainless.task.flow.Sequence;
import deus.brainless.task.behaviour.Wait;

import java.util.function.Supplier;

/** Factory for every task in this library. */
public final class Tasks {

	private Tasks() {}

	@SafeVarargs
	public static Parallel allParallel(Supplier<Task>... children) {
		return Parallel.all(children);
	}

	@SafeVarargs
	public static Parallel anyParallel(Supplier<Task>... children) {
		return Parallel.any(children);
	}

	@SafeVarargs
	public static Fallback fallbackOf(Supplier<Task>... options) {
		return Fallback.of(options);
	}

	public static Retry retry(Supplier<Task> factory, int maxAttempts) {
		return new Retry(factory, maxAttempts);
	}

	@SafeVarargs
	public static Sequence sequenceOf(Supplier<Task>... steps) {
		return Sequence.of(steps);
	}

	public static Wait waitFor(long ticks) {
		return Wait.ofTicks(ticks);
	}

	public static Wait waitForSecs(double secs) {
		return Wait.ofSecs(secs);
	}

}
