package deus.brainless.limbic;


import deus.brainless.drive.Drive;

import java.util.function.BiFunction;

public class MixBuilder {

	private final Sensor sensor;
	private final Temperament temperament;

	public MixBuilder(Sensor sensor, Temperament temperament) {
		this.sensor = sensor;
		this.temperament = temperament;
	}

	public MixBuilder add(String input, double weight) {
		return connect(input, Influence.ADD, weight);
	}

	public MixBuilder sub(String input, double weight) {
		return connect(input, Influence.SUB, weight);
	}

	public MixBuilder mul(String input, double weight) {
		return connect(input, Influence.MUL, weight);
	}

	public MixBuilder div(String input, double weight) {
		return connect(input, Influence.DIV, weight);
	}

	public MixBuilder max(String input, double weight) {
		return connect(input, Influence.MAX, weight);
	}

	public MixBuilder min(String input, double weight) {
		return connect(input, Influence.MIN, weight);
	}

	public MixBuilder op(String input, BiFunction<Double, Double, Double> op, double weight) {
		return connect(input, op, weight);
	}

	private MixBuilder connect(String inputName, BiFunction<Double, Double, Double> op, double weight) {
		Sensor inputSensor = temperament.getNode(inputName);
		inputSensor.connect(sensor, op, weight);
		return this;
	}

	public MixBuilder sigmoid(double factor) {
		sensor.sigmoid(factor);
		return this;
	}

	public MixBuilder suppressedBy(String inhibitor, double threshold) {
		Sensor inh = temperament.getNode(inhibitor);
		sensor.canceledBy(threshold, 1.0, inh);
		return this;
	}

	public MixBuilder debug(String label) {
		sensor.onCompute(v -> System.out.printf("  [%s] = %.4f%n", label, v));
		return this;
	}

	/**
	 * Asks for {@code drive} whenever this sensor sits between {@code min} and
	 * {@code max}, both ends included. Proposals overlapping a shared boundary
	 * both match — declare disjoint windows if you only want one.
	 */
	public MixBuilder onRange(double min, double max, Drive drive) {
		return onRange(min, max, true, drive);
	}

	/** As above, but {@code max} itself does not count. */
	public MixBuilder onRange(double min, double max, boolean maxInclusive, Drive drive) {
		sensor.triggerOn(min, max, maxInclusive, drive);
		temperament.invalidateBindings();
		return this;
	}

	// The three bands are disjoint: 0.70 belongs to onHigh, not to onMedium.

	public MixBuilder onHigh(Drive drive)   { return onRange(0.7, 1.0, true, drive); }
	public MixBuilder onMedium(Drive drive) { return onRange(0.3, 0.7, false, drive); }
	public MixBuilder onLow(Drive drive)    { return onRange(0.0, 0.3, false, drive); }

	public MixBuilder above(double t, Drive drive) { return onRange(t, 1.01, drive); }
	public MixBuilder below(double t, Drive drive) { return onRange(0.0, t, drive); }

	public MixBuilder onExact(double value, double epsilon, Drive drive) {
		return onRange(value - epsilon, value + epsilon, drive);
	}

	public Sensor node() { return sensor; }
}