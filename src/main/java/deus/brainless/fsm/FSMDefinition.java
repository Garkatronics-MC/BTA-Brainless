package deus.brainless.fsm;

import java.util.*;
import java.util.stream.Stream;

public final class FSMDefinition<C> {
	private final Map<FSMState, FSMTransition<C>> transitions;
	private final Map<FSMState, List<FSMRule<C>>> scoped;
	private final List<FSMRule<C>> global;

	private FSMDefinition(Builder<C> b) {
		this.transitions = Map.copyOf(b.transitions);

		Map<FSMState, List<FSMRule<C>>> scopedCopy = new HashMap<>();
		b.scoped.forEach((state, rules) -> scopedCopy.put(state, List.copyOf(rules)));
		this.scoped = Map.copyOf(scopedCopy);

		this.global = List.copyOf(b.global);
	}

	FSMState resolve(C context, FSMState target) {
		FSMTransition<C> t = transitions.get(target);
		return t == null ? null : t.apply(context);
	}

	public static <C> Builder<C> builder() { return new Builder<>(); }


	FSMState evaluate(C context, FSMState current) {
		for (FSMRule<C> r : scoped.getOrDefault(current, List.of()))
			if (r.condition().test(context)) return r.target();
		for (FSMRule<C> r : global)
			if (r.condition().test(context)) return r.target();
		return null;
	}


	public static final class Builder<C> {
		private final Map<FSMState, FSMTransition<C>> transitions = new HashMap<>();
		private final Map<FSMState, List<FSMRule<C>>> scoped = new HashMap<>();  // antes: Map<FSMState, FSMCondition<C>>
		private final List<FSMRule<C>> global = new ArrayList<>();               // antes: Set<FSMCondition<C>>

		public Builder<C> transition(FSMState to, FSMTransition<C> t) {
			transitions.put(to, t);
			return this;
		}

		public Builder<C> whileIn(FSMState s, FSMCondition<C> c, FSMState target) {
			scoped.computeIfAbsent(s, k -> new ArrayList<>()).add(new FSMRule<>(c, target));
			return this;
		}

		public Builder<C> always(FSMCondition<C> c, FSMState target) {
			global.add(new FSMRule<>(c, target));
			return this;
		}

		public FSMDefinition<C> build() {
			Stream.concat(global.stream(), scoped.values().stream().flatMap(List::stream))
				.map(FSMRule::target)
				.filter(t -> !transitions.containsKey(t))
				.findFirst()
				.ifPresent(t -> { throw new IllegalStateException("Regla apunta a un estado sin transición: " + t); });
			return new FSMDefinition<>(this);
		}
	}
}
