package deus.brainless.ai.interfaces;

public interface Job<CTX> {
	String name();

	void tick(CTX ctx);
	boolean isDone(CTX ctx);
	default boolean interrupt() { return false; };
	default double progress(CTX ctx) { return -1; }
	default void onFinish(CTX ctx) {}
}
