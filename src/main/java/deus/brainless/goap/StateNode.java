package deus.brainless.goap;

import deus.brainless.graph.GraphNode;


public class StateNode implements GraphNode {

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
}
