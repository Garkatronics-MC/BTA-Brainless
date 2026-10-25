package deus.brainless.fsm;

@FunctionalInterface
public interface FSMTransition<C> {
	FSMState apply(C context);
}
