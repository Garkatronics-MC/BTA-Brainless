package deus.brainless.goap;

import java.util.List;
import java.util.Optional;

import deus.brainless.agent.Intent;
import deus.brainless.agent.Proposal;

@FunctionalInterface
public interface Selector {
	Optional<Proposal> select(List<Proposal> proposals, Intent current, ArbiterConfig config);
}
