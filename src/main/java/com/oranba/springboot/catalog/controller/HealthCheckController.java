package com.oranba.springboot.catalog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Application is running");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}
