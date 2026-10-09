package deus.brainless.goap.astar;

public class StateNode {
	protected int id;
	protected StateNode parent = null;
	protected TransitionEdge sourceTransition = null;
	protected GState state = null;

	protected StateNode(int id, StateNode parent, TransitionEdge sourceTransition, GState state) {
		this.id = id;
		this.parent = parent;
		this.sourceTransition = sourceTransition;
		this.state = state;
	}

}
