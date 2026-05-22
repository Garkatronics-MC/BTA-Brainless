package deus.brainless.ai.jobs.schedulers;

import deus.brainless.ai.interfaces.Job;

import java.util.ArrayDeque;
import java.util.Deque;

public class PersistentJobScheduler<CTX> extends AbstractJobScheduler<CTX> {

	private final Deque<Job<CTX>> queue = new ArrayDeque<>();
	private Job<CTX> current;

	public PersistentJobScheduler(Mode mode) {
		super(mode);
	}

	public PersistentJobScheduler() {
		this(Mode.FIFO_TIERED);
	}

	@Override
	public void update(CTX ctx) {
		if (current == null || current.isDone(ctx) || current.interrupt()) {
			if (current != null) current.onFinish(ctx);
			if (queue.isEmpty()) refill();
			current = queue.pollFirst();
		}
		if (current != null) current.tick(ctx);
	}

	private void refill() {
		buildQueue().forEach(queue::addLast);
	}

	@Override public Job<CTX> current() { return current; }
	@Override public void interruptCurrent(CTX ctx) { if (current != null) { current.onFinish(ctx); current = null; } }
	@Override public void clear() { queue.clear(); definitions.clear(); current = null; }
}
