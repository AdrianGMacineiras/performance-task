package com.sngular.adriangm.myapp.config;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sngular.adriangm.myapp.model.ProductDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Configuration
public class CacheConfig {

	@Value("${product-service.cache.maximum-size:1000}")
	private int cacheMaximumSize;

	@Value("${product-service.cache.expire-after-write:30m}")
	private Duration cacheExpireAfterWrite;

	@Value("${product-service.cache.expire-after-access:10m}")
	private Duration cacheExpireAfterAccess;

	@Bean("productDetailCache")
	public AsyncLoadingCache<String, ProductDetail> productDetailCache() {
		return Caffeine.newBuilder()
				.maximumSize(this.cacheMaximumSize)
				.expireAfterWrite(this.cacheExpireAfterWrite)
				.expireAfterAccess(this.cacheExpireAfterAccess)
				.recordStats()
				.executor(ForkJoinPool.commonPool())
				.buildAsync(this::asyncLoader);
	}

	private CompletableFuture<ProductDetail> asyncLoader(String key, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}
}
