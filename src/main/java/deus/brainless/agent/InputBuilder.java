package deus.brainless.agent;

public class InputBuilder<C> {
	private final AgentTemplate<C> template;

	InputBuilder(AgentTemplate<C> agentTemplate) {
		this.template = agentTemplate;
	}

	public InputBuilder<C> declare(String name, double initialValue) {
		template.createNode(name, true).setInitialValue(initialValue);
		return this;
	}

	public InputBuilder<C> declare(String name) {
		return declare(name, 0.0);
	}
}
