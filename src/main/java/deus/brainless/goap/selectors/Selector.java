package deus.brainless.goap.selectors;

import java.util.List;
import java.util.Optional;

import deus.brainless.drive.Proposal;
import deus.brainless.goap.arbitration.ArbiterConfig;

/**
 * Decides which proposal wins.
 *
 * <p>Takes no interest in what the agent is already doing. Deciding whether to
 * abandon an activity in progress belongs to whoever owns the activity — it
 * depends on how committed that activity is, which no proposal can express.
 */
@FunctionalInterface
public interface Selector {
	Optional<Proposal> select(List<Proposal> proposals, ArbiterConfig config);
}