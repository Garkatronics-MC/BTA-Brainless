package deus.brainless.ai.connection;

import deus.brainless.ai.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Node {

    public double value;
    final List<Connection> connections = new ArrayList<>();
    private final List<OutputAction> outputs = new ArrayList<>();
    private final List<Cancellation> cancellations = new ArrayList<>();

    public Runnable onCompute = null;

    private boolean useSigmoid = false;
    private double sigmoidFactor = 1.0;
    private boolean isInput = false;

    public static Node input(double value) {
        Node n = new Node();
        n.value = value;
        n.isInput = true;
        return n;
    }

    public static Node index() {
        return new Node();
    }

    public Node sigmoid(double factor) {
        this.useSigmoid = true;
        this.sigmoidFactor = factor;
        return this;
    }

    public boolean isInput() {
        return isInput;
    }

    public Connection connect(Node target, BiFunction<Double, Double, Double> op, double weight) {
        Connection conn = new Connection(this, target, op, weight);
        target.connections.add(conn);
        return conn;
    }

    public void canceledBy(double min, double max, Node inhibitor, double threshold) {
        cancellations.add(new Cancellation(min, max, inhibitor, threshold));
    }

    public void canceledBy(double min, double max, Node inhibitor) {
        canceledBy(min, max, inhibitor, 0.5);
    }

    public void output(double min, double max, Runnable action) {
        outputs.add(new OutputAction(min, max, action));
    }

    public void compute() {
        if (isInput) return;

        value = 0;
        for (Connection conn : connections) {
            value = conn.apply(value, conn.from.value);
        }

        value = Math.min(1.0, Math.max(0.0, value));

        if (useSigmoid) {
            value = AI.sigmoid((value - 0.5) * sigmoidFactor);
        }

        if (onCompute != null) onCompute.run();
    }

    public void executeOutputs() {
        for (Cancellation c : cancellations) {
            if (value >= c.min && value <= c.max && c.inhibitor.value >= c.threshold) return;
        }
        for (OutputAction out : outputs) {
            if (value >= out.min && value <= out.max) out.action.run();
        }
    }

	public double snapshot() {
        return value;
    }


    private record OutputAction(double min, double max, Runnable action) {}

    private record Cancellation(double min, double max, Node inhibitor, double threshold) {}
}
