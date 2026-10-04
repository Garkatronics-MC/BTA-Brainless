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

public class GenericProvider implements SystemOneProvider {

	public static final String DEFAULT_URL = "https://api.typesafe.ai/v1/systemone";
	public static final String DEFAULT_KEY_ENV = "SYSTEM_ONE_API_KEY"; // TYPESAFE_API_KEY

	private static final int MAX_ATTEMPTS = 3;

	private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final HttpClient CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5)) // Make timeout configurable
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

	@Override
	public SystemOne.Response send(SystemOne.Request request) {
		String apiKey = System.getenv(keyName);
		if (apiKey == null || apiKey.isBlank()) {
			throw new SystemOneException("Missing API key: environment variable " + keyName + " is not set");
		}

		try {
			HttpRequest req = HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(15))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request.toMap())))
				.build();

			for (int attempt = 1; ; attempt++) {
				HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
				int status = res.statusCode();

				if (status == 200) {
					return MAPPER.readValue(res.body(), SystemOne.Response.class);
				}

				boolean retryable = status == 429 || status == 529;
				if (!retryable || attempt >= MAX_ATTEMPTS) {
					throw new SystemOneException("HTTP " + status + ": " + res.body());
				}

				Thread.sleep(500L << (attempt - 1)); // 0.5s, 1s, ...
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SystemOneException("Request interrupted", e);
		} catch (IOException e) {
			throw new SystemOneException("Request failed: " + e.getMessage(), e);
		}
	}
}
