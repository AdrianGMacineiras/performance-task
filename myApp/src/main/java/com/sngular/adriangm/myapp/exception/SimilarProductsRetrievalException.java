package com.sngular.adriangm.myapp.exception;

import java.io.Serial;

public class SimilarProductsRetrievalException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public SimilarProductsRetrievalException(String productId, Throwable cause) {
		super("Failed to retrieve similar products for: " + productId, cause);
	}
}
