package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.interfaces.Job;

import java.util.*;

public class InstantJobScheduler<CTX> extends AbstractJobScheduler<CTX> {

	private final double discardThreshold;
	private final int preCalculateCount;
	private final Map<String, Double> snapshot = new HashMap<>();
	private final Deque<Job<CTX>> queue = new ArrayDeque<>();

	public InstantJobScheduler(Mode mode, double discardThreshold, int preCalculateCount) {
		super(mode);
		this.discardThreshold = discardThreshold;
		this.preCalculateCount = preCalculateCount;
	}

	@Override
	public void update(CTX ctx) {
		if (shouldDiscard()) {
			interruptCurrent(ctx);
			queue.clear();
			rebuild();
		}
		if (queue.isEmpty()) rebuild();

		if (!queue.isEmpty()) {
			currentJob = queue.poll();
			currentJob.tick(ctx);

			if (currentJob.isDone(ctx)) {
				currentJob.onFinish(ctx);
				currentJob = null;
			}
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

	@Override
	public void clear() {
		super.clear();
		queue.clear();
		snapshot.clear();
	}
}
