package controller;

import com.sngular.adriangm.myapp.controller.SimilarProductsController;
import com.sngular.adriangm.myapp.controller.mapper.ProductDetailMapper;
import com.sngular.adriangm.myapp.dto.ProductDetailDTO;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimilarProductsControllerTest {

	@Mock
	private ProductDetailMapper productDetailMapper;
	@Mock
	private SimilarProductsService similarProductsService;
	@Mock
	private ServerWebExchange exchange;

	@InjectMocks
	private SimilarProductsController controller;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.controller = new SimilarProductsController(this.productDetailMapper, this.similarProductsService);
	}

	// ===== BASIC FUNCTIONALITY TESTS =====

	@Test
	@DisplayName("Should return products with OK status when similar products exist")
	void getProductSimilar_returnsProducts() {
		// Arrange
		final ProductDetail product = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetailDTO dto = this.createProductDetailDTO("1", "Product 1", 10.0, true);

		when(this.similarProductsService.getSimilarProducts("1")).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("1",
				this.exchange);

		// Assert
		StepVerifier.create(result).assertNext(responseEntity -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertNotNull(responseEntity.getBody());
		}).verifyComplete();

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectNext(dto).verifyComplete();

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("1");
		verify(this.productDetailMapper).toApiModel(product);
	}

	@Test
	@DisplayName("Should return empty flux with OK status when no similar products found")
	void getProductSimilar_returnsEmpty() {
		// Arrange
		when(this.similarProductsService.getSimilarProducts("2")).thenReturn(Flux.empty());

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("2",
				this.exchange);

		// Assert
		StepVerifier.create(result).assertNext(responseEntity -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertNotNull(responseEntity.getBody());
		}).verifyComplete();

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).verifyComplete();

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("2");
		verifyNoInteractions(this.productDetailMapper);
	}

	// ===== MULTIPLE PRODUCTS TESTS =====

	@Test
	@DisplayName("Should return multiple products correctly")
	void getProductSimilar_returnsMultipleProducts() {
		// Arrange
		final List<ProductDetail> products = Arrays.asList(new ProductDetail("1", "Product 1", 10.0, true),
				new ProductDetail("2", "Product 2", 20.0, false), new ProductDetail("3", "Product 3", 30.0, true));

		final List<ProductDetailDTO> dtos = Arrays.asList(this.createProductDetailDTO("1", "Product 1", 10.0, true),
				this.createProductDetailDTO("2", "Product 2", 20.0, false),
				this.createProductDetailDTO("3", "Product 3", 30.0, true));

		when(this.similarProductsService.getSimilarProducts("0")).thenReturn(Flux.fromIterable(products));
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));
		when(this.productDetailMapper.toApiModel(products.get(2))).thenReturn(dtos.get(2));

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("0",
				this.exchange);

		// Assert
		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectNext(dtos.get(0)).expectNext(dtos.get(1))
				.expectNext(dtos.get(2)).verifyComplete();

		// Verify all products were mapped
		verify(this.productDetailMapper, times(3)).toApiModel(any(ProductDetail.class));
	}

	@Test
	@DisplayName("Should preserve order of products")
	void getProductSimilar_preservesOrder() {
		// Arrange
		final List<ProductDetail> products = Arrays.asList(new ProductDetail("3", "Product 3", 30.0, true),
				new ProductDetail("1", "Product 1", 10.0, true), new ProductDetail("2", "Product 2", 20.0, false));

		final List<ProductDetailDTO> dtos = Arrays.asList(this.createProductDetailDTO("3", "Product 3", 30.0, true),
				this.createProductDetailDTO("1", "Product 1", 10.0, true),
				this.createProductDetailDTO("2", "Product 2", 20.0, false));

		when(this.similarProductsService.getSimilarProducts("0")).thenReturn(Flux.fromIterable(products));
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));
		when(this.productDetailMapper.toApiModel(products.get(2))).thenReturn(dtos.get(2));

		// Act & Assert
		StepVerifier.create(this.controller.getProductSimilar("0", this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNext(dtos.get(0)) // 3 first
				.expectNext(dtos.get(1)) // 1 second
				.expectNext(dtos.get(2)) // 2 third
				.verifyComplete();
	}

	// ===== ERROR HANDLING TESTS =====

	@Test
	@DisplayName("Should propagate service errors")
	void getProductSimilar_serviceError() {
		// Arrange
		final RuntimeException serviceError = new RuntimeException("Service error");
		when(this.similarProductsService.getSimilarProducts("3")).thenReturn(Flux.error(serviceError));

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("3",
				this.exchange);

		// Assert
		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectError(RuntimeException.class).verify();

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("3");
		verifyNoInteractions(this.productDetailMapper);
	}

	@Test
	@DisplayName("Should handle mapper errors gracefully")
	void getProductSimilar_mapperError() {
		// Arrange
		final ProductDetail product = new ProductDetail("1", "Product 1", 10.0, true);
		final RuntimeException mapperError = new RuntimeException("Mapping error");

		when(this.similarProductsService.getSimilarProducts("1")).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenThrow(mapperError);

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("1",
				this.exchange);

		// Assert
		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectError(RuntimeException.class).verify();

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("1");
		verify(this.productDetailMapper).toApiModel(product);
	}

	// ===== PATH VARIABLE TESTS =====

	@Test
	@DisplayName("Should handle empty string product ID")
	void getProductSimilar_emptyProductId() {
		// Arrange
		when(this.similarProductsService.getSimilarProducts("")).thenReturn(Flux.empty());

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("",
				this.exchange);

		// Assert
		StepVerifier.create(result)
				.assertNext(responseEntity -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()))
				.verifyComplete();

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).verifyComplete();

		verify(this.similarProductsService).getSimilarProducts("");
	}

	@Test
	@DisplayName("Should handle special characters in product ID")
	void getProductSimilar_specialCharactersProductId() {
		// Arrange
		final String specialId = "product-123_abc@test.com";
		final ProductDetail product = new ProductDetail(specialId, "Special Product", 99.99, true);
		final ProductDetailDTO dto = this.createProductDetailDTO(specialId, "Special Product", 99.99, true);

		when(this.similarProductsService.getSimilarProducts(specialId)).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act & Assert
		StepVerifier.create(
				this.controller.getProductSimilar(specialId, this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNext(dto).verifyComplete();

		verify(this.similarProductsService).getSimilarProducts(specialId);
	}

	@Test
	@DisplayName("Should handle numeric product ID")
	void getProductSimilar_numericProductId() {
		// Arrange
		final String numericId = "12345";
		final ProductDetail product = new ProductDetail(numericId, "Numeric Product", 50.0, false);
		final ProductDetailDTO dto = this.createProductDetailDTO(numericId, "Numeric Product", 50.0, false);

		when(this.similarProductsService.getSimilarProducts(numericId)).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act & Assert
		StepVerifier.create(
				this.controller.getProductSimilar(numericId, this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNext(dto).verifyComplete();
	}

	// ===== EDGE CASES =====

	@Test
	@DisplayName("Should handle products with null/empty fields")
	void getProductSimilar_productsWithNullFields() {
		// Arrange
		final ProductDetail productWithNulls = new ProductDetail(null, null, 0.0, false);
		final ProductDetailDTO dtoWithNulls = this.createProductDetailDTO(null, null, 0.0, false);

		when(this.similarProductsService.getSimilarProducts("null-test")).thenReturn(Flux.just(productWithNulls));
		when(this.productDetailMapper.toApiModel(productWithNulls)).thenReturn(dtoWithNulls);

		// Act & Assert
		StepVerifier.create(
				this.controller.getProductSimilar("null-test", this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNext(dtoWithNulls).verifyComplete();
	}

	@Test
	@DisplayName("Should handle products with extreme values")
	void getProductSimilar_productsWithExtremeValues() {
		// Arrange
		final ProductDetail extremeProduct = new ProductDetail("extreme", "Very Long Product Name ".repeat(10),
				Double.MAX_VALUE, true);
		final ProductDetailDTO extremeDto = this.createProductDetailDTO("extreme", "Very Long Product Name ".repeat(10),
				Double.MAX_VALUE, true);

		when(this.similarProductsService.getSimilarProducts("extreme")).thenReturn(Flux.just(extremeProduct));
		when(this.productDetailMapper.toApiModel(extremeProduct)).thenReturn(extremeDto);

		// Act & Assert
		StepVerifier.create(
				this.controller.getProductSimilar("extreme", this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNext(extremeDto).verifyComplete();
	}

	// ===== PERFORMANCE TESTS =====

	@Test
	@DisplayName("Should handle large number of products efficiently")
	void getProductSimilar_largeDataset() {
		// Arrange
		final int productCount = 100;
		final List<ProductDetail> products = this.generateProducts(productCount);
		final List<ProductDetailDTO> dtos = this.generateProductDTOs(productCount);

		when(this.similarProductsService.getSimilarProducts("large-set")).thenReturn(Flux.fromIterable(products));

		// Mock mapper for all products
		for (int i = 0; i < productCount; i++) {
			when(this.productDetailMapper.toApiModel(products.get(i))).thenReturn(dtos.get(i));
		}

		// Act & Assert
		StepVerifier.create(
				this.controller.getProductSimilar("large-set", this.exchange).flatMapMany(ResponseEntity::getBody))
				.expectNextCount(productCount).verifyComplete();

		verify(this.productDetailMapper, times(productCount)).toApiModel(any(ProductDetail.class));
	}

	// ===== RESPONSE STRUCTURE TESTS =====

	@Test
	@DisplayName("Should return proper ResponseEntity structure")
	void getProductSimilar_responseEntityStructure() {
		// Arrange
		final ProductDetail product = new ProductDetail("structure-test", "Structure Product", 15.0, true);
		final ProductDetailDTO dto = this.createProductDetailDTO("structure-test", "Structure Product", 15.0, true);

		when(this.similarProductsService.getSimilarProducts("structure-test")).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("structure-test",
				this.exchange);

		// Assert
		StepVerifier.create(result).assertNext(responseEntity -> {
			// Verify response structure
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertNotNull(responseEntity.getBody());
			assertTrue(responseEntity.hasBody());

			// Verify headers (should be empty/default)
			assertNotNull(responseEntity.getHeaders());
		}).verifyComplete();
	}

	@Test
	@DisplayName("Should handle products with zero and negative prices")
	void getProductSimilar_productsWithSpecialPrices() {
		// Arrange
		final List<ProductDetail> products = Arrays.asList(new ProductDetail("zero", "Free Product", 0.0, true),
				new ProductDetail("negative", "Discounted Product", -10.0, false));

		final List<ProductDetailDTO> dtos = Arrays.asList(
				this.createProductDetailDTO("zero", "Free Product", 0.0, true),
				this.createProductDetailDTO("negative", "Discounted Product", -10.0, false));

		when(this.similarProductsService.getSimilarProducts("special-prices")).thenReturn(Flux.fromIterable(products));
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));

		// Act & Assert
		StepVerifier
				.create(this.controller.getProductSimilar("special-prices", this.exchange)
						.flatMapMany(ResponseEntity::getBody))
				.expectNext(dtos.get(0)).expectNext(dtos.get(1)).verifyComplete();
	}

	// ===== INTEGRATION-STYLE TESTS =====

	@Test
	@DisplayName("Should handle complete flow with service and mapper integration")
	void getProductSimilar_completeIntegration() {
		// Arrange
		final String productId = "integration-test";
		final ProductDetail product1 = new ProductDetail("int1", "Integration Product 1", 25.99, true);
		final ProductDetail product2 = new ProductDetail("int2", "Integration Product 2", 35.50, false);

		final ProductDetailDTO dto1 = this.createProductDetailDTO("int1", "Integration Product 1", 25.99, true);
		final ProductDetailDTO dto2 = this.createProductDetailDTO("int2", "Integration Product 2", 35.50, false);

		when(this.similarProductsService.getSimilarProducts(productId)).thenReturn(Flux.just(product1, product2));
		when(this.productDetailMapper.toApiModel(product1)).thenReturn(dto1);
		when(this.productDetailMapper.toApiModel(product2)).thenReturn(dto2);

		// Act
		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar(productId,
				this.exchange);

		// Assert - Verify complete response
		StepVerifier.create(result).assertNext(response -> {
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
		}).verifyComplete();

		// Assert - Verify body content
		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).assertNext(actualDto1 -> {
			assertEquals("int1", actualDto1.getId());
			assertEquals("Integration Product 1", actualDto1.getName());
			assertEquals(BigDecimal.valueOf(25.99), actualDto1.getPrice());
			assertTrue(actualDto1.getAvailability());
		}).assertNext(actualDto2 -> {
			assertEquals("int2", actualDto2.getId());
			assertEquals("Integration Product 2", actualDto2.getName());
			assertEquals(BigDecimal.valueOf(35.50), actualDto2.getPrice());
			assertFalse(actualDto2.getAvailability());
		}).verifyComplete();

		// Verify all interactions occurred
		verify(this.similarProductsService).getSimilarProducts(productId);
		verify(this.productDetailMapper).toApiModel(product1);
		verify(this.productDetailMapper).toApiModel(product2);
	}

	// ===== HELPER METHODS =====

	private ProductDetailDTO createProductDetailDTO(String id, String name, double price, boolean availability) {
		final ProductDetailDTO dto = new ProductDetailDTO();
		dto.setId(id);
		dto.setName(name);
		dto.setPrice(price != 0 ? BigDecimal.valueOf(price) : BigDecimal.ZERO);
		dto.setAvailability(availability);
		return dto;
	}

	private List<ProductDetail> generateProducts(int count) {
		return java.util.stream.IntStream.range(1, count + 1)
				.mapToObj(i -> new ProductDetail("prod" + i, "Product " + i, i * 10.0, i % 2 == 0)).toList();
	}

	private List<ProductDetailDTO> generateProductDTOs(int count) {
		return java.util.stream.IntStream.range(1, count + 1)
				.mapToObj(i -> this.createProductDetailDTO("prod" + i, "Product " + i, i * 10.0, i % 2 == 0)).toList();
	}
}
