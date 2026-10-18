package deus.brainless.drive;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An impulse the body demands: hunger, fear, fatigue. The want, before anyone decides what to do about it.
 */
public final class Drive {

	private static final Map<String, Drive> REGISTRY = new ConcurrentHashMap<>();

	private final String id;

	private Drive(String id) {
		this.id = id;
	}

	public static Drive of(String id) {
		Objects.requireNonNull(id, "Drive id must not be null");
		String key = id.trim().toLowerCase(Locale.ROOT);
		if (key.isEmpty()) throw new IllegalArgumentException("Drive id must not be empty");
		return REGISTRY.computeIfAbsent(key, Drive::new);
	}

	public String id() {
		return id;
	}

	public static Set<String> registeredIds() {
		return Set.copyOf(REGISTRY.keySet());
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof Drive other && other.id.equals(this.id));
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Drive[" + id + "]";
	}
}