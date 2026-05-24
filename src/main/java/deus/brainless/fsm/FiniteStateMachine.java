package deus.brainless.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FiniteStateMachine<T, C> {

	private final Map<T, Consumer<C>> actions = new HashMap<>();
	private final Map<T, Function<C, T>> transitions = new HashMap<>();
	private final Map<T, BiConsumer<C, T>> onEnter = new HashMap<>(); // (ctx, from)
	private final Map<T, BiConsumer<C, T>> onExit = new HashMap<>(); // (ctx, to)

	private T current;
	private T terminal;

	public static <TT, CC> Supplier<FiniteStateMachine<TT, CC>> factory(Consumer<FiniteStateMachine<TT, CC>> setup) {
		return () -> {
			FiniteStateMachine<TT, CC> fsm = new FiniteStateMachine<>();
			setup.accept(fsm);
			return fsm;
		};
	}

	/**
	 * Initial state of the machine.
	 */
	public FiniteStateMachine<T, C> start(T initial) {
		this.current = initial;
		return this;
	}

	/**
	 * Mark a state as the end.
	 * Avoid this if you want an infinite loop.
	 */
	public FiniteStateMachine<T, C> terminal(T state) {
		this.terminal = state;
		return this;
	}

	/**
	 * When the machine reaches this state.
	 */
	public FiniteStateMachine<T, C> on(T state, Consumer<C> action) {
		actions.put(state, action);
		return this;
	}

	/**
	 * Conditions for reaching a new state.
	 */
	public FiniteStateMachine<T, C> transition(T state, Function<C, T> next) {
		transitions.put(state, next);
		return this;
	}

	/**
	 * Do something when this state begins.
	 */
	public FiniteStateMachine<T, C> onEnter(T state, BiConsumer<C, T> handler) {
		onEnter.put(state, handler);
		return this;
	}

	/**
	 * Do something when this state ends.
	 */
	public FiniteStateMachine<T, C> onExit(T state, BiConsumer<C, T> handler) {
		onExit.put(state, handler);
		return this;
	}


	/**
	 * Update function
	 */
	public void update(C ctx) {
		if (isDone()) return;

		Consumer<C> action = actions.get(current);
		if (action != null) action.accept(ctx);

		Function<C, T> trans = transitions.get(current);
		if (trans == null) return;

		T next = trans.apply(ctx);
		if (next == null || next == current) return;

		BiConsumer<C, T> exit = onExit.get(current);
		if (exit != null) exit.accept(ctx, next);

		T previous = current;
		current = next;

		BiConsumer<C, T> enter = onEnter.get(current);
		if (enter != null) enter.accept(ctx, previous);
	}

	/**
	 * Reset machine.
	 */
	public void reset() {
		current = null;
	}

	/**
	 * Get current state.
	 */
	public T current() {
		return current;
	}

	/**
	 * Check if the FSM has ended.
	 */
	public boolean isDone() {
		return terminal != null && terminal.equals(current);
	}
}
