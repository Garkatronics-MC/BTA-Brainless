package deus.brainless.limbic;


import deus.brainless.drive.Drive;

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

	/** Declares: while this sensor sits in that window, that drive is being asked for. */
	void triggerOn(double min, double max, boolean maxInclusive, Drive drive) {
		outputs.add(new OutputAction(min, max, maxInclusive, drive));
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

	/**
	 * Whether an inhibitor is currently holding this sensor down. A silenced
	 * sensor proposes nothing and is not eligible to be the dominant drive.
	 */
	boolean silenced(double value, Urges urges) {
		for (Cancellation c : cancellations) {
			if (value >= c.min && value <= c.max && urges.get(c.inhibitor) >= c.threshold) return true;
		}
		return false;
	}

	// Read-only view for inspection/debug.

	public List<OutputAction> outputs() {
		return List.copyOf(outputs);
	}

	public int index() {
		return index;
	}

	public boolean isInput() {
		return isInput;
	}

	public record OutputAction(double min, double max, boolean maxInclusive, Drive drive) {}

	private record Cancellation(double min, double max, int inhibitor, double threshold) {}
}