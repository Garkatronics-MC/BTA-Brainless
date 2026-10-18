package deus.brainless.goap.arbitration;

import deus.brainless.drive.Proposal;
import deus.brainless.goap.arbitration.ArbiterConfig;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HighestPriorityProposalSelector implements ProposalSelector {

	@Override
	public Optional<Proposal> select(List<Proposal> proposals, ArbiterConfig config) {
		return proposals.stream()
			.filter(p -> p.priority() >= config.minPriority())
			.max(Comparator.comparingDouble(Proposal::priority));
	}
}