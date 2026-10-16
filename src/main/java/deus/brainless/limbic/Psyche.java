package deus.brainless.limbic;

import deus.brainless.drive.Proposal;

import java.util.List;
import java.util.function.Consumer;

/**
 * Runtime: a shared {@link Temperament} plus this agent's own state.
 *
 * <p>Evaluating is pure: it reads the state, it never writes it, and it never
 * runs world code. The world reaches the psyche through
 * {@link #set(String, double)} before {@link #probe()}.
 */
public final class Psyche {

	private final Temperament template;
	private final Urges urges;
	private final DrivePolicy policy;

	public Psyche(Temperament template) {
		this(template, template.newState(), new AbsoluteThresholdPolicy());
	}

	public Psyche(Temperament template, DrivePolicy policy) {
		this(template, template.newState(), policy);
	}

	public Psyche(Temperament template, Urges urges, DrivePolicy policy) {
		this.template = template;
		this.urges = urges;
		this.policy = policy;
	}

	/**
	 * Recomputes every need from the current state, then asks the policy what is
	 * being requested right now.
	 */
	public List<Proposal> probe() {
		template.probe(urges);
		return policy.evaluate(urges, template.driveBindings());
	}

	/** Allocation-free variant, for running every tick on many agents. */
	public void probe(Consumer<Proposal> sink) {
		template.probe(urges);
		for (Proposal proposal : policy.evaluate(urges, template.driveBindings())) {
			sink.accept(proposal);
		}
	}

	public void reset() {
		template.getNodes().values().forEach(n -> urges.set(n.index(), n.initialValue()));
	}

	public Urges copyState() {
		return this.urges.copy();
	}

	public Psyche cloneAgent() {
		return new Psyche(template, template.newState(), policy);
	}

	public double get(String name) {
		return urges.get(template.getNode(name).index());
	}

	public Psyche set(String name, double value) {
		urges.set(template.getNode(name).index(), value);
		return this;
	}

	public Temperament template() {
		return template;
	}

	public Urges state() {
		return urges;
	}

	public DrivePolicy policy() {
		return policy;
	}
}