package deus.brainless.oldAgent.jobs.schedulers;

import deus.brainless.oldAgent.jobs.JobDefinition;

@Deprecated(since = "1.5.0", forRemoval = true)
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
			advanceQueue(ctx);
		}

		JobDefinition<CTX> best = getBestCandidate();
		if (currentJob == null || shouldSwitch(best)) {
			if (best != null && best.desireNode().value > 0) {
				loadDefinition(best, ctx);
			}
		}

		if (currentJob != null) {
			currentJob.tick(ctx);
		}
	}

	private boolean shouldSwitch(JobDefinition<CTX> best) {
		if (best == null || best == currentDef) return false;
		boolean higherCategory = best.getPriorityCategory() > currentDef.getPriorityCategory();
		boolean sameCategory   = best.getPriorityCategory() == currentDef.getPriorityCategory();
		boolean desireGap      = best.desireNode().value > currentDef.desireNode().value + currentDef.getInterruptionThreshold();
		return higherCategory || (sameCategory && desireGap);
	}
}
