package deus.brainless.limbic;

import deus.brainless.drive.Drive;

/**
 * One declaration frozen at build time: sensor {@code name} in {@code [min, max]}
 * speaks for {@code drive}. {@code maxInclusive} controls whether {@code max} counts.
 */
public record DriveBinding(String name, Sensor sensor, double min, double max,
                           boolean maxInclusive, Drive drive) {

	public boolean matches(double value) {
		return value >= min && (maxInclusive ? value <= max : value < max);
	}

	public String describe() {
		return name + "[" + min + "," + (maxInclusive ? "" : "<") + max + "] -> " + drive.id();
	}
}