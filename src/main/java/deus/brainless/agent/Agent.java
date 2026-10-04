package deus.brainless.agent;

/**
 * Runtime: un AgentTemplate (compartido) + su propio State.
 */
public class Agent<C> {
	protected final AgentTemplate<C> template;
	protected final State state;
	protected final C context;

	public Agent(AgentTemplate<C> template, State state, C context) {
		this.template = template;
		this.state = state;
		this.context = context;
	}

	public Agent(AgentTemplate<C> template, C context) {
		this(template, template.newState(), context);
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

	public Agent<C> cloneAgent() {
		return new Agent<>(template, this.context);
	}

	public double get(String name) {
		return state.get(template.getNode(name).index());
	}

	public Agent<C> set(String name, double value) {
		state.set(template.getNode(name).index(), value);
		return this;
	}

	public AgentTemplate<C> template() {
		return template;
	}

	public State state() {
		return state;
	}
}
