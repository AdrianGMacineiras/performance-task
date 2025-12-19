package com.sngular.adriangm.myapp.exception;

import java.io.Serial;

public class ProductNotFoundException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public ProductNotFoundException(String productId) {
		super("Product not found: " + productId);
	}
}
