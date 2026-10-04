package deus.brainless.agent;


import java.util.LinkedHashMap;
import java.util.Map;

public class Layer<C> {

	private final String name;
	private final Map<String, Node> nodes = new LinkedHashMap<>();
	private final AgentTemplate<C> agentTemplate;

	public Layer(String name, AgentTemplate<C> agentTemplate) {
		this.name = name;
		this.agentTemplate = agentTemplate;
	}

	Layer<C> copyFor(AgentTemplate<C> newTemplate, Map<String, Node> copiedNodes) {
		Layer<C> l = new Layer<>(name, newTemplate);
		nodes.keySet().forEach(n -> l.nodes.put(n, copiedNodes.get(n)));
		return l;
	}

	public MixBuilder<C> mix(String name) {
		Node node = agentTemplate.createNode(name, false);
		nodes.put(name, node);
		return new MixBuilder<>(node, agentTemplate);
	}

	void compute(State state, C ctx) {
		for (Node node : nodes.values()) node.compute(state);
		for (Node node : nodes.values()) node.executeOutputs(state, ctx);
	}

	public String getName() { return name; }
}
