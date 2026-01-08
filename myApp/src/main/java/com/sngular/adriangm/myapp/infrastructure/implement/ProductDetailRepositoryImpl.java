package com.sngular.adriangm.myapp.infrastructure.implement;

import com.github.benmanes.caffeine.cache.Cache;
import com.sngular.adriangm.myapp.config.ProductServiceProperties;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

	private final RestTemplate restTemplate;
	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final Cache<String, ProductDetail> productCache;
	private final Cache<String, List<String>> similarIdsCache;
	private final ProductServiceProperties properties;

	public ProductDetailRepositoryImpl(RestTemplate restTemplate, CircuitBreakerRegistry circuitBreakerRegistry,
			@Qualifier("productDetailCache") Cache<String, ProductDetail> productCache,
			@Qualifier("similarIdsCache") Cache<String, List<String>> similarIdsCache,
			ProductServiceProperties properties) {
		this.restTemplate = restTemplate;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.productCache = productCache;
		this.similarIdsCache = similarIdsCache;
		this.properties = properties;
	}

	@PostConstruct
	public void initCache() {
		this.productCache.invalidateAll();
		this.similarIdsCache.invalidateAll();
	}

	private CircuitBreaker getCircuitBreaker() {
		return this.circuitBreakerRegistry.circuitBreaker(this.properties.getCircuitBreaker().getName());
	}

	private ProductDetail fetchProductDetail(String productId) {
		try {
			return this.getCircuitBreaker().executeSupplier(() -> this.restTemplate
					.getForObject(this.properties.getBaseUrl() + "/product/" + productId, ProductDetail.class));
		} catch (final Exception e) {
			return null;
		}
	}

	private List<String> fetchSimilarIds(String productId) {
		try {
			return this.getCircuitBreaker().executeSupplier(() -> {
				final String[] similarIds = this.restTemplate.getForObject(
						this.properties.getBaseUrl() + "/product/" + productId + "/similarids", String[].class);
				return similarIds != null ? Arrays.asList(similarIds) : Collections.emptyList();
			});
		} catch (final Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> getSimilarIds(String productId) {
		return this.similarIdsCache.get(productId, this::fetchSimilarIds);
	}

	@Override
	public ProductDetail getProductDetail(String productId) {
		return this.productCache.get(productId, this::fetchProductDetail);
	}
}
