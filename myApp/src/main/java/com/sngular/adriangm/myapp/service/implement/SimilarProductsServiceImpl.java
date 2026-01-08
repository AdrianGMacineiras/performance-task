package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.exception.SimilarProductsRetrievalException;
import com.sngular.adriangm.myapp.infrastructure.ProductDetailRepository;
import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SimilarProductsServiceImpl implements SimilarProductsService {

	private final ProductDetailRepository productDetailRepository;

	@Override
	public List<ProductDetail> getSimilarProducts(String productId) {
		try {
			final List<String> similarIds = this.productDetailRepository.getSimilarIds(productId);

			// Use parallel stream for concurrent processing of product details
			return similarIds.parallelStream().map(id -> {
				try {
					return this.productDetailRepository.getProductDetail(id);
				} catch (final Exception e) {
					return null; // Skip products that are not found or have errors
				}
			}).filter(Objects::nonNull).toList();

		} catch (final Exception e) {
			throw new SimilarProductsRetrievalException(productId, e);
		}
	}
}
