package deus.brainless.limbic;

import java.util.function.BiFunction;

public record Influence(int from, int to, BiFunction<Double, Double, Double> op, double weight) {

	public double apply(double accumulated, double incoming) {
		return op.apply(accumulated, incoming * weight);
	}

	// Built-in ops
	public static final BiFunction<Double, Double, Double> ADD = Double::sum;
	public static final BiFunction<Double, Double, Double> SUB = (a, b) -> a - b;
	public static final BiFunction<Double, Double, Double> MUL = (a, b) -> a * b;
	public static final BiFunction<Double, Double, Double> DIV = (a, b) -> b == 0 ? 0 : a / b;
	public static final BiFunction<Double, Double, Double> MAX = Math::max;
	public static final BiFunction<Double, Double, Double> MIN = Math::min;
}
