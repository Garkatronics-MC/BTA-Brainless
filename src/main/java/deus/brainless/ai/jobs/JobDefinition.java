package deus.brainless.ai.jobs;

import deus.brainless.ai.connection.Node;
import deus.brainless.ai.interfaces.Job;
import java.util.function.Supplier;

public record JobDefinition<CTX>(
	String name,
	Node desireNode,
	Supplier<Job<CTX>> factory
) {
	public Job<CTX> createJob() {
		return factory.get();
	}


}
