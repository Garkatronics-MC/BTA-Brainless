package deus.brainless.limbic;

/**
 * Declares the inputs a temperament reads. Handed to {@link Temperament#inputs}
 * as a lambda and never kept: what it declares belongs to the temperament, not
 * to this.
 *
 * <p>Declaring the same name twice is an error, not an overwrite — a template
 * that quietly renames itself is a template nobody can reason about.
 */
public class InputBuilder {
	private final Temperament template;

	InputBuilder(Temperament template) {
		this.template = template;
	}

	/** An input that starts at zero. */
	public InputBuilder declare(String name) {
		return declare(name, 0.0);
	}

	/** An input and the value it starts at, before the world ever touches it. */
	public InputBuilder declare(String name, double initialValue) {
		template.createNode(name, true).setInitialValue(initialValue);
		return this;
	}
}
