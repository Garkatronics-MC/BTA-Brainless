package deus.brainless.goap;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

// Unchanged
public class GoapAction {

	private final String id;
	private double minCost = 1.0;
	private final ToDoubleFunction<GoapState> cost;
	public final Predicate<GoapState> precondition;
	private final Function<GoapState, GoapState> apply;

	public GoapAction(String id, Predicate<GoapState> precondition, Function<GoapState, GoapState> apply, ToDoubleFunction<GoapState> cost) {
		this.id = id;
		this.precondition = precondition;
		this.apply = apply;
		this.cost = cost;
	}

	public boolean canApply(GoapState state) {
		return precondition.test(state);
	}

	public GoapState apply(GoapState state) {
		return apply.apply(state.copy());
	}

	public double cost(GoapState state) {
		return (double) cost.applyAsDouble(state);
	}

	public double minCost() {
		return minCost;
	}

	public String id() {
		return id;
	}
}
