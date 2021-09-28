package com.smalik.choreographer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@RestController
@RequestMapping("/requests")
public class MainController {

    private final RequesterService service;
    private final WebClient webClient;

    @PostMapping
    public void turn(@RequestBody Load request) {
        service.generateLoad(webClient, request);
    }
}
