package deus.brainless.goap.selectors;

import deus.brainless.agent.Intent;
import deus.brainless.agent.Proposal;
import deus.brainless.goap.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HighestPrioritySelector implements Selector {

	@Override
	public Optional<Proposal> select(List<Proposal> proposals, Intent current, ArbiterConfig config) {
		return proposals.stream()
			.filter(p -> p.priority() >= config.minPriority())
			.max(Comparator.comparingDouble(p -> score(p, current, config)));
	}

	private double score(Proposal p, Intent current, ArbiterConfig config) {
		return p.intent().equals(current) ? p.priority() + config.hysteresis() : p.priority();
	}
}
