package com.shubham.logger.dashboard;

import com.shubham.logger.debug.DebugConfig;
import com.shubham.logger.debug.DebugResult;
import com.shubham.logger.debug.GeminiClient;
import com.shubham.logger.debug.GeminiDebugAssistant;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final GeminiDebugAssistant assistant;
    private final DebugConfig config;

    public DashboardController() {
        this.config = new DebugConfig();
        GeminiClient client = null;
        try {
            client = new GeminiClient(); // Might throw exception if key is missing
        } catch (Exception e) {
            // Handled gracefully so dashboard still runs
        }
        this.assistant = client != null ? new GeminiDebugAssistant(client) : null;
    }

    @GetMapping("/")
    public String index(Model model) {
        boolean hasApiKey = System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").trim().isEmpty();
        model.addAttribute("hasApiKey", hasApiKey);
        return "dashboard";
    }

    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> payload) {
        if (assistant == null) {
            return ResponseEntity.status(500).body(Map.of("error", "GEMINI_API_KEY is missing. Configure it to use analysis."));
        }
        String filePath = (String) payload.get("filePath");
        int lastN = payload.containsKey("lastN") ? Integer.parseInt(payload.get("lastN").toString()) : config.getMaxLines();

        try {
            DebugResult result = assistant.analyze(Paths.get(filePath), lastN);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public ResponseEntity<?> getLogs(@RequestParam String file, @RequestParam(defaultValue = "100") int last) {
        try {
            Path path = Paths.get(file);
            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body(List.of("File not found"));
            }
            List<String> allLines = Files.readAllLines(path);
            int start = Math.max(0, allLines.size() - last);
            List<String> tailLines = allLines.subList(start, allLines.size());
            return ResponseEntity.ok(tailLines);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(List.of("Error reading logs: " + e.getMessage()));
        }
    }

    @GetMapping("/api/files")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFiles() {
        Path logDir = Paths.get(config.getLogDirectory());
        List<Map<String, Object>> fileData = new ArrayList<>();

        if (Files.exists(logDir) && Files.isDirectory(logDir)) {
            try {
                Files.walk(logDir)
                        .filter(p -> p.toString().endsWith(".log") || p.toString().endsWith(".txt"))
                        .forEach(p -> {
                            File f = p.toFile();
                            Map<String, Object> info = new HashMap<>();
                            info.put("name", f.getName());
                            info.put("path", f.getAbsolutePath());
                            info.put("sizeKb", f.length() / 1024);
                            info.put("lastModified", f.lastModified());
                            fileData.add(info);
                        });
            } catch (IOException e) {
                // Ignore
            }
        }
        return ResponseEntity.ok(fileData);
    }
}
