package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.interfaces.Job;

import java.util.*;

public class InstantJobScheduler<CTX> extends AbstractJobScheduler<CTX> {

	private final double discardThreshold;
	private final int preCalculateCount;
	private final Map<String, Double> snapshot = new HashMap<>();

	public InstantJobScheduler(Mode mode, double discardThreshold, int preCalculateCount) {
		super(mode);
		this.discardThreshold = discardThreshold;
		this.preCalculateCount = preCalculateCount;
	}

	@Override
	public void update(CTX ctx) {
		if (shouldDiscard()) {
			interruptCurrent(ctx);
			jobQueue.clear();
			rebuild(ctx);
		}

		if (jobQueue.isEmpty()) rebuild(ctx);

		if (currentJob == null && !jobQueue.isEmpty()) {
			currentJob = jobQueue.poll();
		}

		if (currentJob != null) {
			currentJob.tick(ctx);
			if (currentJob.isDone(ctx)) {
				boolean advanced = advanceQueue(ctx);
				if (advanced && currentJob != null) {
					currentJob.tick(ctx);
				}
			}
		}
	}

	private void rebuild(CTX ctx) {
		snapshot.clear();
		definitions.forEach(d -> snapshot.put(d.name(), d.desireNode().value));

		definitions.stream()
			.filter(d -> d.desireNode().value > 0)
			.sorted((a, b) -> Double.compare(b.desireNode().value, a.desireNode().value))
			.limit(preCalculateCount)
			.forEach(def -> jobQueue.addAll(def.createJobs()));

		currentDef = definitions.stream()
			.filter(d -> d.desireNode().value > 0)
			.max(Comparator.comparingDouble(d -> d.desireNode().value))
			.orElse(null);

		currentJob = jobQueue.poll();
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
		snapshot.clear();
	}
}
