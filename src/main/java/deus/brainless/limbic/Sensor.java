package deus.brainless.limbic;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;


public class Sensor {

	final int index;
	private final boolean isInput;
	private double initialValue = 0.0;

	final List<Influence> influences = new ArrayList<>();
	private final List<OutputAction> outputs = new ArrayList<>();
	private final List<Cancellation> cancellations = new ArrayList<>();

	private DoubleConsumer onCompute = null;
	private boolean useSigmoid = false;
	private double sigmoidFactor = 1.0;

	Sensor(int index, boolean isInput) {
		this.index = index;
		this.isInput = isInput;
	}

	void setInitialValue(double v) { this.initialValue = v; }
	double initialValue()          { return initialValue; }

	static Sensor input(int index) {
		return new Sensor(index, true);
	}

	static Sensor index(int index) {
		return new Sensor(index, false);
	}

	Sensor copy() {
		Sensor n = new Sensor(index, isInput);
		n.influences.addAll(influences);
		n.outputs.addAll(outputs);
		n.cancellations.addAll(cancellations);
		n.onCompute = onCompute;
		n.useSigmoid = useSigmoid;
		n.sigmoidFactor = sigmoidFactor;
		n.initialValue = initialValue;
		return n;
	}

	// Config (Only during build)

	public Sensor sigmoid(double factor) {
		this.useSigmoid = true;
		this.sigmoidFactor = factor;
		return this;
	}

	public Sensor onCompute(DoubleConsumer hook) {
		this.onCompute = hook;
		return this;
	}

	public Influence connect(Sensor target, BiFunction<Double, Double, Double> op, double weight) {
		Influence conn = new Influence(this.index, target.index, op, weight);
		target.influences.add(conn);
		return conn;
	}


	public void canceledBy(double min, double max, Sensor inhibitor, double threshold) {
		cancellations.add(new Cancellation(min, max, inhibitor.index, threshold));
	}

	public void canceledBy(double min, double max, Sensor inhibitor) {
		canceledBy(min, max, inhibitor, 0.5);
	}

	void triggerOn(double min, double max, Effector<Object> effector) {
		outputs.add(new OutputAction(min, max, effector));
	}


	void compute(Urges s) {
		if (isInput) return;

		double value = 0;
		for (Influence conn : influences) {
			value = conn.apply(value, s.get(conn.from()));
		}

		value = Math.min(1.0, Math.max(0.0, value));

		if (useSigmoid) {
			value = Psyches.sigmoid((value - 0.5) * sigmoidFactor);
		}

		s.set(index, value);

		if (onCompute != null) onCompute.accept(value);
	}

	void executeOutputs(Urges s, Object ctx) {
		double value = s.get(index);

		for (Cancellation c : cancellations) {
			if (value >= c.min && value <= c.max && s.get(c.inhibitor) >= c.threshold) return;
		}
		for (OutputAction out : outputs) {
			if (value >= out.min && value <= out.max) out.effector.run(ctx, value);
		}
	}


	public int index() {
		return index;
	}

	public boolean isInput() {
		return isInput;
	}

	private record OutputAction(double min, double max, Effector<Object> effector) {}

	private record Cancellation(double min, double max, int inhibitor, double threshold) {}
}
