package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.config.ProductServiceProperties;
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
	private final ProductServiceProperties properties;

	@Override
	public Flux<ProductDetail> getSimilarProducts(String productId) {
		return this.productDetailRepository.getSimilarIds(productId)
				.timeout(this.properties.getService().getSimilarProductsTimeout()) // Configurable timeout
				.flatMap(id -> this.productDetailRepository.getProductDetail(id)
						.onErrorResume(ProductNotFoundException.class, e -> Mono.empty()).switchIfEmpty(Mono.empty()),
						this.properties.getService().getConcurrencyLevel()) // Configurable concurrency
				.onErrorMap(e -> new SimilarProductsRetrievalException(productId, e))
				.onErrorResume(SimilarProductsRetrievalException.class, e -> Flux.empty());
	}
}
