package deus.brainless.systemOne.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import deus.brainless.systemOne.SystemOne;
import deus.brainless.systemOne.SystemOneException;
import deus.brainless.systemOne.SystemOneProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class GenericProvider implements SystemOneProvider {

	public static final String DEFAULT_URL = "https://api.typesafe.ai/v1/systemone";
	public static final String DEFAULT_KEY_ENV = "SYSTEM_ONE_API_KEY";

	private static final int MAX_ATTEMPTS = 3;

	private static final ObjectMapper MAPPER = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final HttpClient CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.build();

	private final URI uri;
	private final String keyName;

	public GenericProvider() {
		this(DEFAULT_URL, DEFAULT_KEY_ENV);
	}

	public GenericProvider(String url, String envKeyName) {
		this.uri = URI.create(url);
		this.keyName = envKeyName;
	}

	// ---------- Sync ----------

	@Override
	public SystemOne.Response send(SystemOne.Request request) {
		HttpRequest req = buildRequest(request);
		try {
			for (int attempt = 1; ; attempt++) {
				HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

				if (res.statusCode() == 200) {
					return parse(res.body());
				}
				if (!isRetryable(res.statusCode()) || attempt >= MAX_ATTEMPTS) {
					throw httpError(res);
				}
				Thread.sleep(backoffMillis(attempt));
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemOneException("Request interrupted", e);
		} catch (IOException e) {
			throw new SystemOneException("Request failed: " + e.getMessage(), e);
		}
	}

	// ---------- Async native ----------

	@Override
	public CompletableFuture<SystemOne.Response> sendAsync(SystemOne.Request request, Executor executor) {
		HttpRequest req;
		try {
			req = buildRequest(request);
		} catch (SystemOneException e) {
			return CompletableFuture.failedFuture(e);
		}
		return attemptAsync(req, 1, executor);
	}

	private CompletableFuture<SystemOne.Response> attemptAsync(HttpRequest req, int attempt, Executor executor) {
		return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
			.thenComposeAsync(res -> {
				if (res.statusCode() == 200) {
					try {
						return CompletableFuture.completedFuture(parse(res.body()));
					} catch (SystemOneException e) {
						return CompletableFuture.failedFuture(e);
					}
				}
				if (!isRetryable(res.statusCode()) || attempt >= MAX_ATTEMPTS) {
					return CompletableFuture.failedFuture(httpError(res));
				}
				Executor delayed = CompletableFuture.delayedExecutor(
					backoffMillis(attempt), TimeUnit.MILLISECONDS, executor);
				return CompletableFuture
					.supplyAsync(() -> null, delayed)
					.thenCompose(v -> attemptAsync(req, attempt + 1, executor));
			}, executor);
	}


	private HttpRequest buildRequest(SystemOne.Request request) {
		String apiKey = System.getenv(keyName);
		if (apiKey == null || apiKey.isBlank()) {
			throw new SystemOneException("Missing API key: environment variable " + keyName + " is not set");
		}
		try {
			return HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(15))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request.toMap())))
				.build();
		} catch (IOException e) {
			throw new SystemOneException("Could not serialize request: " + e.getMessage(), e);
		}
	}

	private static SystemOne.Response parse(String body) {
		try {
			return MAPPER.readValue(body, SystemOne.Response.class);
		} catch (IOException e) {
			throw new SystemOneException("Bad response: " + e.getMessage(), e);
		}
	}

	private static boolean isRetryable(int status) {
		return status == 429 || status == 529;
	}

	private static long backoffMillis(int attempt) {
		return 500L << (attempt - 1); // 0.5s, 1s, ...
	}

	private static SystemOneException httpError(HttpResponse<String> res) {
		return new SystemOneException("HTTP " + res.statusCode() + ": " + res.body());
	}
}
