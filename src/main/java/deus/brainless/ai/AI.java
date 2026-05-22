package deus.brainless.ai;

import deus.brainless.ai.interfaces.InputProvider;

import java.util.*;
import java.util.function.Consumer;

public class AI<CTX> {

    private final Brain brain;
    private final List<JobQueue<CTX>> queues = new ArrayList<>();

    public static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t);
    }

    public AI(Consumer<Brain> setup) {
        brain = new Brain();
        setup.accept(brain);
    }

    public static <T> AI<T> create(Consumer<Brain> setup) {
        return new AI<T>(setup);
    }

    public JobQueue<CTX> queue(JobQueue.Mode mode, double discardThreshold, int preCalculateCount) {
        JobQueue<CTX> q = new JobQueue<CTX>(mode, discardThreshold, preCalculateCount);
        queues.add(q);
        return q;
    }

    public JobQueue<CTX> queue(JobQueue.Mode mode) {
        return queue(mode, 0.25, 3);
    }

	public void update(InputProvider provider, CTX context) {
		InputSetter setter = new InputSetter(brain);
		provider.fill(setter);

		brain.compute();
		for (JobQueue<CTX> q : queues) q.update(context);
	}

    public Brain getBrain() { return brain; }

    public static class Brain {

        private final Map<String, Node> nodes = new LinkedHashMap<>();
        private final List<Layer> layers = new ArrayList<>();
        private final InputBuilder inputBuilder = new InputBuilder(this);

        public InputBuilder inputs() { return inputBuilder; }

        public void layer(String name, Consumer<Layer> config) {
            Layer layer = new Layer(name, this);
            config.accept(layer);
            layers.add(layer);
        }

        public Node getNode(String name) {
            Node node = nodes.get(name);
            if (node == null) throw new IllegalArgumentException("Node not found: " + name);
            return node;
        }

        public void register(String name, Node node) {
            nodes.put(name, node);
        }

        /** Read-only view for inspection/debug. */
        public Map<String, Node> getNodes() {
            return Collections.unmodifiableMap(nodes);
        }

        public void compute() {
            for (Layer layer : layers) layer.compute();
        }

        /** Reset all non-input nodes to 0. */
        public void reset() {
            nodes.forEach((name, node) -> {
                if (!node.isInput()) node.value = 0;
            });
        }

        public void printState() {
            System.out.println("── Brain state ──────────────────");
            nodes.forEach((name, node) ->
                    System.out.printf("  %-20s = %.4f%n", name, node.value));
            System.out.println("─────────────────────────────────");
        }
    }

    public static class InputBuilder {
        private final Brain brain;

        InputBuilder(Brain brain) { this.brain = brain; }

        public InputBuilder add(String name, double value) {
            brain.register(name, Node.input(value));
            return this;
        }

        public InputBuilder add(String name) {
            return add(name, 0.0);
        }
    }

    public static class InputSetter {
        private final Brain brain;

        InputSetter(Brain brain) { this.brain = brain; }

        public InputSetter set(String name, double value) {
            brain.getNode(name).value = clamp(value);
            return this;
        }
    }

	public static <T> java.util.function.Supplier<AI<T>>factory(
		Consumer<AI.Brain> brainSetup,
		Consumer<AI<T>> queueSetup) {
		return () -> {
			AI<T> ai = new AI<T>(brainSetup);
			queueSetup.accept(ai);
			return ai;
		};
	}
}
