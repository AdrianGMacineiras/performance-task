package com.sngular.adriangm.myapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "product-service")
public class ProductServiceProperties {

	// Main getters and setters
	private String baseUrl;
	private Duration timeout;
	private int concurrency;

	private RestTemplate restTemplate = new RestTemplate();
	private WebClient webclient = new WebClient();
	private Cache cache = new Cache();
	private CircuitBreaker circuitBreaker = new CircuitBreaker();

	@Setter
	@Getter
	public static class RestTemplate {
		private Duration connectTimeout = Duration.ofSeconds(5);
		private Duration readTimeout = Duration.ofSeconds(8);
		private int maxConnections = 200;
		private int maxConnectionsPerRoute = 200;

	}

	@Setter
	@Getter
	public static class WebClient {
		private String baseUrl;
		private Duration timeout = Duration.ofSeconds(8);
		private Duration responseTimeout = Duration.ofSeconds(8);
		private Duration connectionTimeout = Duration.ofSeconds(3);
		private Duration readTimeout = Duration.ofSeconds(8);
		private Duration writeTimeout = Duration.ofSeconds(8);
		private int maxConnections = 1000;
		private Duration maxIdleTime = Duration.ofSeconds(30);
		private Duration maxLifeTime = Duration.ofMinutes(5);
		private Duration pendingAcquireTimeout = Duration.ofSeconds(5);
		private String maxInMemorySize = "16MB";

	}

	@Setter
	@Getter
	public static class Cache {
		private int maximumSize = 1000;
		private Duration expireAfterWrite = Duration.ofMinutes(30);
		private Duration expireAfterAccess = Duration.ofMinutes(10);
		private boolean recordStats = true;

	}

	@Setter
	@Getter
	public static class CircuitBreaker {
		private int failureRateThreshold = 50;
		private Duration waitDurationInOpenState = Duration.ofSeconds(10);
		private int slidingWindowSize = 100;
		private int minimumNumberOfCalls = 20;
		private int slowCallRateThreshold = 50;
		private Duration slowCallDurationThreshold = Duration.ofSeconds(5);

	}
}
