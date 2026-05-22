package deus.brainless.ai.connection;

import deus.brainless.ai.AI;

import java.util.function.BiFunction;

public class MixBuilder {

    private final Node node;
    private final AI.Brain brain;

    public MixBuilder(Node node, AI.Brain brain) {
        this.node = node;
        this.brain = brain;
    }

    public MixBuilder add(String input, double weight) {
        return connect(input, Connection.ADD, weight);
    }

    public MixBuilder sub(String input, double weight) {
        return connect(input, Connection.SUB, weight);
    }

    public MixBuilder mul(String input, double weight) {
        return connect(input, Connection.MUL, weight);
    }

    public MixBuilder div(String input, double weight) {
        return connect(input, Connection.DIV, weight);
    }

    public MixBuilder max(String input, double weight) {
        return connect(input, Connection.MAX, weight);
    }

    public MixBuilder min(String input, double weight) {
        return connect(input, Connection.MIN, weight);
    }

    public MixBuilder op(String input, BiFunction<Double, Double, Double> op, double weight) {
        return connect(input, op, weight);
    }

    private MixBuilder connect(String inputName, BiFunction<Double, Double, Double> op, double weight) {
        Node inputNode = brain.getNode(inputName);
        inputNode.connect(node, op, weight);
        return this;
    }

    public MixBuilder sigmoid(double factor) {
        node.sigmoid(factor);
        return this;
    }

    public MixBuilder canceledBy(String inhibitor, double threshold) {
        Node inh = brain.getNode(inhibitor);
        node.canceledBy(threshold, 1.0, inh);
        return this;
    }

    public MixBuilder debug(String label) {
        node.onCompute = () -> System.out.printf("  [%s] = %.4f%n", label, node.value);
        return this;
    }

    public MixBuilder onRange(double min, double max, Runnable action) {
        node.output(min, max, action);
        return this;
    }

    public MixBuilder onHigh(Runnable action)   { return onRange(0.7, 1.0, action); }
    public MixBuilder onMedium(Runnable action) { return onRange(0.3, 0.7, action); }
    public MixBuilder onLow(Runnable action)    { return onRange(0.0, 0.3, action); }

    public MixBuilder above(double t, Runnable action) { return onRange(t, 1.01, action); }
    public MixBuilder below(double t, Runnable action) { return onRange(0.0, t, action); }

    public MixBuilder onExact(double value, double epsilon, Runnable action) {
        return onRange(value - epsilon, value + epsilon, action);
    }

    public Node node() { return node; }
}
