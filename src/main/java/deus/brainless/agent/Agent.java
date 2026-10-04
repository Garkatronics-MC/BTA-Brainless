package deus.brainless.agent;

/**
 * Runtime: un AgentTemplate (compartido) + su propio State.
 */
public class Agent<C> {
	protected final AgentTemplate<C> template;
	protected final State state;
	protected final C context;

	public Agent(AgentTemplate<C> template, C context) {
		this.template = template;
		this.state = template.newState();
		this.context = context;
	}

	public void compute() {
		template.compute(state, context);
	}


	public void reset() {
		template.getNodes().values().forEach(n -> state.set(n.index(), n.isInput() ? n.initialValue() : 0));
	}

	public State copyState() {
		return this.state.copy();
	}

	public Agent cloneAgent() {
		return new Agent(template, state.copy());
	}

	public double get(String name) {
		return state.get(template.getNode(name).index());
	}

	public void set(String name, double value) {
		state.set(template.getNode(name).index(), value);
	}

	public AgentTemplate template() {
		return template;
	}

	public State state() {
		return state;
	}
}
