package deus.brainless.ai.jobs;

import deus.brainless.ai.connection.Node;
import deus.brainless.ai.interfaces.Job;
import java.util.function.Supplier;


public class JobDefinition<CTX> {

	private final String name;
	private final Node desireNode;
	private final Supplier<Job<CTX>> factory;

	private int priorityCategory = 0;
	private double interruptionThreshold = 0.15;

	public JobDefinition(String name, Node desireNode, Supplier<Job<CTX>> factory) {
		this.name = name;
		this.desireNode = desireNode;
		this.factory = factory;
	}

	public Job<CTX> createJob() {
		return factory.get();
	}

	public JobDefinition<CTX> withCategory(int category) {
		this.priorityCategory = category;
		return this;
	}

	public JobDefinition<CTX> withThreshold(double threshold) {
		this.interruptionThreshold = threshold;
		return this;
	}


	public String name() {
		return name;
	}

	public Node desireNode() {
		return desireNode;
	}

	public Supplier<Job<CTX>> factory() {
		return factory;
	}

	public int getPriorityCategory() {
		return priorityCategory;
	}

	public double getInterruptionThreshold() {
		return interruptionThreshold;
	}
}
