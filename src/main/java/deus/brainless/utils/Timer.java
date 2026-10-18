package deus.brainless.utils;

/** Stopwatch in ticks; durations round up. */
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

	/** @throws IllegalArgumentException if negative */
	public static Timer fromTicks(long ticks) {
		if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0, was " + ticks);
		return new Timer(Math.multiplyExact(ticks, NANOS_PER_TICK));
	}

	/** Infinity means "never". @throws IllegalArgumentException if negative or NaN */
	public static Timer fromSecs(double seconds) {
		if (Double.isNaN(seconds) || seconds < 0)
			throw new IllegalArgumentException("seconds must be >= 0, was " + seconds);
		if (Double.isInfinite(seconds)) return new Timer(Long.MAX_VALUE);
		return new Timer(Math.round(seconds * NANOS_PER_SECOND));
	}

	/** @throws IllegalArgumentException if negative */
	public static Timer fromMillis(long millis) {
		if (millis < 0) throw new IllegalArgumentException("millis must be >= 0, was " + millis);
		return fromNano(Math.multiplyExact(millis, NANOS_PER_MILLI));
	}

	/** @throws IllegalArgumentException if negative */
	public static Timer fromNano(long nanos) {
		if (nanos < 0) throw new IllegalArgumentException("nanos must be >= 0, was " + nanos);
		return new Timer(nanos);
	}

	// ------------------------------------------------------------------ control

	public Timer start() {
		running = true;
		return this;
	}

	public Timer stop() {
		running = false;
		return this;
	}

	public Timer reset() {
		elapsedNanos = 0;
		running = autoStart;
		return this;
	}

	public Timer setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
		return this;
	}

	public Timer tick() {
		if (!running) return this;
		if (elapsedNanos > Long.MAX_VALUE - NANOS_PER_TICK) elapsedNanos = Long.MAX_VALUE;
		else elapsedNanos += NANOS_PER_TICK;
		return this;
	}

	// ------------------------------------------------------------------ state

	public boolean hasElapsed() {
		return elapsedNanos >= durationNanos;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean autoStart() {
		return autoStart;
	}

	public long elapsedTicks() { return elapsedNanos / NANOS_PER_TICK; }

	public long elapsedNanos() { return elapsedNanos; }

	public double elapsedSecs() { return (double) elapsedNanos / NANOS_PER_SECOND; }

	public long durationNanos() { return durationNanos; }

	public long durationTicks() { return ticksCeil(durationNanos); }

	public long remainingTicks() {
		return hasElapsed() ? 0 : ticksCeil(durationNanos - elapsedNanos);
	}

	public double remainingSecs() { return (double) remainingTicks() / TICKS_PER_SECOND; }

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