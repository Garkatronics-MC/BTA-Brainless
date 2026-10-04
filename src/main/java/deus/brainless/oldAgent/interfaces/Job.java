package deus.brainless.oldAgent.interfaces;

@Deprecated(since = "1.5.0", forRemoval = true)
public interface Job<CTX> {
	String name();
	void tick(CTX ctx);
	boolean isDone(CTX ctx);
	default double progress(CTX ctx) { return -1; }
	default void onFinish(CTX ctx) {}
	default String parentJobName() { return null; }
	default boolean hasParent() { return parentJobName() != null; }
	void setParent(String parent);
}
