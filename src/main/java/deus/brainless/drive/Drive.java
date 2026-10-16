package deus.brainless.drive;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An impulse the body or the character demands of the agent: hunger, fear,
 * fatigue, boredom. Not a goal, not a behaviour — the want, before anyone has
 * decided what to do about it. Carries no action.
 *
 * <p>Open to extension: {@link #of(String)} accepts any id, including names
 * owned by other mods. The set is deliberately never closed, so consumers must
 * handle drives they do not recognise.
 *
 * <p>Ids are normalised (trimmed, lowercased) and instances are interned, so
 * two references to {@code "Hunger"} and {@code "hunger"} are the same drive.
 * That identity is load-bearing: it is how a consumer recognises "this is the
 * drive I am already on" rather than a new one that merely looks similar.
 */
public final class Drive {

	private static final Map<String, Drive> REGISTRY = new ConcurrentHashMap<>();

	private final String id;

	private Drive(String id) {
		this.id = id;
	}

	/** Returns the interned drive for {@code id}, creating it on first use. */
	public static Drive of(String id) {
		Objects.requireNonNull(id, "Drive id must not be null");
		String key = id.trim().toLowerCase(Locale.ROOT);
		if (key.isEmpty()) throw new IllegalArgumentException("Drive id must not be empty");
		return REGISTRY.computeIfAbsent(key, Drive::new);
	}

	public String id() {
		return id;
	}

	/** Every drive id created so far, in no particular order. Intended for debug tooling. */
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