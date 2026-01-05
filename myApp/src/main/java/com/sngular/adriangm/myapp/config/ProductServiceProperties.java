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

	// Main properties
	private String baseUrl;
	private Duration timeout;
	private int concurrency;

	private ServiceConfig service = new ServiceConfig();
	private RestTemplate restTemplate = new RestTemplate();
	private WebClient webclient = new WebClient();
	private Cache cache = new Cache();
	private CircuitBreaker circuitBreaker = new CircuitBreaker();

	@Setter
	@Getter
	public static class ServiceConfig {
		private Duration similarProductsTimeout = Duration.ofSeconds(2);
		private int concurrencyLevel = 8;
	}

	@Setter
	@Getter
	public static class RestTemplate {
		private Duration connectTimeout = Duration.ofSeconds(2);
		private Duration readTimeout = Duration.ofSeconds(6);
		private int maxConnections = 100;
		private int maxConnectionsPerRoute = 50;
	}

	@Setter
	@Getter
	public static class WebClient {
		private String baseUrl;
		private Duration timeout = Duration.ofSeconds(8);
		private Duration responseTimeout = Duration.ofSeconds(6);
		private Duration connectionTimeout = Duration.ofSeconds(2);
		private Duration readTimeout = Duration.ofSeconds(6);
		private Duration writeTimeout = Duration.ofSeconds(6);
		private int maxConnections = 100;
		private Duration maxIdleTime = Duration.ofSeconds(20);
		private Duration maxLifeTime = Duration.ofMinutes(2);
		private Duration pendingAcquireTimeout = Duration.ofSeconds(3);
		private String maxInMemorySize = "16MB";
		private String connectionPoolName = "custom-pool";
	}

	@Setter
	@Getter
	public static class Cache {
		private int maximumSize = 500;
		private Duration expireAfterWrite = Duration.ofMinutes(15);
		private Duration expireAfterAccess = Duration.ofMinutes(5);
		private Duration refreshAfterWrite = Duration.ofMinutes(10);
		private boolean recordStats = true;
	}

	@Setter
	@Getter
	public static class CircuitBreaker {
		private String name = "productDetailCB";
		private int failureRateThreshold = 60;
		private Duration waitDurationInOpenState = Duration.ofSeconds(10);
		private int slidingWindowSize = 50;
		private int minimumNumberOfCalls = 5;
		private int slowCallRateThreshold = 50;
		private Duration slowCallDurationThreshold = Duration.ofSeconds(3);
	}
}
