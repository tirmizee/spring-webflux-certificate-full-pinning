package com.tirmizee.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MockJsonClient {

    private final WebClient webClient;

    public Mono<String> getToDdById(String id) {
        return webClient.get()
                .uri("https://jsonplaceholder.typicode.com/todos/{id}", id)
                .retrieve()
                .bodyToMono(String.class);
    }

}
