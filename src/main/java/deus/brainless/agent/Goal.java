package deus.brainless.agent;

import deus.brainless.goap.GoapState;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * A planner goal: when it's satisfied, the cost heuristic, and the start state.
 */
public record Goal(Predicate<GoapState> satisfied,
                   ToDoubleFunction<GoapState> heuristic,
                   GoapState start) {

	public Goal(Predicate<GoapState> satisfied, GoapState start) {
		this(satisfied, state -> 0.0, start);
	}

	public static Goal of(Predicate<GoapState> satisfied, GoapState start) {
		return new Goal(satisfied, start);
	}

	public static Goal of(Predicate<GoapState> satisfied,
	                      ToDoubleFunction<GoapState> heuristic,
	                      GoapState start) {
		return new Goal(satisfied, heuristic, start);
	}
}