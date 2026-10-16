package deus.brainless.limbic;

public class InputBuilder {
	private final Temperament template;

	InputBuilder(Temperament template) {
		this.template = template;
	}

	public InputBuilder declare(String name, double initialValue) {
		template.createNode(name, true).setInitialValue(initialValue);
		return this;
	}

	public InputBuilder declare(String name) {
		return declare(name, 0.0);
	}
}