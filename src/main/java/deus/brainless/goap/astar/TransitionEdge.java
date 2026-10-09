package deus.brainless.goap.astar;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class TransitionEdge {

	private final String id;
	private final Predicate<GState> precondition;
	private final Consumer<GState> effect;
	private final ToDoubleFunction<GState> cost;
	private final float minCost;

	public TransitionEdge(String id, Predicate<GState> precondition, Consumer<GState> effect, ToDoubleFunction<GState> cost, float minCost) {
		this.id = id;
		this.precondition = precondition;
		this.effect = effect;
		this.cost = cost;
		this.minCost = minCost;
	}

	public TransitionEdge(String id, Predicate<GState> precondition, Consumer<GState> effect, float cost) {
		this(id, precondition, effect, s -> cost, cost);
	}

	public boolean canApply(GState state) {
		return precondition.test(state);
	}

	public GState apply(GState state) {
		GState next = state.copy();
		effect.accept(next);
		return next;
	}

	public float cost(GState state) {
		return (float) cost.applyAsDouble(state);
	}

	public float minCost() {
		return minCost;
	}

	public String id() {
		return id;
	}
}
