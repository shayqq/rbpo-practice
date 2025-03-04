package ru.mtuci.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.request.TestRequest;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @PostMapping("/enter")
    public TestRequest enter(@RequestBody TestRequest req) {
        return req;
    }

}
