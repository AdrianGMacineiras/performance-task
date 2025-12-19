package controller;

import com.sngular.adriangm.myapp.controller.SimilarProductsController;
import com.sngular.adriangm.myapp.controller.mapper.ProductDetailMapper;
import com.sngular.adriangm.myapp.dto.ProductDetailDTO;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

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

	@Test
	void getProductSimilar_returnsProducts() {
		final ProductDetail product = new ProductDetail("1", "Product 1", 10.0, true);
		final ProductDetailDTO dto = new ProductDetailDTO();
		dto.setId("1");
		dto.setName("Product 1");
		dto.setPrice(BigDecimal.valueOf(10.0));
		dto.setAvailability(true);

		when(this.similarProductsService.getSimilarProducts("1")).thenReturn(Flux.just(product));
		when(this.productDetailMapper.toApiModel(product)).thenReturn(dto);

		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("1",
				this.exchange);

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectNext(dto).verifyComplete();
	}

	@Test
	void getProductSimilar_returnsEmpty() {
		when(this.similarProductsService.getSimilarProducts("2")).thenReturn(Flux.empty());

		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("2",
				this.exchange);

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).verifyComplete();
	}

	@Test
	void getProductSimilar_serviceError() {
		when(this.similarProductsService.getSimilarProducts("3"))
				.thenReturn(Flux.error(new RuntimeException("Service error")));

		final Mono<ResponseEntity<Flux<ProductDetailDTO>>> result = this.controller.getProductSimilar("3",
				this.exchange);

		StepVerifier.create(result.flatMapMany(ResponseEntity::getBody)).expectError(RuntimeException.class).verify();
	}
}
