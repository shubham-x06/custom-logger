package com.shubham.logger.controller;

import com.shubham.logger.Logger;
import com.shubham.logger.Loglevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
public class LogController {

    @Value("${logger.allowed-sources}")
    private String allowedSourcesConfig;

    @PostMapping
    public ResponseEntity<Map<String, Object>> log(@RequestBody Map<String, String> body) {
        String level   = body.getOrDefault("level", "INFO");
        String message = body.getOrDefault("message", "");
        String source  = body.getOrDefault("source", "unknown");

        List<String> allowedSources = Arrays.asList(allowedSourcesConfig.split(","));

        if (!allowedSources.contains(source)) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "Source not allowed"));
        }

        try {
            Loglevel lvl = Loglevel.valueOf(level.toUpperCase());
            Logger.getInstance().log(lvl, message, source);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "Invalid level"));
        }
    }
}
