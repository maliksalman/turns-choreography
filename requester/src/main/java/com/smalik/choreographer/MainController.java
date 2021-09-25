package com.smalik.choreographer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/requests")
public class MainController {

    private final RequesterService service;

    @PostMapping
    public void turn(@RequestBody Load request) {
        service.generateLoad(request);
    }
}
