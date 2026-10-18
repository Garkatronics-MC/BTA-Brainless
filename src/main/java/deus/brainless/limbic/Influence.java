package deus.brainless.limbic;

import java.util.function.BiFunction;

/**
 * One wire from a source sensor into a mix. Applied to the mix's running total
 * in declaration order: {@code total = op(total, source * weight)}.
 *
 * <p>The built-in ops are <em>sequential</em>, not independent terms, and the mix
 * starts from {@link Sensor#bias(double)} (0 by default):
 * <ul>
 *   <li>{@link #ADD} — {@code total + source * weight}
 *   <li>{@link #SUB} — {@code total - source * weight}
 *   <li>{@link #MUL} — {@code total * source * weight}; with a zero bias the
 *       product collapses to zero, so set a non-zero bias first
 *   <li>{@link #DIV} — {@code total / source * weight}, or {@code 0} when the
 *       weighted source is zero (division by zero yields zero, not the total)
 *   <li>{@link #MAX} — {@code max(total, source * weight)}
 *   <li>{@link #MIN} — {@code min(total, source * weight)}
 * </ul>
 */
public record Influence(int from, int to, BiFunction<Double, Double, Double> op, double weight) {

	public double apply(double accumulated, double incoming) {
		return op.apply(accumulated, incoming * weight);
	}

	// Built-in ops
	public static final BiFunction<Double, Double, Double> ADD = Double::sum;
	public static final BiFunction<Double, Double, Double> SUB = (a, b) -> a - b;
	public static final BiFunction<Double, Double, Double> MUL = (a, b) -> a * b;
	/** Divides the accumulated total by the incoming value; a zero divisor yields zero. */
	public static final BiFunction<Double, Double, Double> DIV = (a, b) -> b == 0 ? 0 : a / b;
	public static final BiFunction<Double, Double, Double> MAX = Math::max;
	public static final BiFunction<Double, Double, Double> MIN = Math::min;
}
