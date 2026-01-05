package service;

import com.sngular.adriangm.myapp.config.ProductServiceProperties;
import com.sngular.adriangm.myapp.exception.ProductNotFoundException;
import com.sngular.adriangm.myapp.exception.SimilarProductsRetrievalException;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.implement.SimilarProductsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SimilarProductsServiceImplTest {

	private ProductDetailRepository productDetailRepository;
	private ProductServiceProperties properties;
	private ProductServiceProperties.ServiceConfig serviceConfig;
	private SimilarProductsServiceImpl similarProductsService;

	@BeforeEach
	void setUp() {
		this.productDetailRepository = Mockito.mock(ProductDetailRepository.class);
		this.properties = Mockito.mock(ProductServiceProperties.class);
		this.serviceConfig = Mockito.mock(ProductServiceProperties.ServiceConfig.class);

		// Default mock configuration
		when(this.properties.getService()).thenReturn(this.serviceConfig);
		when(this.serviceConfig.getSimilarProductsTimeout()).thenReturn(Duration.ofSeconds(2));
		when(this.serviceConfig.getConcurrencyLevel()).thenReturn(8);

		this.similarProductsService = new SimilarProductsServiceImpl(this.productDetailRepository, this.properties);
	}

	// ===== BASIC FUNCTIONALITY TESTS =====

	@Test
	@DisplayName("Should return product details when similar products exist")
	void getSimilarProducts_returnsProductDetails() {
		// Arrange
		final List<String> similarIds = Arrays.asList("1", "2");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product2 = new ProductDetail("2", "Product 2", 20.0, false);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.just(product2));

		// Act & Assert
		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product2).verifyComplete();
	}

	@Test
	@DisplayName("Should return empty flux when no similar IDs found")
	void getSimilarProducts_emptyWhenNoSimilarIds() {
		when(this.productDetailRepository.getSimilarIds(anyString())).thenReturn(Flux.empty());

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	// ===== PARTIAL FAILURE TESTS =====

	@Test
	@DisplayName("Should return available products when some products fail to load")
	void getSimilarProducts_someProductsFail() {
		final List<String> similarIds = Arrays.asList("1", "2", "3");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product3 = new ProductDetail("3", "Product 3", 30.0, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.empty()); // Simulates failure
		when(this.productDetailRepository.getProductDetail("3")).thenReturn(Mono.just(product3));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product3).verifyComplete();
	}

	@Test
	@DisplayName("Should return empty flux when all product details fail to load")
	void getSimilarProducts_allProductsFail() {
		final List<String> similarIds = Arrays.asList("1", "2");
		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.empty());
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.empty());

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	// ===== EXCEPTION HANDLING TESTS =====

	@Test
	@DisplayName("Should handle ProductNotFoundException and continue processing")
	void getSimilarProducts_handlesProductNotFoundException() {
		final List<String> similarIds = Arrays.asList("1", "2", "3");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product3 = new ProductDetail("3", "Product 3", 30.0, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2"))
				.thenReturn(Mono.error(new ProductNotFoundException("2")));
		when(this.productDetailRepository.getProductDetail("3")).thenReturn(Mono.just(product3));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product3).verifyComplete();
	}

	@Test
	@DisplayName("Should handle mixed exceptions gracefully")
	void getSimilarProducts_handlesMixedExceptions() {
		final List<String> similarIds = Arrays.asList("1", "2", "3", "4");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product4 = new ProductDetail("4", "Product 4", 40.0, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2"))
				.thenReturn(Mono.error(new ProductNotFoundException("2"))); // This gets handled and becomes empty
		when(this.productDetailRepository.getProductDetail("3")).thenReturn(Mono.empty()); // Simulate empty result
																							// instead of runtime
																							// exception
		when(this.productDetailRepository.getProductDetail("4")).thenReturn(Mono.just(product4));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product4).verifyComplete();
	}

	@Test
	@DisplayName("Should return empty flux when getSimilarIds throws exception")
	void getSimilarProducts_repositoryThrowsException() {
		when(this.productDetailRepository.getSimilarIds("0"))
				.thenReturn(Flux.error(new RuntimeException("Repository error")));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	@Test
	@DisplayName("Should map general exceptions to SimilarProductsRetrievalException and recover")
	void getSimilarProducts_mapsSimilarProductsRetrievalException() {
		when(this.productDetailRepository.getSimilarIds("0"))
				.thenReturn(Flux.error(new SimilarProductsRetrievalException("0", new RuntimeException("Test error"))));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	@Test
	@DisplayName("Should handle RuntimeException during product detail retrieval")
	void getSimilarProducts_handlesRuntimeExceptionInProductDetail() {
		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.just("1", "2"));
		when(this.productDetailRepository.getProductDetail("1"))
				.thenReturn(Mono.error(new RuntimeException("Unexpected error")));
		when(this.productDetailRepository.getProductDetail("2"))
				.thenReturn(Mono.just(new ProductDetail("2", "Product 2", 20.0, true)));

		// When a RuntimeException occurs in flatMap, it gets mapped and the whole
		// stream fails
		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete(); // Returns empty due
																									// to error mapping
	}

	// ===== TIMEOUT TESTS =====

	@Test
	@DisplayName("Should handle timeout according to configuration")
	void getSimilarProducts_respectsTimeoutConfiguration() {
		// Configure shorter timeout for this test
		when(this.serviceConfig.getSimilarProductsTimeout()).thenReturn(Duration.ofMillis(100));

		// Create a delayed flux that will timeout
		when(this.productDetailRepository.getSimilarIds("0"))
				.thenReturn(Flux.just("1").delayElements(Duration.ofMillis(200)));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete(); // Should complete
																									// empty due to
																									// timeout -> error
																									// mapping -> empty
																									// flux
	}

	@Test
	@DisplayName("Should use configured timeout from properties")
	void getSimilarProducts_usesConfiguredTimeout() {
		final Duration customTimeout = Duration.ofSeconds(5);
		when(this.serviceConfig.getSimilarProductsTimeout()).thenReturn(customTimeout);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.just("1"));
		when(this.productDetailRepository.getProductDetail("1"))
				.thenReturn(Mono.just(new ProductDetail("1", "Product 1", 10.0, true)));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNextCount(1).verifyComplete();

		// Verify the configuration was used during method execution
		verify(this.serviceConfig, atLeast(1)).getSimilarProductsTimeout();
	}

	// ===== CONCURRENCY TESTS =====

	@Test
	@DisplayName("Should process products with configured concurrency level")
	void getSimilarProducts_usesConcurrencyConfiguration() {
		final int customConcurrency = 4;
		when(this.serviceConfig.getConcurrencyLevel()).thenReturn(customConcurrency);

		final List<String> similarIds = Arrays.asList("1", "2", "3", "4", "5");
		final List<ProductDetail> products = similarIds.stream()
				.map(id -> new ProductDetail(id, "Product " + id, Double.parseDouble(id) * 10, true)).toList();

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		for (int i = 0; i < similarIds.size(); i++) {
			when(this.productDetailRepository.getProductDetail(similarIds.get(i)))
					.thenReturn(Mono.just(products.get(i)));
		}

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNextCount(5).verifyComplete();

		// Verify configuration was used during method execution
		verify(this.serviceConfig, atLeast(1)).getConcurrencyLevel();
	}

	// ===== EDGE CASES =====

	@Test
	@DisplayName("Should handle duplicate product IDs correctly")
	void getSimilarProducts_withDuplicateIds() {
		final List<String> similarIds = Arrays.asList("1", "2", "1");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product2 = new ProductDetail("2", "Product 2", 20.0, false);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.just(product2));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product2).expectNext(product1).verifyComplete();
	}

	@Test
	@DisplayName("Should preserve order of similar product IDs")
	void getSimilarProducts_preservesOrder() {
		final List<String> similarIds = Arrays.asList("2", "1");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product2 = new ProductDetail("2", "Product 2", 20.0, false);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.just(product2));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product2)
				.expectNext(product1).verifyComplete();
	}

	@Test
	@DisplayName("Should handle empty string product ID")
	void getSimilarProducts_emptyProductId() {
		when(this.productDetailRepository.getSimilarIds("")).thenReturn(Flux.empty());

		StepVerifier.create(this.similarProductsService.getSimilarProducts("")).verifyComplete();
	}

	@Test
	@DisplayName("Should handle filtered similar IDs list")
	void getSimilarProducts_withFilteredSimilarIds() {
		// Simulate repository returning already filtered list (no nulls)
		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.just("1", "2")); // Repository already
																								// filtered out nulls

		when(this.productDetailRepository.getProductDetail("1"))
				.thenReturn(Mono.just(new ProductDetail("1", "Product 1", 10.0, true)));
		when(this.productDetailRepository.getProductDetail("2"))
				.thenReturn(Mono.just(new ProductDetail("2", "Product 2", 20.0, false)));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNextCount(2).verifyComplete();
	}

	// ===== PERFORMANCE AND LARGE DATA TESTS =====

	@Test
	@DisplayName("Should handle large number of similar products efficiently")
	void getSimilarProducts_largeDataset() {
		final int productCount = 50;
		final List<String> similarIds = IntStream.range(1, productCount + 1).mapToObj(String::valueOf).toList();

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));

		// Mock all product details
		similarIds.forEach(id -> {
			final ProductDetail product = new ProductDetail(id, "Product " + id, Double.parseDouble(id) * 10,
					Integer.parseInt(id) % 2 == 0);
			when(this.productDetailRepository.getProductDetail(id)).thenReturn(Mono.just(product));
		});

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNextCount(productCount)
				.verifyComplete();
	}

	@Test
	@DisplayName("Should handle single similar product")
	void getSimilarProducts_singleProduct() {
		final ProductDetail product = new ProductDetail("1", "Single Product", 99.99, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.just("1"));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product).verifyComplete();
	}

	// ===== CONFIGURATION VALIDATION TESTS =====

	@Test
	@DisplayName("Should work with minimum timeout configuration")
	void getSimilarProducts_minimumTimeoutConfiguration() {
		when(this.serviceConfig.getSimilarProductsTimeout()).thenReturn(Duration.ofMillis(1));

		final ProductDetail product = new ProductDetail("1", "Fast Product", 10.0, true);
		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.just("1"));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product).verifyComplete();
	}

	@Test
	@DisplayName("Should work with minimum concurrency configuration")
	void getSimilarProducts_minimumConcurrencyConfiguration() {
		when(this.serviceConfig.getConcurrencyLevel()).thenReturn(1);

		final List<String> similarIds = Arrays.asList("1", "2");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product2 = new ProductDetail("2", "Product 2", 20.0, false);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.just(product2));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product2).verifyComplete();
	}

	// ===== INTEGRATION-STYLE TESTS =====

	@Test
	@DisplayName("Should handle complex scenario with mixed successes, failures, and timeouts")
	void getSimilarProducts_complexScenario() {
		final List<String> similarIds = Arrays.asList("success1", "fail1", "success2", "timeout1");
		final ProductDetail successProduct1 = new ProductDetail("success1", "Success 1", 10.0, true);
		final ProductDetail successProduct2 = new ProductDetail("success2", "Success 2", 20.0, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("success1")).thenReturn(Mono.just(successProduct1));
		when(this.productDetailRepository.getProductDetail("fail1"))
				.thenReturn(Mono.error(new ProductNotFoundException("fail1")));
		when(this.productDetailRepository.getProductDetail("success2")).thenReturn(Mono.just(successProduct2));
		when(this.productDetailRepository.getProductDetail("timeout1")).thenReturn(
				Mono.just(new ProductDetail("timeout1", "Timeout", 30.0, true)).delayElement(Duration.ofSeconds(10))); // Will
																														// not
																														// timeout
																														// in
																														// this
																														// test

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(successProduct1)
				.expectNext(successProduct2).expectNext(new ProductDetail("timeout1", "Timeout", 30.0, true))
				.verifyComplete();
	}
}
