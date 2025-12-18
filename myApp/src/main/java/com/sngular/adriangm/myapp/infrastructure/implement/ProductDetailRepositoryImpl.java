package com.sngular.adriangm.myapp.infrastructure.implement;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

	private final WebClient webClient;
	private static final String BASE_URL = "http://localhost:3001";
	private final Map<String, ProductDetail> productCache = new ConcurrentHashMap<>();
	private final CircuitBreaker circuitBreaker = CircuitBreakerRegistry.ofDefaults().circuitBreaker("productDetailCB");

	@Override
	public Flux<String> getSimilarIds(String productId) {
		return this.webClient.get().uri(BASE_URL + "/product/" + productId + "/similarids").retrieve()
				.bodyToFlux(String.class).transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker));
	}

	@Override
	public Mono<ProductDetail> getProductDetail(String productId) {
		final ProductDetail cached = this.productCache.get(productId);
		if (cached != null) {
			return Mono.just(cached);
		}
		return this.webClient.get().uri(BASE_URL + "/product/" + productId).retrieve().bodyToMono(ProductDetail.class)
				.doOnNext(product -> this.productCache.put(productId, product))
				.transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker)).onErrorResume(e -> Mono.empty());
	}
}
