package deus.brainless.goap.selectors;

import java.util.List;
import java.util.Optional;

import deus.brainless.agent.Intent;
import deus.brainless.agent.Proposal;
import deus.brainless.goap.arbitration.ArbiterConfig;

@FunctionalInterface
public interface Selector {
	Optional<Proposal> select(List<Proposal> proposals, Intent current, ArbiterConfig config);
}
