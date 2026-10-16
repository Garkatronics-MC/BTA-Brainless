package deus.brainless.goap;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class GoapPlanner {

	private final Set<GoapAction> actions = new LinkedHashSet<>();

	public GoapPlanner() {}

	public GoapPlanner(Collection<GoapAction> actions) {
		this.actions.addAll(actions);
	}

	public GoapPlanner addAction(GoapAction action) {
		actions.add(Objects.requireNonNull(action));
		return this;
	}

	public GoapPlanner removeAction(GoapAction action) {
		actions.remove(action);
		return this;
	}

	public boolean hasAction(GoapAction action) {
		return actions.contains(action);
	}

	public List<GoapAction> actions() {
		return List.copyOf(actions);
	}

	public List<GoapAction> planFrom(StateNode node) {
		List<GoapAction> plan = new ArrayList<>();

		for (StateNode current = node; current.parent != null; current = current.parent) {
			plan.add(current.action);
		}

		Collections.reverse(plan);
		return plan;
	}

	public Optional<List<GoapAction>> compute(
		Predicate<GoapState> isGoal,
		ToDoubleFunction<GoapState> heuristic,
		GoapState start
	) {

		PriorityQueue<StateNode> open = new PriorityQueue<>();
		Map<String, Double> bestG = new HashMap<>();

		StateNode root = new StateNode(start.toKey(), start);
		root.f = heuristic.applyAsDouble(start);

		bestG.put(root.id, 0.0);
		open.add(root);

		while (!open.isEmpty()) {
			StateNode current = open.poll();

			if (current.g > bestG.getOrDefault(
				current.id, Double.POSITIVE_INFINITY)) {
				continue;
			}

			if (isGoal.test(current.state)) {
				return Optional.of(planFrom(current));
			}

			for (GoapAction action : actions) {
				if (!action.canApply(current.state)) {
					continue;
				}

				GoapState state = action.apply(current.state.copy());
				StateNode next = new StateNode(state.toKey(), state);
				next.g = current.g + action.cost(current.state);

				if (bestG.getOrDefault(
					next.id, Double.POSITIVE_INFINITY) <= next.g) {
					continue;
				}

				next.parent = current;
				next.action = action;
				next.f = next.g + heuristic.applyAsDouble(state);

				bestG.put(next.id, next.g);
				open.add(next);
			}
		}

		return Optional.empty();
	}
}
