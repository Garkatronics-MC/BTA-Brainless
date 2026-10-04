package deus.brainless.systemOne;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class AsyncDefaults {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	static final Executor EXECUTOR = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "systemone-async-" + COUNTER.incrementAndGet());
		t.setDaemon(true); // no impide que la JVM termine
		return t;
	});

	private AsyncDefaults() {}
}
