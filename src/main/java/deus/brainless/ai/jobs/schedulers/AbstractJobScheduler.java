package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.interfaces.Job;
import deus.brainless.ai.interfaces.JobScheduler;
import deus.brainless.ai.jobs.JobDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class AbstractJobScheduler<CTX> implements JobScheduler<CTX> {

	public enum Mode { HIGHEST_WINS, FIFO_TIERED, WEIGHTED_RANDOM }

	protected final Mode mode;
	protected final List<JobDefinition<CTX>> definitions = new ArrayList<>();
	protected final Random random = new Random();

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
		}
	}

	@Override
	public void clear() {
		currentJob = null;
		currentDef = null;
		definitions.clear();
	}

	protected boolean handleJobCompletion(CTX ctx) {
		if (currentJob == null) return false;
		Optional<Job<CTX>> next = currentJob.getNext();
		currentJob.onFinish(ctx);
		if (next.isPresent()) {
			currentJob = next.get();
			return true;
		}
		currentJob = null;
		currentDef = null;
		return false;
	}
	protected JobDefinition<CTX> getBestCandidate() {
		if (definitions.isEmpty()) return null;

		return switch (mode) {
			case HIGHEST_WINS -> definitions.stream()
				.filter(d -> d.desireNode().value > 0)
				.max((a, b) -> Double.compare(a.desireNode().value, b.desireNode().value))
				.orElse(null);

			case FIFO_TIERED -> {
				JobDefinition<CTX> best = null;
				double highestValue = -1.0;
				for (JobDefinition<CTX> d : definitions) {
					if (d.desireNode().value > highestValue) {
						highestValue = d.desireNode().value;
						best = d;
					}
				}
				yield highestValue > 0 ? best : null;
			}

			case WEIGHTED_RANDOM -> {
				double total = definitions.stream().filter(d -> d.desireNode().value > 0).mapToDouble(d -> d.desireNode().value).sum();
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

	protected List<Job<CTX>> buildQueue() {
		return switch (mode) {
			case HIGHEST_WINS -> buildHighest();
			case FIFO_TIERED  -> buildTiered();
			case WEIGHTED_RANDOM -> buildWeighted();
		};
	}

	private List<Job<CTX>> buildHighest() {
		return definitions.stream()
			.filter(d -> d.desireNode().value > 0)
			.sorted((a, b) -> Double.compare(b.desireNode().value, a.desireNode().value))
			.map(JobDefinition::createJob)
			.toList();
	}

	private List<Job<CTX>> buildTiered() {
		List<Job<CTX>> out = new ArrayList<>();
		List<JobDefinition<CTX>> high = new ArrayList<>(), mid = new ArrayList<>(), low = new ArrayList<>();

		for (JobDefinition<CTX> d : definitions) {
			if (d.desireNode().value <= 0) continue;
			if (d.desireNode().value >= 0.7)      high.add(d);
			else if (d.desireNode().value >= 0.3) mid.add(d);
			else                                  low.add(d);
		}

		high.forEach(d -> out.add(d.createJob()));
		mid.forEach(d -> out.add(d.createJob()));
		low.forEach(d -> out.add(d.createJob()));
		return out;
	}

	private List<Job<CTX>> buildWeighted() {
		List<JobDefinition<CTX>> mutable = definitions.stream()
			.filter(d -> d.desireNode().value > 0)
			.collect(Collectors.toCollection(ArrayList::new));

		List<Job<CTX>> out = new ArrayList<>();

		for (int i = 0; i < mutable.size() && !mutable.isEmpty(); i++) {
			double total = mutable.stream().mapToDouble(d -> d.desireNode().value).sum();
			double roll = random.nextDouble() * total;
			double acc = 0;

			for (int j = 0; j < mutable.size(); j++) {
				acc += mutable.get(j).desireNode().value;
				if (acc >= roll) {
					out.add(mutable.remove(j).createJob());
					break;
				}
			}
		}
		return out;
	}
}
