package service;

import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.implement.SimilarProductsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class SimilarProductsServiceImplTest {

	private ProductDetailRepository productDetailRepository;
	private SimilarProductsServiceImpl similarProductsService;

	@BeforeEach
	void setUp() {
		this.productDetailRepository = Mockito.mock(ProductDetailRepository.class);
		this.similarProductsService = new SimilarProductsServiceImpl(this.productDetailRepository);
	}

	@Test
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
	void getSimilarProducts_emptyWhenNoSimilarIds() {
		when(this.productDetailRepository.getSimilarIds(anyString())).thenReturn(Flux.empty());
		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	@Test
	void getSimilarProducts_someProductsFail() {
		final List<String> similarIds = Arrays.asList("1", "2", "3");
		final ProductDetail product1 = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetail product3 = new ProductDetail("3", "Product 3", 30.0, true);

		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.just(product1));
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.empty()); // Simula fallo
		when(this.productDetailRepository.getProductDetail("3")).thenReturn(Mono.just(product3));

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).expectNext(product1)
				.expectNext(product3).verifyComplete();
	}

	@Test
	void getSimilarProducts_allProductsFail() {
		final List<String> similarIds = Arrays.asList("1", "2");
		when(this.productDetailRepository.getSimilarIds("0")).thenReturn(Flux.fromIterable(similarIds));
		when(this.productDetailRepository.getProductDetail("1")).thenReturn(Mono.empty());
		when(this.productDetailRepository.getProductDetail("2")).thenReturn(Mono.empty());

		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	@Test
	void getSimilarProducts_repositoryThrowsException() {
		when(this.productDetailRepository.getSimilarIds("0"))
				.thenReturn(Flux.error(new RuntimeException("Repo error")));
		StepVerifier.create(this.similarProductsService.getSimilarProducts("0")).verifyComplete();
	}

	@Test
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
}
