package deus.brainless.agent;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;


public class Node {

	final int index;
	private final boolean isInput;
	private double initialValue = 0.0;

	final List<Connection> connections = new ArrayList<>();
	private final List<OutputAction> outputs = new ArrayList<>();
	private final List<Cancellation> cancellations = new ArrayList<>();

	private DoubleConsumer onCompute = null;
	private boolean useSigmoid = false;
	private double sigmoidFactor = 1.0;

	Node(int index, boolean isInput) {
		this.index = index;
		this.isInput = isInput;
	}

	void setInitialValue(double v) { this.initialValue = v; }
	double initialValue()          { return initialValue; }

	static Node input(int index) {
		return new Node(index, true);
	}

	static Node index(int index) {
		return new Node(index, false);
	}

	Node copy() {
		Node n = new Node(index, isInput);
		n.connections.addAll(connections);
		n.outputs.addAll(outputs);
		n.cancellations.addAll(cancellations);
		n.onCompute = onCompute;
		n.useSigmoid = useSigmoid;
		n.sigmoidFactor = sigmoidFactor;
		n.initialValue = initialValue;
		return n;
	}

	// Config (Only during build)

	public Node sigmoid(double factor) {
		this.useSigmoid = true;
		this.sigmoidFactor = factor;
		return this;
	}

	public Node onCompute(DoubleConsumer hook) {
		this.onCompute = hook;
		return this;
	}

	public Connection connect(Node target, BiFunction<Double, Double, Double> op, double weight) {
		Connection conn = new Connection(this.index, target.index, op, weight);
		target.connections.add(conn);
		return conn;
	}


	public void canceledBy(double min, double max, Node inhibitor, double threshold) {
		cancellations.add(new Cancellation(min, max, inhibitor.index, threshold));
	}

	public void canceledBy(double min, double max, Node inhibitor) {
		canceledBy(min, max, inhibitor, 0.5);
	}

	void output(double min, double max, Reaction<Object> reaction) {
		outputs.add(new OutputAction(min, max, reaction));
	}


	void compute(State s) {
		if (isInput) return;

		double value = 0;
		for (Connection conn : connections) {
			value = conn.apply(value, s.get(conn.from()));
		}

		value = Math.min(1.0, Math.max(0.0, value));

		if (useSigmoid) {
			value = Agents.sigmoid((value - 0.5) * sigmoidFactor);
		}

		s.set(index, value);

		if (onCompute != null) onCompute.accept(value);
	}

	void executeOutputs(State s, Object ctx) {
		double value = s.get(index);

		for (Cancellation c : cancellations) {
			if (value >= c.min && value <= c.max && s.get(c.inhibitor) >= c.threshold) return;
		}
		for (OutputAction out : outputs) {
			if (value >= out.min && value <= out.max) out.reaction.run(ctx, value);
		}
	}


	public int index() {
		return index;
	}

	public boolean isInput() {
		return isInput;
	}

	private record OutputAction(double min, double max, Reaction<Object> reaction) {}

	private record Cancellation(double min, double max, int inhibitor, double threshold) {}
}
