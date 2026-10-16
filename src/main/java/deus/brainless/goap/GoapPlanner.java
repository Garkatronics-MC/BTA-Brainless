package deus.brainless.goap;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class GoapPlanner {

	private final List<GoapAction> actions;

	public GoapPlanner(List<GoapAction> actions) {
		this.actions = actions;
	}

	private static boolean compare(StateNode a, StateNode b) {
		return a.f < b.f;
	}

	public List<GoapAction> planFrom(StateNode node) {
		List<GoapAction> plan = new ArrayList<>();
		StateNode current = node;
		while (current.parent != null) {
			plan.add(current.action);
			current = current.parent;
		}
		Collections.reverse(plan);
		return plan;
	}

	private List<GoapAction> compute(Predicate<GoapState> isGoal, ToDoubleFunction<GoapState> heuristic, GoapState start) {
		PriorityQueue<StateNode> open = new PriorityQueue<>();
		HashMap<String, Double> best_g = new HashMap<>();

		StateNode root = new StateNode(start.toKey(), start);
		root.f = heuristic.applyAsDouble(start);
		best_g.put(root.id, 0.0);
		open.add(root);

		while (!open.isEmpty()) {
			StateNode current = open.poll();
			if (current.g < best_g.get(current.id)) continue;

			if (isGoal.test(current.state)) return planFrom(current);

			for (GoapAction action : this.actions) {
				if (!action.canApply(current.state)) continue;

				GoapState state = action.apply(current.state.copy());
				StateNode next = new StateNode(state.toKey(), state);
				next.g = current.g + action.cost(current.state);

				if (best_g.containsKey(next.id) && best_g.get(next.id) <= next.g) continue;

				next.parent = current;
				next.action = action;
				next.f = next.g + heuristic.applyAsDouble(state);
				best_g.put(next.id, next.g);
				open.add(next);
			}

		}

		throw new Error("GoapGraph: no plan found.");

	}

}
