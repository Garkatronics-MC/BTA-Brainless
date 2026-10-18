package deus.brainless.limbic;

import deus.brainless.drive.Proposal;

import java.util.List;

/**
 * Reads limbic state and reports which drives are asking, and how badly.
 * Interpretation only: never computes needs and never assumes an action happened.
 */
@FunctionalInterface
public interface DrivePolicy {

	List<Proposal> evaluate(Urges urges, List<DriveBinding> bindings);
}