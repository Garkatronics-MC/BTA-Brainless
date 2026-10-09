package deus.brainless.limbic;

/**
 * Runtime: un AgentTemplate (compartido) + su propio State.
 */
public class Psyche<C> {
	protected final Temperament<C> template;
	protected final Urges urges;
	protected final C context;

	public Psyche(Temperament<C> template, Urges urges, C context) {
		this.template = template;
		this.urges = urges;
		this.context = context;
	}

	public Psyche(Temperament<C> template, C context) {
		this(template, template.newState(), context);
	}

	public void probe() {
		template.probe(urges, context);
	}

	public void reset() {
		template.getNodes().values().forEach(n -> urges.set(n.index(), n.isInput() ? n.initialValue() : 0));
	}

	public Urges copyState() {
		return this.urges.copy();
	}

	public Psyche<C> cloneAgent() {
		return new Psyche<>(template, this.context);
	}

	public double get(String name) {
		return urges.get(template.getNode(name).index());
	}

	public Psyche<C> set(String name, double value) {
		urges.set(template.getNode(name).index(), value);
		return this;
	}

	public Temperament<C> template() {
		return template;
	}

	public Urges state() {
		return urges;
	}
}
