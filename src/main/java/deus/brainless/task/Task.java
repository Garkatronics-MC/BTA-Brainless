package deus.brainless.task;


public interface Task {

	void start();

	TaskStatus tick();

	void stop(StopReason reason);

	default boolean interruptible() {
		return true;
	}
}
