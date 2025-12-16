package com.sngular.adriangm.myapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductDetail {
	private String id;
	private String name;
	private double price;
	private boolean availability;

}
