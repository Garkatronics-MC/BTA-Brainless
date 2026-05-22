package deus.brainless.ai.interfaces;

import deus.brainless.ai.connection.Node;

public interface Job<CTX> {
	String name();

	void tick(CTX ctx);
	boolean isDone(CTX ctx);

	default double progress(CTX ctx) { return -1; }
	default void cancel(CTX ctx) {}
}
