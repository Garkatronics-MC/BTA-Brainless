package deus.brainless.fsm;

@FunctionalInterface
public interface FSMCondition<C> {
	boolean test(C context);
}
