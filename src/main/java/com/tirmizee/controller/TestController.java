package com.tirmizee.controller;

import com.tirmizee.client.MockJsonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final MockJsonClient mockJsonClient;

    @GetMapping("/api/todo/{id}")
    public Mono<String> getTodo(@PathVariable String id) {
        return mockJsonClient.getToDdById(id);
    }

}
