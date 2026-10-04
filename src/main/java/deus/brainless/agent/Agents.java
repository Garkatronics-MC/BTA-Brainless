package deus.brainless.agent;

public final class Agents {

	private Agents() {}

	public static <C> Agent<C> from(AgentTemplate<C> template, C context) {
		return new Agent<C>(template, context);
	}

	public static double clamp(double value) {
		return Math.max(0.0, Math.min(1.0, value));
	}

	public static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

	public static double lerp(double a, double b, double t) {
		return a + (b - a) * clamp(t);
	}
}
