package deus.brainless.goap;

import java.util.*;

public class GoapState {

	private static final float PRECISION = 100f; // 2 decimals

	private final Map<String, Float> values;
	private final Map<String, float[]> ranges; // key -> {min, max}

	public GoapState() {
		this.values = new HashMap<>();
		this.ranges = new HashMap<>();
	}

	public GoapState(GoapState other) {
		this.values = new HashMap<>(other.values);
		this.ranges = other.ranges;
	}

	public GoapState copy() {
		return new GoapState(this);
	}

	public String toKey() {
		StringJoiner joiner = new StringJoiner(";");
		for (Map.Entry<String, Float> e : new TreeMap<>(values).entrySet()) {
			joiner.add(String.format(Locale.ROOT, "%s=%.4f", e.getKey(), e.getValue() + 0.0f));
		}
		return joiner.toString();
	}

	public GoapState range(String key, float min, float max) {
		ranges.put(key, new float[]{min, max});
		return this;
	}

	private float normalize(String key, float value) {
		float[] r = ranges.get(key);
		if (r != null) value = Math.max(r[0], Math.min(r[1], value));
		return Math.round(value * PRECISION) / PRECISION;
	}

	public GoapState set(String key, float value) {
		values.put(key, normalize(key, value));
		return this;
	}

	public GoapState add(String key, float delta) {
		return set(key, get(key) + delta);
	}

	public float get(String key) {
		return values.getOrDefault(key, 0f);
	}

	// Two states are equal when all their values are equal.
	// Needed so A* recognizes states it has already seen.
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GoapState)) return false;
		return values.equals(((GoapState) o).values);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
