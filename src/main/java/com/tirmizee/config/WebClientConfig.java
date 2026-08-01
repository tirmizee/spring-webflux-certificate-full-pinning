package com.tirmizee.config;

import com.tirmizee.security.CertificatePinLoader;
import com.tirmizee.security.FullCertPinningTrustManager;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(CertificatePinLoader pinLoader) throws Exception {

        FullCertPinningTrustManager trustManager = new FullCertPinningTrustManager(pinLoader.loadPinSet());

        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(trustManager)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(spec -> spec.sslContext(sslContext));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
