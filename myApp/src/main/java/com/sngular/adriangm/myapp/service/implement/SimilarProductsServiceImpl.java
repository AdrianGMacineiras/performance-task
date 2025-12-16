package com.sngular.adriangm.myapp.service.implement;

import com.sngular.adriangm.myapp.model.ProductDetail;
import com.sngular.adriangm.myapp.service.SimilarProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SimilarProductsServiceImpl implements SimilarProductsService {

	private final RestTemplate restTemplate;
	private static final String BASE_URL = "http://localhost:3001";
	private static final int MAX_PARALLEL = 20;
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(200);

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

		final List<String> limitedIds = similarIds.size() > MAX_PARALLEL
				? similarIds.subList(0, MAX_PARALLEL)
				: similarIds;

		final List<CompletableFuture<ProductDetail>> futures = limitedIds.stream()
				.map(id -> CompletableFuture.supplyAsync(() -> {
					final String detailUrl = BASE_URL + "/product/" + id;
					try {
						final ResponseEntity<ProductDetail> detailResponse = this.restTemplate.getForEntity(detailUrl,
								ProductDetail.class);
						if (detailResponse.getStatusCode().is2xxSuccessful() && detailResponse.getBody() != null) {
							return detailResponse.getBody();
						}
					} catch (final RestClientException e) {
						return null;
					}
					return null;
				}, EXECUTOR)).toList();

		return futures.stream().map(future -> {
			try {
				return future.get();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			} catch (final ExecutionException e) {
				return null;
			}
		}).filter(Objects::nonNull).toList();
	}
}
