package deus.brainless.oldAgent.interfaces;

import deus.brainless.oldAgent.jobs.JobDefinition;

@Deprecated(since = "1.5.0", forRemoval = true)
public interface JobScheduler<CTX> {

	void register(JobDefinition<CTX> job);

	void update(CTX ctx);

	Job<CTX> current();

	void interruptCurrent(CTX ctx);

	void clear();
}
