package deus.brainless.fsm;

public final class FSM<C> {
	private final FSMDefinition<C> definition;
	private FSMState current;
	private FSMState previous;

	public FSM(FSMDefinition<C> definition, FSMState initial) {
		this.definition = definition;
		this.current = initial;
	}

	public boolean dispatch(C context, FSMState target) {
		FSMState next = definition.resolve(context, target);
		if (next == null) return false;
		previous = current;
		current = next;
		return true;
	}

	public boolean update(C context) {
		FSMState target = definition.evaluate(context, current);
		if (target == null || target == current) return false;
		return dispatch(context, target);
	}

	public FSMState current()  { return current; }
	public FSMState previous() { return previous; }
}
