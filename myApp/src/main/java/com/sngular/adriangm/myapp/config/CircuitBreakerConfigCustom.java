package com.sngular.adriangm.myapp.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfigCustom {

	private final ProductServiceProperties properties;

	public CircuitBreakerConfigCustom(ProductServiceProperties properties) {
		this.properties = properties;
	}

	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		final CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.ofDefaults();
		final CircuitBreakerConfig customConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(this.properties.getCircuitBreaker().getFailureRateThreshold())
				.waitDurationInOpenState(this.properties.getCircuitBreaker().getWaitDurationInOpenState())
				.slidingWindowSize(this.properties.getCircuitBreaker().getSlidingWindowSize())
				.minimumNumberOfCalls(this.properties.getCircuitBreaker().getMinimumNumberOfCalls())
				.slowCallRateThreshold(this.properties.getCircuitBreaker().getSlowCallRateThreshold())
				.slowCallDurationThreshold(this.properties.getCircuitBreaker().getSlowCallDurationThreshold()).build();

		final CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
		registry.circuitBreaker(this.properties.getCircuitBreaker().getName(), customConfig);
		return registry;
	}
}
