package deus.brainless.ai.interfaces;

import deus.brainless.ai.jobs.JobDefinition;

public interface JobScheduler<CTX> {

	void register(JobDefinition<CTX> job);

	void update(CTX ctx);

	Job<CTX> current();

	void interruptCurrent(CTX ctx);

	void clear();
}
