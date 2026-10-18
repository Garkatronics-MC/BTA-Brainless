package deus.brainless.limbic;


import java.util.LinkedHashMap;
import java.util.Map;

public class Layer {

	private final String name;
	private final Map<String, Sensor> nodes = new LinkedHashMap<>();
	private final Temperament temperament;

	public Layer(String name, Temperament temperament) {
		this.name = name;
		this.temperament = temperament;
	}

	Layer copyFor(Temperament newTemplate, Map<String, Sensor> copiedNodes) {
		Layer l = new Layer(name, newTemplate);
		nodes.keySet().forEach(n -> l.nodes.put(n, copiedNodes.get(n)));
		return l;
	}

	public MixBuilder mix(String name) {
		Sensor sensor = temperament.createNode(name, false);
		nodes.put(name, sensor);
		return new MixBuilder(sensor, temperament);
	}

	void compute(Urges urges) {
		for (Sensor sensor : nodes.values()) sensor.compute(urges);
	}

	public String getName() { return name; }

	Map<String, Sensor> nodes() { return nodes; }
}