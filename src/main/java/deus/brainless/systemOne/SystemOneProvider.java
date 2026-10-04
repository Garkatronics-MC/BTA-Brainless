package deus.brainless.systemOne;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface SystemOneProvider {

	SystemOne.Response send(SystemOne.Request request);

	default CompletableFuture<SystemOne.Response> sendAsync(SystemOne.Request request) {
		return sendAsync(request, AsyncDefaults.EXECUTOR);
	}

	default CompletableFuture<SystemOne.Response> sendAsync(SystemOne.Request request, Executor executor) {
		return CompletableFuture.supplyAsync(() -> send(request), executor);
	}
}

