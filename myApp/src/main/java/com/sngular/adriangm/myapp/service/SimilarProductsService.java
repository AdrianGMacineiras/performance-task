package com.sngular.adriangm.myapp.service;

import com.sngular.adriangm.myapp.model.ProductDetail;

import java.util.List;

public interface SimilarProductsService {
	List<ProductDetail> getSimilarProducts(String productId);
}
