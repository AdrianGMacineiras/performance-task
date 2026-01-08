package com.sngular.adriangm.myapp.config;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

	private final ProductServiceProperties properties;

	@Bean
	public RestTemplate restTemplate() {
		final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(this.properties.getRestTemplate().getMaxConnections());
		connectionManager.setDefaultMaxPerRoute(this.properties.getRestTemplate().getMaxConnectionsPerRoute());

		final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

		final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		factory.setConnectTimeout((int) this.properties.getRestTemplate().getConnectTimeout().toMillis());
		factory.setConnectionRequestTimeout((int) this.properties.getRestTemplate().getReadTimeout().toMillis());
		return new RestTemplate(factory);
	}
}
