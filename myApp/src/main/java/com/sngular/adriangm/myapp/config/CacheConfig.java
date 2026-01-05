package com.sngular.adriangm.myapp.config;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sngular.adriangm.myapp.model.ProductDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Configuration
public class CacheConfig {

	private final ProductServiceProperties properties;

	public CacheConfig(ProductServiceProperties properties) {
		this.properties = properties;
	}

	@Bean("productDetailCache")
	public AsyncLoadingCache<String, ProductDetail> productDetailCache() {
		final Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
				.maximumSize(this.properties.getCache().getMaximumSize())
				.expireAfterWrite(this.properties.getCache().getExpireAfterWrite())
				.expireAfterAccess(this.properties.getCache().getExpireAfterAccess())
				.refreshAfterWrite(this.properties.getCache().getRefreshAfterWrite())
				.executor(ForkJoinPool.commonPool());

		if (this.properties.getCache().isRecordStats()) {
			caffeineBuilder.recordStats();
		}

		return caffeineBuilder.buildAsync(this::asyncLoader);
	}

	private CompletableFuture<ProductDetail> asyncLoader(String key, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}
}
