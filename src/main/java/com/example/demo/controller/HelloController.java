package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    RestClient restClient;

    @GetMapping("/info")
    public String info() {
        System.out.println("*");
        String response;
        try {
            response = restClient.get()
                    .uri("https://dog.ceo/api/breeds/image/random")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            response = "Failed to fetch data from Google";
        }
        return response;
    }
}

