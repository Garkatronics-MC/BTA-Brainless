package deus.brainless.agent;


import java.util.*;
import java.util.function.Consumer;

/**
 * Blueprint: reusable structure (nodes, connections, layers).
 * It does not store values. To execute it, create an {@link Agent}.
 */
public class AgentTemplate<C> {
	private final List<Layer<C>> layers;
	private final Map<String, Node> nodes;
	private int nextIndex;
	private final InputBuilder<C> inputBuilder = new InputBuilder<C>(this);

	private AgentTemplate(Map<String, Node> nodes, List<Layer<C>> layers, int nextIndex) {
		this.nodes = nodes;
		this.layers = layers;
		this.nextIndex = nextIndex;
	}

	public AgentTemplate() {
		this(new LinkedHashMap<>(), new ArrayList<>(), 0);
	}

	public void layerFirst(String name, Consumer<Layer<C>> config) {
		Layer<C> layer = new Layer<>(name, this);
		config.accept(layer);
		layers.add(0, layer);
	}

	@SuppressWarnings("unchecked")
	public <T extends C> AgentTemplate<T> extend() {
		AgentTemplate<C> copy = new AgentTemplate<>();
		nodes.forEach((name, node) -> copy.nodes.put(name, node.copy()));
		copy.nextIndex = nextIndex;
		layers.forEach(layer -> copy.layers.add(layer.copyFor(copy, copy.nodes)));
		return (AgentTemplate<T>) copy;
	}

	public MixBuilder<C> edit(String name) {
		return new MixBuilder<>(getNode(name), this);
	}

	public void layer(String name, Consumer<Layer<C>> config) {
		Layer<C> layer = new Layer<>(name, this);
		config.accept(layer);
		layers.add(layer);
	}

	public InputBuilder<C> inputs() {
		return inputBuilder;
	}

	Node createNode(String name, boolean isInput) {
		if (nodes.containsKey(name)) throw new IllegalArgumentException("Node already exists: " + name);
		Node node = isInput ? Node.input(nextIndex) : Node.index(nextIndex);
		nextIndex++;
		nodes.put(name, node);
		return node;
	}

	// (package-private)

	State newState() {
		State s = new State(size());
		nodes.values().forEach(n -> s.set(n.index(), n.initialValue()));
		return s;
	}

	void compute(State state, C ctx) {
		for (Layer<C> layer : layers) layer.compute(state, ctx);
	}


	public int size() {
		return nextIndex;
	}

	public Node getNode(String name) {
		Node node = nodes.get(name);
		if (node == null) throw new IllegalArgumentException("Node not found: " + name);
		return node;
	}

	/**
	 * Read-only view for inspection/debug.
	 */
	public Map<String, Node> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
}
