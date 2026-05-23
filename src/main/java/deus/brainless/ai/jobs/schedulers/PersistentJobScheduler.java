package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.jobs.JobDefinition;

public class PersistentJobScheduler<CTX> extends AbstractJobScheduler<CTX> {

	public PersistentJobScheduler(Mode mode) {
		super(mode);
	}

	public PersistentJobScheduler() {
		this(Mode.HIGHEST_WINS);
	}

	@Override
	public void update(CTX ctx) {
		if (currentJob != null && currentJob.isDone(ctx)) {
			boolean advanced = advanceQueue(ctx);
			if (advanced && currentJob != null) {
				currentJob.tick(ctx);
				return;
			}
		}

		JobDefinition<CTX> best = getBestCandidate();

		if (currentJob != null && best != null && best != currentDef) {
			boolean higherCategory = best.getPriorityCategory() > currentDef.getPriorityCategory();
			boolean sameCategory = best.getPriorityCategory() == currentDef.getPriorityCategory();
			boolean desireGap = best.desireNode().value > currentDef.desireNode().value + currentDef.getInterruptionThreshold();

			if (higherCategory || (sameCategory && desireGap)) {
				loadDefinition(best, ctx);
			}
		}

		if (currentJob == null && best != null && best.desireNode().value > 0) {
			loadDefinition(best, ctx);
		}

		if (currentJob != null) {
			currentJob.tick(ctx);
		}
	}
}
