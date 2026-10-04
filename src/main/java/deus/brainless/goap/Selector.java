package deus.brainless.goap;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface Selector {
	Optional<Proposal> select(List<Proposal> proposals, Intent current, ArbiterConfig config);
}
