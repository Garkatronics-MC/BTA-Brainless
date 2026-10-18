package deus.brainless.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logger under its own name so example output is filterable. */
public final class ExampleLog {

	public static final Logger LOGGER = LoggerFactory.getLogger("brainless.example");

	private ExampleLog() {}
}
