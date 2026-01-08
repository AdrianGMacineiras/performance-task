package infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDetailRepositoryImplTest {

	@Mock
	private RestTemplate restTemplate;
	@Mock
	private CircuitBreakerRegistry circuitBreakerRegistry;
	@Mock
	private Cache<String, ProductDetail> productCache;
	@Mock
	private Cache<String, List<String>> similarIdsCache;
	@Mock
	private ProductServiceProperties properties;
	@Mock
	private ProductServiceProperties.CircuitBreaker circuitBreakerProperties;
	@Mock
	private CircuitBreaker circuitBreaker;

	private ProductDetailRepositoryImpl repository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Setup properties mocks
		when(this.properties.getCircuitBreaker()).thenReturn(this.circuitBreakerProperties);
		when(this.circuitBreakerProperties.getName()).thenReturn("productDetailCB");
		when(this.properties.getBaseUrl()).thenReturn("http://localhost:3001");

		// Setup circuit breaker mocks
		when(this.circuitBreakerRegistry.circuitBreaker("productDetailCB")).thenReturn(this.circuitBreaker);

		this.repository = new ProductDetailRepositoryImpl(this.restTemplate, this.circuitBreakerRegistry,
				this.productCache, this.similarIdsCache, this.properties);
	}

	@Test
	@DisplayName("Should create repository instance successfully")
	void constructor_success() {
		assertNotNull(this.repository);
	}

	@Test
	@DisplayName("Should clear cache on initialization")
	void initCache_clearsCache() {
		// Act
		this.repository.initCache();

		// Assert
		verify(this.productCache).invalidateAll();
	}

	// ===== GET SIMILAR IDS TESTS =====

	@Test
	@DisplayName("Should retrieve similar product IDs successfully")
	void getSimilarIds_success() {
		// Arrange
		final String[] expectedIds = {"1", "2", "3"};
		final List<String> expectedList = Arrays.asList(expectedIds);

		// Mock cache to call the mapping function
		when(this.similarIdsCache.get(eq("0"), any())).thenAnswer(invocation -> {
			final Function<String, List<String>> mappingFunction = invocation.getArgument(1);
			return mappingFunction.apply("0");
		});

		when(this.circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
			// Execute the actual supplier to simulate circuit breaker passing through
			final var supplier = invocation.getArgument(0, java.util.function.Supplier.class);
			return supplier.get();
		});
		when(this.restTemplate.getForObject("http://localhost:3001/product/0/similarids", String[].class))
				.thenReturn(expectedIds);

		// Act
		final List<String> result = this.repository.getSimilarIds("0");

		// Assert
		assertNotNull(result);
		assertEquals(expectedList, result);
		verify(this.circuitBreaker).executeSupplier(any());
	}

	@Test
	@DisplayName("Should return empty list when no similar IDs found")
	void getSimilarIds_emptyResult() {
		// Arrange
		// Mock cache to call the mapping function
		when(this.similarIdsCache.get(eq("0"), any())).thenAnswer(invocation -> {
			final Function<String, List<String>> mappingFunction = invocation.getArgument(1);
			return mappingFunction.apply("0");
		});

		when(this.circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
			// Execute the actual supplier to simulate circuit breaker passing through
			final var supplier = invocation.getArgument(0, java.util.function.Supplier.class);
			return supplier.get();
		});
		when(this.restTemplate.getForObject("http://localhost:3001/product/0/similarids", String[].class))
				.thenReturn(null);

		// Act
		final List<String> result = this.repository.getSimilarIds("0");

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	@DisplayName("Should return empty list when getSimilarIds throws exception")
	void getSimilarIds_exception() {
		// Arrange
		// Mock cache to call the mapping function
		when(this.similarIdsCache.get(eq("0"), any())).thenAnswer(invocation -> {
			final Function<String, List<String>> mappingFunction = invocation.getArgument(1);
			return mappingFunction.apply("0");
		});

		when(this.circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Service error"));

		// Act
		final List<String> result = this.repository.getSimilarIds("0");

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	// ===== GET PRODUCT DETAIL TESTS =====

	@Test
	@DisplayName("Should retrieve product detail successfully")
	void getProductDetail_success() {
		// Arrange
		final ProductDetail expected = new ProductDetail("1", "Product 1", 10.0, true);
		when(this.productCache.get(eq("1"), any())).thenReturn(expected);

		// Act
		final ProductDetail result = this.repository.getProductDetail("1");

		// Assert
		assertNotNull(result);
		assertEquals(expected, result);
		verify(this.productCache).get(eq("1"), any());
	}

	@Test
	@DisplayName("Should return null when product not found")
	void getProductDetail_notFound() {
		// Arrange
		when(this.productCache.get(eq("999"), any())).thenReturn(null);

		// Act
		final ProductDetail result = this.repository.getProductDetail("999");

		// Assert
		assertNull(result);
	}

	@Test
	@DisplayName("Should handle RestClientException gracefully")
	void getProductDetail_restClientException() {
		// Arrange
		when(this.productCache.get(eq("1"), any())).thenAnswer(invocation -> {
			final Function<String, ProductDetail> loader = invocation.getArgument(1);
			return loader.apply("1"); // This will trigger the circuit breaker
		});
		when(this.circuitBreaker.executeSupplier(any())).thenThrow(new RestClientException("Service unavailable"));

		// Act
		final ProductDetail result = this.repository.getProductDetail("1");

		// Assert
		assertNull(result);
	}

	// ===== CIRCUIT BREAKER TESTS =====

	@Test
	@DisplayName("Should use circuit breaker for REST calls")
	void circuitBreaker_integration() {
		// Arrange
		final ProductDetail expected = new ProductDetail("1", "Product 1", 10.0, true);
		when(this.productCache.get(eq("1"), any())).thenAnswer(invocation -> {
			final Function<String, ProductDetail> loader = invocation.getArgument(1);
			return loader.apply("1");
		});
		when(this.circuitBreaker.executeSupplier(any())).thenReturn(expected);

		// Act
		final ProductDetail result = this.repository.getProductDetail("1");

		// Assert
		assertNotNull(result);
		assertEquals(expected, result);
		verify(this.circuitBreaker).executeSupplier(any());
	}
}
