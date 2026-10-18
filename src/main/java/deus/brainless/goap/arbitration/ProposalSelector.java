package deus.brainless.goap.arbitration;

import java.util.List;
import java.util.Optional;

import deus.brainless.drive.Proposal;

/**
 * Decides which proposal wins the arbitration.
 *
 * <p>Takes no interest in what the agent is already doing. Deciding whether to
 * abandon an activity in progress belongs to whoever owns the activity — it
 * depends on how committed that activity is, which no proposal can express.
 *
 * <p>Not to be confused with {@link deus.brainless.task.flow.Fallback}, which
 * also picks one: that one chooses between tasks as it runs them, this one
 * chooses between drives before anything happens.
 */
@FunctionalInterface
public interface ProposalSelector {

	Optional<Proposal> select(List<Proposal> proposals, ArbiterConfig config);
}
