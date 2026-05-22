package deus.brainless.ai.jobs;

import deus.brainless.ai.connection.Node;
import deus.brainless.ai.interfaces.Job;
import deus.brainless.ai.interfaces.Task;

import java.util.function.Consumer;

public record JobDefinition<CTX>(
	String name,
	Node desireNode,
	Consumer<CTX> tick,
	Task<CTX> onInterrupt
) {}
