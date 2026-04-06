package com.shubham.logger.debug;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GroqDebugAssistant {
    private final GroqClient groqClient;
    private final Gson gson;

    public GroqDebugAssistant(GroqClient groqClient) {
        this.groqClient = groqClient;
        this.gson = new Gson();
    }

    public DebugResult analyzeLines(List<String> tailLines) {
        String logTail = String.join("\n", tailLines);

        String systemPrompt = "You are an expert Java debugging assistant for an async logging framework. " +
                "Given log output, respond ONLY in JSON with keys: rootCause (string), explanation (2-3 sentences), " +
                "fix (specific code-level suggestion), severity (LOW/MEDIUM/HIGH).";

        String jsonResponse = groqClient.complete(systemPrompt, logTail);
        
        // Strip markdown fences
        jsonResponse = jsonResponse.trim();
        if (jsonResponse.startsWith("```json")) {
            jsonResponse = jsonResponse.substring(7);
        } else if (jsonResponse.startsWith("```")) {
            jsonResponse = jsonResponse.substring(3);
        }
        if (jsonResponse.endsWith("```")) {
            jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
        }
        jsonResponse = jsonResponse.trim();

        return gson.fromJson(jsonResponse, DebugResult.class);
    }

    public DebugResult analyze(Path logFile, int lastN) {
        List<String> tailLines;
        try {
            String content = new String(Files.readAllBytes(logFile), java.nio.charset.StandardCharsets.UTF_8);
            List<String> allLines = java.util.Arrays.asList(content.split("\\r?\\n"));
            int start = Math.max(0, allLines.size() - lastN);
            tailLines = allLines.subList(start, allLines.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read log file: " + logFile, e);
        }
        return analyzeLines(tailLines);
    }
}
