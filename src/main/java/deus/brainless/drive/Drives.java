package deus.brainless.drive;

/**
 * The drives this library ships. Anything else is created with
 * {@link Drive#of(String)}.
 */
public final class Drives {

	private Drives() {}

	public static final Drive HUNGER  = Drive.of("hunger");
	public static final Drive FEAR    = Drive.of("fear");
	public static final Drive FATIGUE = Drive.of("fatigue");
	public static final Drive HAPPINESS = Drive.of("happiness");
	public static final Drive ANGER = Drive.of("anger");
	public static final Drive STRESS = Drive.of("stress");
	// ? spublic static final Drive CONFIDENCE = Drive.of("confidence");
}
