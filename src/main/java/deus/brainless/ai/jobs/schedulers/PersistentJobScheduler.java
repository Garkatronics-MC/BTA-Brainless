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
			currentJob.onFinish(ctx);
			currentJob = null;
			currentDef = null;
		}

		JobDefinition<CTX> bestCandidate = getBestCandidate();

		if (currentJob != null && bestCandidate != null && bestCandidate != currentDef) {

			if (bestCandidate.getPriorityCategory() > currentDef.getPriorityCategory()) {
				changeJob(bestCandidate, ctx);
			}
			else if (bestCandidate.getPriorityCategory() == currentDef.getPriorityCategory()) {
				double currentDesire = currentDef.desireNode().value;
				double candidateDesire = bestCandidate.desireNode().value;

				if (candidateDesire > currentDesire + currentDef.getInterruptionThreshold()) {
					changeJob(bestCandidate, ctx);
				}
			}
		}

		if (currentJob == null && bestCandidate != null && bestCandidate.desireNode().value > 0) {
			changeJob(bestCandidate, ctx);
		}

		if (currentJob != null) {
			currentJob.tick(ctx);
		}
	}

	private void changeJob(JobDefinition<CTX> newDef, CTX ctx) {
		if (currentJob != null) {
			currentJob.onFinish(ctx);
		}
		this.currentDef = newDef;
		this.currentJob = newDef.createJob();
	}

	@Override
	public void clear() {
		super.clear();
	}
}
