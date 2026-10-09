package deus.brainless.limbic;


import java.util.LinkedHashMap;
import java.util.Map;

public class Layer<C> {

	private final String name;
	private final Map<String, Sensor> nodes = new LinkedHashMap<>();
	private final Temperament<C> temperament;

	public Layer(String name, Temperament<C> temperament) {
		this.name = name;
		this.temperament = temperament;
	}

	Layer<C> copyFor(Temperament<C> newTemplate, Map<String, Sensor> copiedNodes) {
		Layer<C> l = new Layer<>(name, newTemplate);
		nodes.keySet().forEach(n -> l.nodes.put(n, copiedNodes.get(n)));
		return l;
	}

	public MixBuilder<C> mix(String name) {
		Sensor sensor = temperament.createNode(name, false);
		nodes.put(name, sensor);
		return new MixBuilder<>(sensor, temperament);
	}

	void compute(Urges urges, C ctx) {
		for (Sensor sensor : nodes.values()) sensor.compute(urges);
		for (Sensor sensor : nodes.values()) sensor.executeOutputs(urges, ctx);
	}

	public String getName() { return name; }
}
