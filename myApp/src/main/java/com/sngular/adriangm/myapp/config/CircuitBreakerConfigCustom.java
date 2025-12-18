package com.sngular.adriangm.myapp.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfigCustom {
	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		final CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.ofDefaults();
		final CircuitBreakerConfig customConfig = CircuitBreakerConfig.custom().failureRateThreshold(50)
				.waitDurationInOpenState(Duration.ofSeconds(3)).slidingWindowSize(20).build();
		final CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
		registry.circuitBreaker("productDetailCB", customConfig);
		return registry;
	}
}
