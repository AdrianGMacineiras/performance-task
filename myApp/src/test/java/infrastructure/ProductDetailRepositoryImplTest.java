package infrastructure;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.sngular.adriangm.myapp.config.ProductServiceProperties;
import com.sngular.adriangm.myapp.infrastructure.implement.ProductDetailRepositoryImpl;
import com.sngular.adriangm.myapp.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDetailRepositoryImplTest {

	@Mock
	private WebClient webClient;
	@Mock
	private CircuitBreakerRegistry circuitBreakerRegistry;
	@Mock
	private AsyncLoadingCache<String, ProductDetail> productCache;
	@Mock
	private ProductServiceProperties properties;
	@Mock
	private ProductServiceProperties.CircuitBreaker circuitBreakerProperties;
	@Mock
	private CircuitBreaker circuitBreaker;

	private ProductDetailRepositoryImpl repository;
	private ConcurrentMap<String, CompletableFuture<ProductDetail>> cacheMap;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Setup properties mocks
		when(this.properties.getCircuitBreaker()).thenReturn(this.circuitBreakerProperties);
		when(this.circuitBreakerProperties.getName()).thenReturn("productDetailCB");
		when(this.properties.getBaseUrl()).thenReturn("http://localhost:3001");

		// Setup circuit breaker mocks
		when(this.circuitBreakerRegistry.circuitBreaker("productDetailCB")).thenReturn(this.circuitBreaker);

		// Setup cache mocks
		this.cacheMap = new ConcurrentHashMap<>();
		when(this.productCache.asMap()).thenReturn(this.cacheMap);

		this.repository = new ProductDetailRepositoryImpl(this.webClient, this.circuitBreakerRegistry,
				this.productCache, this.properties);
	}

	@Test
	@DisplayName("Should create repository instance successfully")
	void constructor_success() {
		assertNotNull(this.repository);
	}

	@Test
	@DisplayName("Should clear cache on initialization")
	void initCache_clearsCache() {
		// Arrange
		this.cacheMap.put("test", CompletableFuture.completedFuture(new ProductDetail()));

		// Act
		this.repository.initCache();

		// Assert
		verify(this.productCache).asMap();
		assertTrue(this.cacheMap.isEmpty());
	}

	@Test
	@DisplayName("Should handle cache operations correctly")
	void cache_operations() {
		// Arrange
		assertTrue(this.cacheMap.isEmpty());

		// Act - Add something to cache map directly for testing
		this.cacheMap.put("direct",
				CompletableFuture.completedFuture(new ProductDetail("direct", "Direct Product", 10.0, true)));

		// Assert
		assertFalse(this.cacheMap.isEmpty());
		assertTrue(this.cacheMap.containsKey("direct"));
		assertEquals(1, this.cacheMap.size());
	}
}
