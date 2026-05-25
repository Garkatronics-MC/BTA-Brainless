package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.interfaces.Job;
import deus.brainless.ai.interfaces.JobScheduler;
import deus.brainless.ai.jobs.JobDefinition;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractJobScheduler<CTX> implements JobScheduler<CTX> {

	public enum Mode { HIGHEST_WINS, FIFO_TIERED, WEIGHTED_RANDOM }

	protected final Mode mode;
	protected final List<JobDefinition<CTX>> definitions = new ArrayList<>();
	protected final Random random = new Random();

	protected final Deque<Job<CTX>> jobQueue = new ArrayDeque<>();
	protected Job<CTX> currentJob = null;
	protected JobDefinition<CTX> currentDef = null;

	protected AbstractJobScheduler(Mode mode) {
		this.mode = mode;
	}

	@Override
	public void register(JobDefinition<CTX> def) {
		definitions.add(def);
	}

	@Override
	public Job<CTX> current() {
		return currentJob;
	}

	@Override
	public void interruptCurrent(CTX ctx) {
		if (currentJob != null) {
			currentJob.onFinish(ctx);
			currentJob = null;
			currentDef = null;
			jobQueue.clear();
		}
	}

	@Override
	public void clear() {
		currentJob = null;
		currentDef = null;
		jobQueue.clear();
		definitions.clear();
	}

	protected boolean advanceQueue(CTX ctx) {
		currentJob.onFinish(ctx);
		Job<CTX> prev = currentJob;
		currentJob = jobQueue.poll();
		if (currentJob != null) {
			currentJob.setParent(prev.name());
			return true;
		}
		currentDef = null;
		return false;
	}

	protected void loadDefinition(JobDefinition<CTX> def, CTX ctx) {
		if (currentJob != null) currentJob.onFinish(ctx);
		jobQueue.clear();

		List<Job<CTX>> jobs = def.createJobs(ctx);
		jobQueue.addAll(jobs);

		currentDef = def;
		currentJob = jobQueue.poll();
	}

	protected JobDefinition<CTX> getBestCandidate() {
		if (definitions.isEmpty()) return null;

		return switch (mode) {
			case HIGHEST_WINS -> definitions.stream()
				.filter(d -> d.desireNode().value > 0)
				.max(Comparator.comparingDouble(d -> d.desireNode().value))
				.orElse(null);

			case FIFO_TIERED -> {
				JobDefinition<CTX> best = null;
				double highest = -1.0;
				for (JobDefinition<CTX> d : definitions) {
					if (d.desireNode().value > highest) {
						highest = d.desireNode().value;
						best = d;
					}
				}
				yield highest > 0 ? best : null;
			}

			case WEIGHTED_RANDOM -> {
				double total = definitions.stream()
					.filter(d -> d.desireNode().value > 0)
					.mapToDouble(d -> d.desireNode().value).sum();
				if (total <= 0) yield null;
				double roll = random.nextDouble() * total;
				double acc = 0;
				for (JobDefinition<CTX> d : definitions) {
					if (d.desireNode().value <= 0) continue;
					acc += d.desireNode().value;
					if (acc >= roll) yield d;
				}
				yield null;
			}
		};
	}
}
