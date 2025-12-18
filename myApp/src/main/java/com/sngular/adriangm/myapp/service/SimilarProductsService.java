package com.sngular.adriangm.myapp.service;

import com.sngular.adriangm.myapp.model.ProductDetail;
import reactor.core.publisher.Flux;

public interface SimilarProductsService {
	Flux<ProductDetail> getSimilarProducts(String productId);
}
