package deus.brainless.limbic;


import java.util.*;
import java.util.function.Consumer;

/**
 * Blueprint: reusable structure (nodes, connections, layers).
 * It does not store values. To execute it, create an {@link Psyche}.
 */
public class Temperament<C> {
	private final List<Layer<C>> layers;
	private final Map<String, Sensor> nodes;
	private int nextIndex;
	private final InputBuilder<C> inputBuilder = new InputBuilder<C>(this);

	private Temperament(Map<String, Sensor> nodes, List<Layer<C>> layers, int nextIndex) {
		this.nodes = nodes;
		this.layers = layers;
		this.nextIndex = nextIndex;
	}

	public Temperament() {
		this(new LinkedHashMap<>(), new ArrayList<>(), 0);
	}

	public void layerFirst(String name, Consumer<Layer<C>> config) {
		Layer<C> layer = new Layer<>(name, this);
		config.accept(layer);
		layers.add(0, layer);
	}

	@SuppressWarnings("unchecked")
	public <T extends C> Temperament<T> extend() {
		Temperament<C> copy = new Temperament<>();
		nodes.forEach((name, sensor) -> copy.nodes.put(name, sensor.copy()));
		copy.nextIndex = nextIndex;
		layers.forEach(layer -> copy.layers.add(layer.copyFor(copy, copy.nodes)));
		return (Temperament<T>) copy;
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

	Sensor createNode(String name, boolean isInput) {
		if (nodes.containsKey(name)) throw new IllegalArgumentException("Node already exists: " + name);
		Sensor sensor = isInput ? Sensor.input(nextIndex) : Sensor.index(nextIndex);
		nextIndex++;
		nodes.put(name, sensor);
		return sensor;
	}

	// (package-private)

	Urges newState() {
		Urges s = new Urges(size());
		nodes.values().forEach(n -> s.set(n.index(), n.initialValue()));
		return s;
	}

	void probe(Urges urges, C ctx) {
		for (Layer<C> layer : layers) layer.compute(urges, ctx);
	}


	public int size() {
		return nextIndex;
	}

	public Sensor getNode(String name) {
		Sensor sensor = nodes.get(name);
		if (sensor == null) throw new IllegalArgumentException("Node not found: " + name);
		return sensor;
	}

	/**
	 * Read-only view for inspection/debug.
	 */
	public Map<String, Sensor> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
}
