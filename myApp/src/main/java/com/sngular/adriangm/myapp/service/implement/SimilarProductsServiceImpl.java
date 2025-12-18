package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class SimilarProductsServiceImpl implements SimilarProductsService {

	private final ProductDetailRepository productDetailRepository;

	@Override
	public Flux<ProductDetail> getSimilarProducts(String productId) {
		return this.productDetailRepository.getSimilarIds(productId)
				.flatMap(this.productDetailRepository::getProductDetail);
	}
}
