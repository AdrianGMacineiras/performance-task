package com.sngular.adriangm.myapp.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sngular.adriangm.myapp.model.ProductDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {

	private final ProductServiceProperties properties;

	@Bean("productDetailCache")
	public Cache<String, ProductDetail> productDetailCache() {
		final Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
				.maximumSize(this.properties.getCache().getMaximumSize())
				.expireAfterWrite(this.properties.getCache().getExpireAfterWrite())
				.expireAfterAccess(this.properties.getCache().getExpireAfterAccess());

		if (this.properties.getCache().isRecordStats()) {
			caffeineBuilder.recordStats();
		}

		return caffeineBuilder.build();
	}

	@Bean("similarIdsCache")
	public Cache<String, List<String>> similarIdsCache() {
		final Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
				.maximumSize(this.properties.getCache().getMaximumSize())
				.expireAfterWrite(this.properties.getCache().getExpireAfterWrite())
				.expireAfterAccess(this.properties.getCache().getExpireAfterAccess());

		if (this.properties.getCache().isRecordStats()) {
			caffeineBuilder.recordStats();
		}

		return caffeineBuilder.build();
	}
}
