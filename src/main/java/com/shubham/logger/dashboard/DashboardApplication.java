package com.shubham.logger.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.shubham.logger"})
public class DashboardApplication {
    public static void main(String[] args) {
        if (System.getenv("GEMINI_API_KEY") == null || System.getenv("GEMINI_API_KEY").trim().isEmpty()) {
            System.err.println("WARNING: GEMINI_API_KEY is not set.");
            System.err.println("Run: export GEMINI_API_KEY=your_key_here");
            System.err.println("Get a key at: https://aistudio.google.com/app/apikey");
            // Dashboard does not exit, it shows a banner in the UI
        }
        SpringApplication.run(DashboardApplication.class, args);
    }
}
