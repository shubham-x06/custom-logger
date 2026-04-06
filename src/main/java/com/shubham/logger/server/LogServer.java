package com.shubham.logger.server;

import com.shubham.logger.Logger;
import com.shubham.logger.Loglevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@org.springframework.context.annotation.ComponentScan(basePackages = {"com.shubham.logger"})
@RestController
public class LogServer {

    @Value("${logger.allowed-sources}")
    private String allowedSourcesConfig;

    public static void main(String[] args) {
        if (System.getenv("GEMINI_API_KEY") == null || System.getenv("GEMINI_API_KEY").trim().isEmpty()) {
            System.err.println("WARNING: GEMINI_API_KEY is not set.");
            System.err.println("Dashboard features will show a red banner until you export the key.");
        }
        SpringApplication.run(LogServer.class, args);
    }

    @PostMapping("/log")
    public ResponseEntity<String> receiveLog(
            @RequestHeader(value = "X-Logger-Source", required = true) String sourceHeader,
            @RequestBody Map<String, String> payload) {

        List<String> allowedSources = Arrays.asList(allowedSourcesConfig.split(","));
        if (!allowedSources.contains(sourceHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Source not allowed");
        }

        String levelStr = payload.get("level");
        String message = payload.get("message");
        
        try {
            Loglevel level = Loglevel.valueOf(levelStr.toUpperCase());
            Logger.getInstance().log(level, message, sourceHeader);
            return ResponseEntity.ok("Logged successfully");
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.badRequest().body("Invalid Loglevel or payload");
        }
    }
}
