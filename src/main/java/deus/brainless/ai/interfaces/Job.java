package deus.brainless.ai.interfaces;

import java.util.Optional;

public interface Job<CTX> {
	String name();

	void tick(CTX ctx);
	boolean isDone(CTX ctx);
	default double progress(CTX ctx) { return -1; }
	default void onFinish(CTX ctx) {}
	default Optional<Job<CTX>> getNext() {
		return Optional.empty();
	}
}
