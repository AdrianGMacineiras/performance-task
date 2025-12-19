package com.sngular.adriangm.myapp.infrastructure.implement;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Repository
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

	private final WebClient webClient;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final AsyncLoadingCache<String, ProductDetail> productCache;

	@Value("${product-service.base-url:http://localhost:3001}")
	private String baseUrl;

	public ProductDetailRepositoryImpl(WebClient webClient, CircuitBreakerRegistry circuitBreakerRegistry,
			@Qualifier("productDetailCache") AsyncLoadingCache<String, ProductDetail> productCache) {
		this.webClient = webClient;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.productCache = productCache;
	}

	@PostConstruct
	public void initCache() {
		// Reconfigura el cache con el loader real
		this.productCache.asMap().clear();
	}

	private CircuitBreaker getCircuitBreaker() {
		return this.circuitBreakerRegistry.circuitBreaker("productDetailCB");
	}

	private CompletableFuture<ProductDetail> fetchProductDetail(String productId) {
		return this.webClient.get().uri(this.baseUrl + "/product/" + productId).retrieve()
				.bodyToMono(ProductDetail.class).transformDeferred(CircuitBreakerOperator.of(this.getCircuitBreaker()))
				.onErrorResume(e -> Mono.empty()).toFuture();
	}

	@Override
	public Flux<String> getSimilarIds(String productId) {
		return this.webClient.get().uri(this.baseUrl + "/product/" + productId + "/similarids").retrieve()
				.bodyToMono(String[].class).flatMapMany(Flux::fromArray)
				.transformDeferred(CircuitBreakerOperator.of(this.getCircuitBreaker()))
				.onErrorResume(e -> Flux.empty());
	}

	@Override
	public Mono<ProductDetail> getProductDetail(String productId) {

		// Usa el cache pero con el loader personalizado
		return Mono.fromFuture(this.productCache.asMap().computeIfAbsent(productId, this::fetchProductDetail));
	}
}
