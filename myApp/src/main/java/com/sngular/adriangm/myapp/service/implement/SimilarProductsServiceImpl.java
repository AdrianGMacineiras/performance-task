package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.exception.ProductNotFoundException;
import com.sngular.adriangm.myapp.exception.SimilarProductsRetrievalException;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SimilarProductsServiceImpl implements SimilarProductsService {

	private final ProductDetailRepository productDetailRepository;

	@Override
	public Flux<ProductDetail> getSimilarProducts(String productId) {
		return this.productDetailRepository.getSimilarIds(productId)
				.flatMap(id -> this.productDetailRepository.getProductDetail(id)
						.onErrorResume(ProductNotFoundException.class, e -> Mono.empty()).switchIfEmpty(Mono.empty()))
				.onErrorMap(e -> new SimilarProductsRetrievalException(productId, e))
				.onErrorResume(SimilarProductsRetrievalException.class, e -> Flux.empty());
	}
}
