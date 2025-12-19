package com.sngular.adriangm.myapp.exception;

public class SimilarProductsRetrievalException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public SimilarProductsRetrievalException(String productId, Throwable cause) {
		super("Failed to retrieve similar products for: " + productId, cause);
	}
}
