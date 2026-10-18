package deus.brainless.task;


@FunctionalInterface
public interface TaskFactory<C> {
	Task create(C context);
}
