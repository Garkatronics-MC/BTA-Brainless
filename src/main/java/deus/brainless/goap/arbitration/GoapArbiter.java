package deus.brainless.goap.arbitration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import deus.brainless.drive.Drive;
import deus.brainless.drive.Proposal;

/** Hands back the one drive the psyche settled on this tick. Holds no memory between calls. */
public class GoapArbiter {
	private final List<Proposal> pending = new ArrayList<>();
	private final ProposalSelector selector;
	private final ArbiterConfig config;


	public GoapArbiter(ProposalSelector selector, ArbiterConfig config) {
		this.selector = selector;
		this.config = config;
	}

	public void propose(Proposal p) { pending.add(p); }
	public void propose(List<Proposal> pps) { pending.addAll(pps); }

	public Optional<Drive> resolve() {
		Optional<Proposal> chosen = selector.select(List.copyOf(pending), this.config);
		pending.clear();
		return chosen.map(Proposal::drive)
			.or(() -> Optional.ofNullable(config.fallback()));
	}
}
