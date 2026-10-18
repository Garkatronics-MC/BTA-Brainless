package deus.brainless.limbic;

/**
 * Declares the inputs a temperament reads, via {@link Temperament#inputs}.
 * Declaring the same name twice is an error, not an overwrite.
 */
public class InputBuilder {
	private final Temperament template;

	InputBuilder(Temperament template) {
		this.template = template;
	}

	public InputBuilder declare(String name) {
		return declare(name, 0.0);
	}

	public InputBuilder declare(String name, double initialValue) {
		template.createNode(name, true).setInitialValue(initialValue);
		return this;
	}
}
