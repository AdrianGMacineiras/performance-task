package com.sngular.adriangm.myapp.infrastructure;

import com.sngular.adriangm.myapp.model.ProductDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductDetailRepository {
	Flux<String> getSimilarIds(String productId);
	Mono<ProductDetail> getProductDetail(String productId);
}
