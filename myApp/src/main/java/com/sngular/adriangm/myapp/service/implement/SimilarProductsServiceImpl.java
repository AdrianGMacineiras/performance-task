package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimilarProductsServiceImpl implements SimilarProductsService {

	private final RestTemplate restTemplate;
	private static final String BASE_URL = "http://localhost:3001";

	@Override
	public List<ProductDetail> getSimilarProducts(String productId) {
		final String similarIdsUrl = BASE_URL + "/product/" + productId + "/similarids";
		final List<String> similarIds;
		try {
			final ResponseEntity<String[]> response = this.restTemplate.getForEntity(similarIdsUrl, String[].class);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				return Collections.emptyList();
			}
			similarIds = List.of(response.getBody());
		} catch (final RestClientException e) {
			return Collections.emptyList();
		}

		final List<ProductDetail> result = new ArrayList<>();
		for (final String id : similarIds) {
			final String detailUrl = BASE_URL + "/product/" + id;
			try {
				final ResponseEntity<ProductDetail> detailResponse = this.restTemplate.getForEntity(detailUrl,
						ProductDetail.class);
				if (detailResponse.getStatusCode().is2xxSuccessful() && detailResponse.getBody() != null) {
					result.add(detailResponse.getBody());
				}
			} catch (final RestClientException ignored) {
				return Collections.emptyList();
			}
		}
		return result;
	}
}
