package deus.brainless.goap.arbitration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import deus.brainless.drive.Drive;
import deus.brainless.drive.Proposal;
import deus.brainless.goap.selectors.Selector;

/**
 * Collects what the psyche asked for this tick and hands back the one drive it
 * settled on. Holds no memory between calls: it does not know, and does not
 * need to know, what the agent is already doing.
 */
public class GoapArbiter {
	private final List<Proposal> pending = new ArrayList<>();
	private final Selector selector;
	private final ArbiterConfig config;


	public GoapArbiter(Selector selector, ArbiterConfig config) {
		this.selector = selector;
		this.config = config;
	}

	public void propose(Proposal p) { pending.add(p); }
	public void propose(List<Proposal> pps) { pending.addAll(pps); }

	/** Consumes everything proposed so far. */
	public Optional<Drive> resolve() {
		Optional<Proposal> chosen = selector.select(List.copyOf(pending), this.config);
		pending.clear();
		return chosen.map(Proposal::drive)
			.or(() -> Optional.ofNullable(config.fallback()));
	}
}
