package deus.brainless.fsm;

import java.util.function.BiConsumer;

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

		boolean changed = current != next && (current == null || !current.equals(next));

		if (changed) {
			BiConsumer<C, FSMState> exit = definition.onExit(current);
			if (exit != null) exit.accept(context, next);
		}

		previous = current;
		current = next;

		if (changed) {
			BiConsumer<C, FSMState> enter = definition.onEnter(current);
			if (enter != null) enter.accept(context, previous);
		}
		return true;
	}

	public void forceState(C context, FSMState state) {
		if (state == current) return;
		dispatch(context, state);
	}

	public boolean update(C context) {
		FSMState target = definition.evaluate(context, current);
		if (target == null || target == current) return false;
		return dispatch(context, target);
	}

	public FSMState current()  { return current; }
	public FSMState previous() { return previous; }
}