package deus.brainless.goap.arbitration;

import java.util.List;
import java.util.Optional;

import deus.brainless.drive.Proposal;

/** Decides which proposal wins the arbitration, before anything happens. */
@FunctionalInterface
public interface ProposalSelector {

	Optional<Proposal> select(List<Proposal> proposals, ArbiterConfig config);
}
