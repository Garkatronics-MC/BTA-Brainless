package deus.brainless.task;

import deus.brainless.task.flow.Fallback;
import deus.brainless.task.flow.Parallel;
import deus.brainless.task.flow.Retry;
import deus.brainless.task.flow.Sequence;
import deus.brainless.task.behaviour.Wait;

import java.util.function.Supplier;

/**
 * Every task this library ships, in one place. The three groups are the three
 * questions a task can answer for itself: is it built out of other tasks
 * ({@code flow}), is it ready to use as it is ({@code behaviour}), or is it the
 * contract itself and lives in {@code task}.
 */
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

	/**
	 * Waits a fixed stretch of ticks and then succeeds. For a duration, not for a
	 * condition: nothing is being watched, the clock simply runs out.
	 */
	public static Wait waitFor(long ticks) {
		return Wait.ofTicks(ticks);
	}

	/** As {@link #waitFor(long)}, in seconds. Fractional seconds round up. */
	public static Wait waitForSecs(double secs) {
		return Wait.ofSecs(secs);
	}

}
