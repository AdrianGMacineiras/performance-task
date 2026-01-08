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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimilarProductsControllerTest {

	@Mock
	private ProductDetailMapper productDetailMapper;
	@Mock
	private SimilarProductsService similarProductsService;

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
		final List<ProductDetail> products = List.of(product);

		when(this.similarProductsService.getSimilarProducts("1")).thenReturn(products);
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("1");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains(dto));
		assertEquals(1, result.getBody().size());

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("1");
		verify(this.productDetailMapper).toApiModel(product);
	}

	@Test
	@DisplayName("Should return empty set with OK status when no similar products found")
	void getProductSimilar_returnsEmpty() {
		// Arrange
		when(this.similarProductsService.getSimilarProducts("2")).thenReturn(List.of());

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("2");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());

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

		when(this.similarProductsService.getSimilarProducts("0")).thenReturn(products);
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));
		when(this.productDetailMapper.toApiModel(products.get(2))).thenReturn(dtos.get(2));

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("0");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(3, result.getBody().size());
		assertTrue(result.getBody().containsAll(dtos));

		// Verify all products were mapped
		verify(this.productDetailMapper, times(3)).toApiModel(any(ProductDetail.class));
	}

	@Test
	@DisplayName("Should return all products (Set doesn't preserve order)")
	void getProductSimilar_returnsAllProducts() {
		// Arrange
		final List<ProductDetail> products = Arrays.asList(new ProductDetail("3", "Product 3", 30.0, true),
				new ProductDetail("1", "Product 1", 10.0, true), new ProductDetail("2", "Product 2", 20.0, false));

		final List<ProductDetailDTO> dtos = Arrays.asList(this.createProductDetailDTO("3", "Product 3", 30.0, true),
				this.createProductDetailDTO("1", "Product 1", 10.0, true),
				this.createProductDetailDTO("2", "Product 2", 20.0, false));

		when(this.similarProductsService.getSimilarProducts("0")).thenReturn(products);
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));
		when(this.productDetailMapper.toApiModel(products.get(2))).thenReturn(dtos.get(2));

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("0");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(3, result.getBody().size());
		assertTrue(result.getBody().containsAll(dtos));
	}

	// ===== ERROR HANDLING TESTS =====

	@Test
	@DisplayName("Should propagate service errors")
	void getProductSimilar_serviceError() {
		// Arrange
		final RuntimeException serviceError = new RuntimeException("Service error");
		when(this.similarProductsService.getSimilarProducts("3")).thenThrow(serviceError);

		// Act & Assert
		assertThrows(RuntimeException.class, () -> this.controller.getProductSimilar("3"));

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
		final List<ProductDetail> products = List.of(product);

		when(this.similarProductsService.getSimilarProducts("1")).thenReturn(products);
		when(this.productDetailMapper.toApiModel(product)).thenThrow(mapperError);

		// Act & Assert
		assertThrows(RuntimeException.class, () -> this.controller.getProductSimilar("1"));

		// Verify interactions
		verify(this.similarProductsService).getSimilarProducts("1");
		verify(this.productDetailMapper).toApiModel(product);
	}

	// ===== PATH VARIABLE TESTS =====

	@Test
	@DisplayName("Should handle empty string product ID")
	void getProductSimilar_emptyProductId() {
		// Arrange
		when(this.similarProductsService.getSimilarProducts("")).thenReturn(List.of());

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().isEmpty());

		verify(this.similarProductsService).getSimilarProducts("");
	}

	@Test
	@DisplayName("Should handle special characters in product ID")
	void getProductSimilar_specialCharactersProductId() {
		// Arrange
		final String specialId = "product-123_abc@test.com";
		final ProductDetail product = new ProductDetail(specialId, "Special Product", 99.99, true);
		final ProductDetailDTO dto = this.createProductDetailDTO(specialId, "Special Product", 99.99, true);

		when(this.similarProductsService.getSimilarProducts(specialId)).thenReturn(List.of(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar(specialId);

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains(dto));
		assertEquals(1, result.getBody().size());

		verify(this.similarProductsService).getSimilarProducts(specialId);
	}

	@Test
	@DisplayName("Should handle numeric product ID")
	void getProductSimilar_numericProductId() {
		// Arrange
		final String numericId = "12345";
		final ProductDetail product = new ProductDetail(numericId, "Numeric Product", 50.0, false);
		final ProductDetailDTO dto = this.createProductDetailDTO(numericId, "Numeric Product", 50.0, false);

		when(this.similarProductsService.getSimilarProducts(numericId)).thenReturn(List.of(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar(numericId);

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains(dto));
		assertEquals(1, result.getBody().size());
	}

	// ===== EDGE CASES =====

	@Test
	@DisplayName("Should handle products with null/empty fields")
	void getProductSimilar_productsWithNullFields() {
		// Arrange
		final ProductDetail productWithNulls = new ProductDetail(null, null, 0.0, false);
		final ProductDetailDTO dtoWithNulls = this.createProductDetailDTO(null, null, 0.0, false);

		when(this.similarProductsService.getSimilarProducts("null-test")).thenReturn(List.of(productWithNulls));
		when(this.productDetailMapper.toApiModel(productWithNulls)).thenReturn(dtoWithNulls);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("null-test");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains(dtoWithNulls));
		assertEquals(1, result.getBody().size());
	}

	@Test
	@DisplayName("Should handle products with extreme values")
	void getProductSimilar_productsWithExtremeValues() {
		// Arrange
		final ProductDetail extremeProduct = new ProductDetail("extreme", "Very Long Product Name ".repeat(10),
				Double.MAX_VALUE, true);
		final ProductDetailDTO extremeDto = this.createProductDetailDTO("extreme", "Very Long Product Name ".repeat(10),
				Double.MAX_VALUE, true);

		when(this.similarProductsService.getSimilarProducts("extreme")).thenReturn(List.of(extremeProduct));
		when(this.productDetailMapper.toApiModel(extremeProduct)).thenReturn(extremeDto);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("extreme");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains(extremeDto));
		assertEquals(1, result.getBody().size());
	}

	// ===== PERFORMANCE TESTS =====

	@Test
	@DisplayName("Should handle large number of products efficiently")
	void getProductSimilar_largeDataset() {
		// Arrange
		final int productCount = 100;
		final List<ProductDetail> products = this.generateProducts();
		final List<ProductDetailDTO> dtos = this.generateProductDTOs();

		when(this.similarProductsService.getSimilarProducts("large-set")).thenReturn(products);

		// Mock mapper for all products
		for (int i = 0; i < productCount; i++) {
			when(this.productDetailMapper.toApiModel(products.get(i))).thenReturn(dtos.get(i));
		}

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("large-set");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(productCount, result.getBody().size());
		assertTrue(result.getBody().containsAll(dtos));

		verify(this.productDetailMapper, times(productCount)).toApiModel(any(ProductDetail.class));
	}

	// ===== RESPONSE STRUCTURE TESTS =====

	@Test
	@DisplayName("Should return proper ResponseEntity structure")
	void getProductSimilar_responseEntityStructure() {
		// Arrange
		final ProductDetail product = new ProductDetail("structure-test", "Structure Product", 15.0, true);
		final ProductDetailDTO dto = this.createProductDetailDTO("structure-test", "Structure Product", 15.0, true);

		when(this.similarProductsService.getSimilarProducts("structure-test")).thenReturn(List.of(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("structure-test");

		// Assert
		assertNotNull(result);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertTrue(result.hasBody());
		assertTrue(result.getBody().contains(dto));

		// Verify headers (should be empty/default)
		assertNotNull(result.getHeaders());
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

		when(this.similarProductsService.getSimilarProducts("special-prices")).thenReturn(products);
		when(this.productDetailMapper.toApiModel(products.get(0))).thenReturn(dtos.get(0));
		when(this.productDetailMapper.toApiModel(products.get(1))).thenReturn(dtos.get(1));

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar("special-prices");

		// Assert
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertTrue(result.getBody().containsAll(dtos));
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

		when(this.similarProductsService.getSimilarProducts(productId)).thenReturn(Arrays.asList(product1, product2));
		when(this.productDetailMapper.toApiModel(product1)).thenReturn(dto1);
		when(this.productDetailMapper.toApiModel(product2)).thenReturn(dto2);

		// Act
		final ResponseEntity<Set<ProductDetailDTO>> result = this.controller.getProductSimilar(productId);

		// Assert - Verify complete response
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals(2, result.getBody().size());
		assertTrue(result.getBody().contains(dto1));
		assertTrue(result.getBody().contains(dto2));

		// Verify content details
		final Set<ProductDetailDTO> actualDtos = result.getBody();
		final ProductDetailDTO actualDto1 = actualDtos.stream().filter(dto -> "int1".equals(dto.getId())).findFirst()
				.orElse(null);
		final ProductDetailDTO actualDto2 = actualDtos.stream().filter(dto -> "int2".equals(dto.getId())).findFirst()
				.orElse(null);

		assertNotNull(actualDto1);
		assertEquals("Integration Product 1", actualDto1.getName());
		assertEquals(BigDecimal.valueOf(25.99), actualDto1.getPrice());
		assertTrue(actualDto1.getAvailability());

		assertNotNull(actualDto2);
		assertEquals("Integration Product 2", actualDto2.getName());
		assertEquals(BigDecimal.valueOf(35.50), actualDto2.getPrice());
		assertFalse(actualDto2.getAvailability());

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

	private List<ProductDetail> generateProducts() {
		return java.util.stream.IntStream.range(1, 100 + 1)
				.mapToObj(i -> new ProductDetail("prod" + i, "Product " + i, i * 10.0, i % 2 == 0)).toList();
	}

	private List<ProductDetailDTO> generateProductDTOs() {
		return java.util.stream.IntStream.range(1, 100 + 1)
				.mapToObj(i -> this.createProductDetailDTO("prod" + i, "Product " + i, i * 10.0, i % 2 == 0)).toList();
	}
}
