package com.sngular.adriangm.myapp.infrastructure;

import com.sngular.adriangm.myapp.model.ProductDetail;

import java.util.List;

public interface ProductDetailRepository {
	List<String> getSimilarIds(String productId);
	ProductDetail getProductDetail(String productId);
}
