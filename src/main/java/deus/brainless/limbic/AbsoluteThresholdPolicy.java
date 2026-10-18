package deus.brainless.limbic;

import deus.brainless.drive.Proposal;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives propose above their absolute threshold; if none does, the loudest valid
 * drive proposes once it clears the fallback. Silenced drives are excluded.
 */
public final class AbsoluteThresholdPolicy implements DrivePolicy {

	public static final double DEFAULT_FALLBACK_THRESHOLD = 0.30;

	private final double fallbackThreshold;

	public AbsoluteThresholdPolicy() {
		this(DEFAULT_FALLBACK_THRESHOLD);
	}

	public AbsoluteThresholdPolicy(double fallbackThreshold) {
		if (fallbackThreshold < 0.0 || fallbackThreshold > 1.0)
			throw new IllegalArgumentException("fallbackThreshold must be within [0,1], was " + fallbackThreshold);
		this.fallbackThreshold = fallbackThreshold;
	}

	public double fallbackThreshold() {
		return fallbackThreshold;
	}

	@Override
	public List<Proposal> evaluate(Urges urges, List<DriveBinding> bindings) {
		List<Proposal> proposals = new ArrayList<>();

		DriveBinding dominant = null;
		double dominantValue = -1.0;

		for (DriveBinding binding : bindings) {
			double value = urges.get(binding.sensor().index());
			if (binding.sensor().silenced(value, urges)) continue;

			// Strict >, so an exact tie keeps the first declaration: declaration
			// order decides, not hash or iteration luck.
			if (value > dominantValue) {
				dominantValue = value;
				dominant = binding;
			}

			if (binding.matches(value)) {
				proposals.add(new Proposal(binding.drive(), value));
			}
		}

		if (proposals.isEmpty() && dominant != null && dominantValue >= fallbackThreshold) {
			proposals.add(new Proposal(dominant.drive(), dominantValue));
		}

		return proposals;
	}

	@Override
	public String toString() {
		return "AbsoluteThresholdPolicy[fallback=" + fallbackThreshold + "]";
	}
}