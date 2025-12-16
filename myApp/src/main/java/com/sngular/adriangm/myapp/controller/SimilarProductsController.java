package com.sngular.adriangm.myapp.controller;

import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class SimilarProductsController {

	private final SimilarProductsService similarProductsService;

	@GetMapping("/{productId}/similar")
	public ResponseEntity<List<ProductDetail>> getSimilarProducts(@PathVariable String productId) {
		final List<ProductDetail> similarProducts = this.similarProductsService.getSimilarProducts(productId);
		if (similarProducts == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		return ResponseEntity.ok(similarProducts);
	}
}
