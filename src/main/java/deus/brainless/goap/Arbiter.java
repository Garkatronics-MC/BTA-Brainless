package deus.brainless.goap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Arbiter {
	private final List<Proposal> pending = new ArrayList<>();
	private final Selector selector;
	private final ArbiterConfig config;


	public Arbiter(Selector selector, ArbiterConfig config) {
		this.selector = selector;
		this.config = config;
	}

	public void propose(Proposal p) { pending.add(p); }

	public Optional<Intent> resolve(Intent current) {
		Optional<Proposal> chosen = selector.select(List.copyOf(pending), current, this.config);
		pending.clear();
		return chosen.map(Proposal::intent)
			.or(() -> Optional.ofNullable(config.fallback()))
			.filter(i -> !i.equals(current));
	}
}
