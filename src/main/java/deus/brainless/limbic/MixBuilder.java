package deus.brainless.limbic;


import java.util.function.BiFunction;

public class MixBuilder<C> {

	private final Sensor sensor;
	private final Temperament temperament;

	public MixBuilder(Sensor sensor, Temperament temperament) {
		this.sensor = sensor;
		this.temperament = temperament;
	}

	public MixBuilder<C> add(String input, double weight) {
		return connect(input, Influence.ADD, weight);
	}

	public MixBuilder<C> sub(String input, double weight) {
		return connect(input, Influence.SUB, weight);
	}

	public MixBuilder<C> mul(String input, double weight) {
		return connect(input, Influence.MUL, weight);
	}

	public MixBuilder<C> div(String input, double weight) {
		return connect(input, Influence.DIV, weight);
	}

	public MixBuilder<C> max(String input, double weight) {
		return connect(input, Influence.MAX, weight);
	}

	public MixBuilder<C> min(String input, double weight) {
		return connect(input, Influence.MIN, weight);
	}

	public MixBuilder<C> op(String input, BiFunction<Double, Double, Double> op, double weight) {
		return connect(input, op, weight);
	}

	private MixBuilder<C> connect(String inputName, BiFunction<Double, Double, Double> op, double weight) {
		Sensor inputSensor = temperament.getNode(inputName);
		inputSensor.connect(sensor, op, weight);
		return this;
	}

	public MixBuilder<C> sigmoid(double factor) {
		sensor.sigmoid(factor);
		return this;
	}

	public MixBuilder<C> suppressedBy(String inhibitor, double threshold) {
		Sensor inh = temperament.getNode(inhibitor);
		sensor.canceledBy(threshold, 1.0, inh);
		return this;
	}

	public MixBuilder<C> debug(String label) {
		sensor.onCompute(v -> System.out.printf("  [%s] = %.4f%n", label, v));
		return this;
	}

	@SuppressWarnings("unchecked")
	public MixBuilder<C> onRange(double min, double max, Effector<C> effector) {
		sensor.triggerOn(min, max, (ctx, v) -> effector.run((C) ctx, v));
		return this;
	}

	public MixBuilder<C> onHigh(Effector<C> effector)   { return onRange(0.7, 1.0, effector); }
	public MixBuilder<C> onMedium(Effector<C> effector) { return onRange(0.3, 0.7, effector); }
	public MixBuilder<C> onLow(Effector<C> effector)    { return onRange(0.0, 0.3, effector); }

	public MixBuilder<C> above(double t, Effector<C> effector) { return onRange(t, 1.01, effector); }
	public MixBuilder<C> below(double t, Effector<C> effector) { return onRange(0.0, t, effector); }

	public MixBuilder<C> onExact(double value, double epsilon, Effector<C> effector) {
		return onRange(value - epsilon, value + epsilon, effector);
	}

	public Sensor node() { return sensor; }
}
