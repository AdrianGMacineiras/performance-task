package com.sngular.adriangm.myapp.infrastructure.implement;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

	private final WebClient webClient;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private static final String BASE_URL = "http://localhost:3001";
	private final AsyncLoadingCache<String, ProductDetail> productCache = Caffeine.newBuilder().maximumSize(1000)
			.expireAfterWrite(Duration.ofMinutes(10))
			.buildAsync((productId, executor) -> this.fetchProductDetail(productId));

	private CircuitBreaker getCircuitBreaker() {
		return this.circuitBreakerRegistry.circuitBreaker("productDetailCB");
	}

	private CompletableFuture<ProductDetail> fetchProductDetail(String productId) {
		return this.webClient.get().uri(BASE_URL + "/product/" + productId).retrieve().bodyToMono(ProductDetail.class)
				.transformDeferred(CircuitBreakerOperator.of(this.getCircuitBreaker())).onErrorResume(e -> Mono.empty())
				.toFuture();
	}

	@Override
	public Flux<String> getSimilarIds(String productId) {
		return this.webClient.get().uri(BASE_URL + "/product/" + productId + "/similarids").retrieve()
				.bodyToMono(String[].class).flatMapMany(Flux::fromArray)
				.transformDeferred(CircuitBreakerOperator.of(this.getCircuitBreaker()));
	}

	@Override
	public Mono<ProductDetail> getProductDetail(String productId) {
		return Mono.fromFuture(this.productCache.get(productId));
	}
}
