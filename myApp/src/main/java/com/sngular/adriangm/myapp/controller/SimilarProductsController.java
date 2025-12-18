package com.sngular.adriangm.myapp.controller;

import com.sngular.adriangm.myapp.api.generated.DefaultApi;
import com.sngular.adriangm.myapp.controller.mapper.ProductDetailMapper;
import com.sngular.adriangm.myapp.dto.ProductDetailDTO;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SimilarProductsController implements DefaultApi {

	private final ProductDetailMapper productDetailMapper;
	private final SimilarProductsService similarProductsService;

	@Override
	@GetMapping("/{id}/similar")
	public Mono<ResponseEntity<Flux<ProductDetailDTO>>> getProductSimilar(@PathVariable("id") String productId,
																		  ServerWebExchange exchange) {
		final Flux<ProductDetailDTO> dtoFlux = this.similarProductsService.getSimilarProducts(productId)
				.map(this.productDetailMapper::toApiModel);
		return Mono.just(ResponseEntity.ok(dtoFlux));
	}
}
