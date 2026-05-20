package deus.brainless.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public class Layer {

    private final String name;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final AI.Brain brain;

    public Layer(String name, AI.Brain brain) {
        this.name = name;
        this.brain = brain;
    }

    public MixBuilder mix(String name) {
        Node node = Node.index();
        brain.register(name, node);
        nodes.put(name, node);
        return new MixBuilder(node, brain);
    }

    public void compute() {
        for (Node node : nodes.values()) node.compute();
        for (Node node : nodes.values()) node.executeOutputs();
    }

    public String getName() { return name; }
}
