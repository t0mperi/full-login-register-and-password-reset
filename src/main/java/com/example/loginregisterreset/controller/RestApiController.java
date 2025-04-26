package com.example.loginregisterreset.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class RestApiController {

    @GetMapping("/api/status")
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        status.put("message", "Service is running");
        return status; 
    }
}
