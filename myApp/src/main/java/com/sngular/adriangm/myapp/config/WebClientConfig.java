package com.sngular.adriangm.myapp.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

	private final ProductServiceProperties properties;

	public WebClientConfig(ProductServiceProperties properties) {
		this.properties = properties;
	}

	@Bean
	public WebClient webClient() {
		final ConnectionProvider provider = ConnectionProvider
				.builder(this.properties.getWebclient().getConnectionPoolName())
				.maxConnections(this.properties.getWebclient().getMaxConnections())
				.maxIdleTime(this.properties.getWebclient().getMaxIdleTime())
				.maxLifeTime(this.properties.getWebclient().getMaxLifeTime())
				.pendingAcquireTimeout(this.properties.getWebclient().getPendingAcquireTimeout()).build();

		final HttpClient httpClient = HttpClient.create(provider)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						(int) this.properties.getWebclient().getConnectionTimeout().toMillis())
				.responseTimeout(this.properties.getWebclient().getResponseTimeout())
				.doOnConnected(conn -> conn
						.addHandlerLast(new ReadTimeoutHandler(
								(int) this.properties.getWebclient().getReadTimeout().getSeconds()))
						.addHandlerLast(new WriteTimeoutHandler(
								(int) this.properties.getWebclient().getWriteTimeout().getSeconds())));

		return WebClient.builder().baseUrl(this.properties.getWebclient().getBaseUrl())
				.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}
}
