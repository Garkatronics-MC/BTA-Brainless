package deus.brainless.limbic;

public class InputBuilder<C> {
	private final Temperament<C> template;

	InputBuilder(Temperament<C> temperament) {
		this.template = temperament;
	}

	public InputBuilder<C> declare(String name, double initialValue) {
		template.createNode(name, true).setInitialValue(initialValue);
		return this;
	}

	public InputBuilder<C> declare(String name) {
		return declare(name, 0.0);
	}
}
