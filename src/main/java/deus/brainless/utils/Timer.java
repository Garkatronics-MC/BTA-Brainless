package deus.brainless.utils;

/**
 * A stopwatch counted in ticks, for cooldowns and "wait N ticks" work.
 *
 * <p>Advances only when {@link #tick()} is called, matching the task system:
 * whoever owns the game loop drives it. It never reads the wall clock, so it
 * cannot disagree with the tick that woke it, and it cannot run twice if the
 * same tick is processed twice.
 *
 * <p>Durations are kept in nanoseconds so no precision is thrown away at
 * construction, but they are <em>rounded up</em> to whole ticks when asked
 * whether they have passed: {@code fromNano(10_000_000)} — ten milliseconds —
 * is due after one tick, never after zero. If you need genuine sub-tick
 * resolution, this is the wrong class; drive something else off the wall clock.
 *
 * <p>A new timer is already running, so a cooldown is just:
 * <pre>{@code
 * Timer cooldown = Timer.fromTicks(20);
 * if (cooldown.hasElapsed()) { attack(); cooldown.reset(); }
 * }</pre>
 *
 * <p>{@link #stop()} pauses and {@link #start()} resumes from where it paused;
 * neither forgets elapsed time. {@link #reset()} starts a fresh cycle, and
 * also starts the timer unless {@link #setAutoStart(boolean)} says otherwise.
 */
public final class Timer {

	public static final long TICKS_PER_SECOND = 20;
	public static final long NANOS_PER_MILLI = 1_000_000L;
	public static final long NANOS_PER_TICK = 50_000_000L;
	public static final long NANOS_PER_SECOND = TICKS_PER_SECOND * NANOS_PER_TICK;

	private final long durationNanos;
	private long elapsedNanos;
	private boolean running = true;
	private boolean autoStart = true;

	private Timer(long durationNanos) {
		this.durationNanos = durationNanos;
	}

	/**
	 * Exactly {@code ticks} ticks. No rounding.
	 *
	 * @throws IllegalArgumentException if {@code ticks} is negative
	 * @throws ArithmeticException if it does not fit in nanoseconds
	 */
	public static Timer fromTicks(long ticks) {
		if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0, was " + ticks);
		return new Timer(Math.multiplyExact(ticks, NANOS_PER_TICK));
	}

	/**
	 * Exactly {@code seconds}. Fractional seconds survive: {@code fromSecs(0.05)}
	 * is still one tick when it comes due. Infinity means "never".
	 *
	 * @throws IllegalArgumentException if {@code seconds} is negative or NaN
	 */
	public static Timer fromSecs(double seconds) {
		if (Double.isNaN(seconds) || seconds < 0)
			throw new IllegalArgumentException("seconds must be >= 0, was " + seconds);
		if (Double.isInfinite(seconds)) return new Timer(Long.MAX_VALUE);
		return new Timer(Math.round(seconds * NANOS_PER_SECOND));
	}

	/**
	 * Exactly {@code millis}. The unit to reach for under one second.
	 *
	 * <p>{@code long} on purpose: a fractional millisecond is below anything a
	 * tick can resolve, so a {@code double} overload would only buy rounding
	 * noise.
	 *
	 * @throws IllegalArgumentException if {@code millis} is negative
	 * @throws ArithmeticException if it does not fit in nanoseconds
	 */
	public static Timer fromMillis(long millis) {
		if (millis < 0) throw new IllegalArgumentException("millis must be >= 0, was " + millis);
		return fromNano(Math.multiplyExact(millis, NANOS_PER_MILLI));
	}

	/**
	 * Exactly {@code nanos}. The primitive all of the others are built on.
	 *
	 * @throws IllegalArgumentException if {@code nanos} is negative
	 */
	public static Timer fromNano(long nanos) {
		if (nanos < 0) throw new IllegalArgumentException("nanos must be >= 0, was " + nanos);
		return new Timer(nanos);
	}

	// ------------------------------------------------------------------ control

	/**
	 * Resumes from wherever it was paused. If it already ran out, this does
	 * nothing useful — call {@link #reset()} for a fresh cycle.
	 */
	public Timer start() {
		running = true;
		return this;
	}

	/** Pauses. Elapsed time is kept, and {@link #start()} picks up from there. */
	public Timer stop() {
		running = false;
		return this;
	}

	/**
	 * Zeroes the clock and begins a fresh cycle. Also starts the timer unless
	 * autoStart is off, in which case it stays stopped until {@link #start()}.
	 */
	public Timer reset() {
		elapsedNanos = 0;
		running = autoStart;
		return this;
	}

	/**
	 * Whether {@link #reset()} should also start the timer. Applies from now on;
	 * it does not start or stop the timer retroactively.
	 */
	public Timer setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
		return this;
	}

	/** Advances one tick, if running. Saturates rather than wrapping. */
	public Timer tick() {
		if (!running) return this;
		if (elapsedNanos > Long.MAX_VALUE - NANOS_PER_TICK) elapsedNanos = Long.MAX_VALUE;
		else elapsedNanos += NANOS_PER_TICK;
		return this;
	}

	// ------------------------------------------------------------------ state

	/** Whether the requested time has passed. True even while paused. */
	public boolean hasElapsed() {
		return elapsedNanos >= durationNanos;
	}

	/** Whether {@link #tick()} is currently counting. */
	public boolean isRunning() {
		return running;
	}

	public boolean autoStart() {
		return autoStart;
	}

	/** Whole ticks since the last {@link #reset()}, counting while paused. */
	public long elapsedTicks() { return elapsedNanos / NANOS_PER_TICK; }

	public long elapsedNanos() { return elapsedNanos; }

	public double elapsedSecs() { return (double) elapsedNanos / NANOS_PER_SECOND; }

	public long durationNanos() { return durationNanos; }

	public long durationTicks() { return ticksCeil(durationNanos); }

	/** Whole ticks still to go, rounded up. Zero once due. */
	public long remainingTicks() {
		return hasElapsed() ? 0 : ticksCeil(durationNanos - elapsedNanos);
	}

	/** Never negative. Counts down faster only while running. */
	public double remainingSecs() { return (double) remainingTicks() / TICKS_PER_SECOND; }

	/** Rounds up, without overflowing on absurd durations. */
	private static long ticksCeil(long nanos) {
		if (nanos <= 0) return 0;
		long whole = nanos / NANOS_PER_TICK;
		return nanos % NANOS_PER_TICK == 0 ? whole : whole + 1;
	}

	@Override
	public String toString() {
		return "Timer[" + durationTicks() + "t requested, " + elapsedTicks() + "t elapsed"
			+ (running ? "" : ", stopped") + "]";
	}
}