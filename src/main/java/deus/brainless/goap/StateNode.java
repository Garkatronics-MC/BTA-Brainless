package deus.brainless.goap;

import deus.brainless.graph.GraphNode;


/**
 * A node on the search frontier. Ordered by {@link #f} so it can live in the
 * planner's priority queue — same contract as {@code RouteNode} in the graph
 * package, which orders by its estimated score.
 */
public class StateNode implements GraphNode, Comparable<StateNode> {

	public final String id;
	public final GoapState state;
	public double g = 0.0;
	public double f = 0.0;
	public StateNode parent = null;
	public GoapAction action = null;

	public StateNode(String id, GoapState state) {
		this.id = id;
		this.state = state;
	}


	@Override
	public String getId() {
		return id == null ? "start" : id;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StateNode)) return false;
		return state.equals(((StateNode) o).state);
	}

	@Override
	public int hashCode() {
		return state.hashCode();
	}

	/** Lower {@link #f} first. */
	@Override
	public int compareTo(StateNode other) {
		return Double.compare(this.f, other.f);
	}
}
