package deus.brainless.limbic;

import deus.brainless.drive.Drive;

import java.util.List;

public final class Psyches {

	private Psyches() {}

	public static Psyche from(Temperament template) {
		return new Psyche(template);
	}

	public static Psyche from(Temperament template, double fallbackThreshold) {
		return new Psyche(template, new AbsoluteThresholdPolicy(fallbackThreshold));
	}

	public static List<Drive> declaredDrives(Temperament template) {
		return List.copyOf(template.declaredDrives());
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