package deus.brainless.goap.astar;

import java.util.HashMap;
import java.util.Map;

public class GState {

	private static final float PRECISION = 100f; // 2 decimals

	private final Map<String, Float> values;
	private final Map<String, float[]> ranges; // clave -> {min, max}

	public GState() {
		this.values = new HashMap<>();
		this.ranges = new HashMap<>();
	}

	public GState(GState other) {
		this.values = new HashMap<>(other.values);
		this.ranges = other.ranges;
	}

	public GState copy() {
		return new GState(this);
	}

	public GState range(String key, float min, float max) {
		ranges.put(key, new float[]{min, max});
		return this;
	}

	private float normalize(String key, float value) {
		float[] r = ranges.get(key);
		if (r != null) value = Math.max(r[0], Math.min(r[1], value));
		return Math.round(value * PRECISION) / PRECISION;
	}

	public GState set(String key, float value) {
		values.put(key, normalize(key, value));
		return this;
	}

	public GState add(String key, float delta) {
		return set(key, get(key) + delta);
	}

	public float get(String key) {
		return values.getOrDefault(key, 0f);
	}

}
