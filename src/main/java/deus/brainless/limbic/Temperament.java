package deus.brainless.limbic;


import deus.brainless.drive.Drive;

import java.util.*;
import java.util.function.Consumer;

/** Reusable structure (nodes, connections, layers); holds no values. {@link Psyche} executes it. */
public class Temperament {
	private final List<Layer> layers;
	private final Map<String, Sensor> nodes;
	private int nextIndex;
	private final InputBuilder inputBuilder = new InputBuilder(this);
	private List<DriveBinding> bindings;

	private Temperament(Map<String, Sensor> nodes, List<Layer> layers, int nextIndex) {
		this.nodes = nodes;
		this.layers = layers;
		this.nextIndex = nextIndex;
	}

	public Temperament() {
		this(new LinkedHashMap<>(), new ArrayList<>(), 0);
	}

	public void layerFirst(String name, Consumer<Layer> config) {
		Layer layer = new Layer(name, this);
		config.accept(layer);
		layers.add(0, layer);
	}

	public Temperament extend() {
		Temperament copy = new Temperament();
		nodes.forEach((name, sensor) -> copy.nodes.put(name, sensor.copy()));
		copy.nextIndex = nextIndex;
		layers.forEach(layer -> copy.layers.add(layer.copyFor(copy, copy.nodes)));
		copy.invalidateBindings();
		return copy;
	}

	public MixBuilder edit(String name) {
		return new MixBuilder(getNode(name), this);
	}

	public MixBuilder mix(String name) {
		return new MixBuilder(createNode(name, false), this);
	}

	public void layer(String name, Consumer<Layer> config) {
		Layer layer = new Layer(name, this);
		config.accept(layer);
		layers.add(layer);
	}

	public void inputs(Consumer<InputBuilder> config) {
		config.accept(inputBuilder);
	}

	Sensor createNode(String name, boolean isInput) {
		if (nodes.containsKey(name)) throw new IllegalArgumentException("Node already exists: " + name);
		Sensor sensor = isInput ? Sensor.input(nextIndex) : Sensor.index(nextIndex);
		nextIndex++;
		nodes.put(name, sensor);
		invalidateBindings();
		return sensor;
	}

	void invalidateBindings() {
		bindings = null;
	}

	// (package-private)

	Urges newState() {
		Urges s = new Urges(size());
		nodes.values().forEach(n -> s.set(n.index(), n.initialValue()));
		return s;
	}

	void probe(Urges urges) {
		for (Layer layer : layers) layer.compute(urges);
	}

	List<DriveBinding> driveBindings() {
		if (bindings == null) {
			List<DriveBinding> list = new ArrayList<>();
			nodes.forEach((name, sensor) -> {
				for (Sensor.OutputAction out : sensor.outputs()) {
					list.add(new DriveBinding(name, sensor, out.min(), out.max(),
						out.maxInclusive(), out.drive()));
				}
			});
			bindings = List.copyOf(list);
		}
		return bindings;
	}

	public int size() {
		return nextIndex;
	}

	public Sensor getNode(String name) {
		Sensor sensor = nodes.get(name);
		if (sensor == null) throw new IllegalArgumentException("Node not found: " + name);
		return sensor;
	}

	public Map<String, Sensor> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}

	public Set<Drive> declaredDrives() {
		Set<Drive> set = new LinkedHashSet<>();
		driveBindings().forEach(b -> set.add(b.drive()));
		return Collections.unmodifiableSet(set);
	}
}