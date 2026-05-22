package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.connection.Node;
import deus.brainless.ai.interfaces.Job;
import deus.brainless.ai.interfaces.JobScheduler;
import deus.brainless.ai.jobs.JobDefinition;
import deus.brainless.ai.interfaces.Task;

import java.util.*;
import java.util.function.Consumer;
public class InstantJobScheduler<CTX> extends AbstractJobScheduler<CTX> {

	private final double discardThreshold;
	private final int preCalculateCount;
	private final Map<String, Double> snapshot = new HashMap<>();
	private final Deque<Job<CTX>> queue = new ArrayDeque<>();
	private Job<CTX> current;

	public InstantJobScheduler(Mode mode, double discardThreshold, int preCalculateCount) {
		super(mode);
		this.discardThreshold = discardThreshold;
		this.preCalculateCount = preCalculateCount;
	}

	@Override
	public void update(CTX ctx) {
		if (shouldDiscard()) { interruptCurrent(ctx); queue.clear(); rebuild(); }
		if (queue.isEmpty()) rebuild();

		if (!queue.isEmpty()) {
			current = queue.poll();
			current.tick(ctx);
			if (current.isDone(ctx)) current = null;
		}
	}

	private void rebuild() {
		definitions.forEach(d -> snapshot.put(d.name(), d.desireNode().value));
		List<Job<CTX>> built = buildQueue();
		queue.clear();
		built.stream().limit(preCalculateCount).forEach(queue::add);
	}

	private boolean shouldDiscard() {
		if (snapshot.isEmpty()) return true;
		double delta = definitions.stream()
			.mapToDouble(d -> Math.abs(d.desireNode().value - snapshot.getOrDefault(d.name(), 0.0)))
			.sum();
		return delta >= discardThreshold;
	}

	@Override public Job<CTX> current() { return current; }
	@Override public void interruptCurrent(CTX ctx) { if (current != null) current.cancel(ctx); }
	@Override public void clear() { queue.clear(); definitions.clear(); snapshot.clear(); current = null; }
}
