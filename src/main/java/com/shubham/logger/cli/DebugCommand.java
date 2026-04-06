package com.shubham.logger.cli;

import com.shubham.logger.debug.DebugConfig;
import com.shubham.logger.debug.DebugResult;
import com.shubham.logger.debug.GroqClient;
import com.shubham.logger.debug.GroqDebugAssistant;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@Command(name = "debug", description = "AI-powered debug analysis of logger files using Groq")
public class DebugCommand implements Runnable {

    @Option(names = {"--file"}, required = true, description = "Path to the log file")
    private String file;

    @Option(names = {"--last"}, description = "Number of trailing lines to analyze (defaults to config)")
    private Integer last;

    @Option(names = {"--watch"}, defaultValue = "false", description = "Watch mode for live analysis")
    private boolean watch;

    @Override
    public void run() {
        if (System.getenv("GROQ_API_KEY") == null || System.getenv("GROQ_API_KEY").trim().isEmpty()) {
            System.err.println("ERROR: GROQ_API_KEY is not set.\n" +
                    "Run: export GROQ_API_KEY=your_key_here\n" +
                    "Get a key at: https://console.groq.com");
            System.exit(1);
        }

        DebugConfig config = new DebugConfig();
        int lastN = (this.last != null) ? this.last : config.getMaxLines();

        GroqClient client = new GroqClient(config.getModel());
        GroqDebugAssistant assistant = new GroqDebugAssistant(client);
        Path logFile = Path.of(file);

        if (!watch) {
            System.out.println("=== Groq Analysis ===");
            try {
                DebugResult result = assistant.analyze(logFile, lastN);
                printResult(result);
            } catch (Exception e) {
                System.err.println("Analysis failed: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Cause: " + e.getCause());
                }
            }
        } else {
            System.out.println("[watching " + file + " — Ctrl+C to stop]");
            watchFile(logFile, assistant, config);
        }
    }

    private void printResult(DebugResult result) {
        String colorCode = "\u001B[0m"; // default
        if ("HIGH".equalsIgnoreCase(result.severity())) {
            colorCode = "\u001B[31m"; // red
        } else if ("MEDIUM".equalsIgnoreCase(result.severity())) {
            colorCode = "\u001B[33m"; // yellow
        } else if ("LOW".equalsIgnoreCase(result.severity())) {
            colorCode = "\u001B[32m"; // green
        }

        System.out.println("Root Cause:  " + result.rootCause());
        System.out.println("Explanation: " + result.explanation());
        System.out.println("Fix:         " + result.fix());
        System.out.println(colorCode + "Severity:    " + result.severity() + "\u001B[0m");
        System.out.println("-------------------------------------------------");
    }

    private void watchFile(Path logPath, GroqDebugAssistant assistant, DebugConfig config) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path parentDir = logPath.getParent() != null ? logPath.getParent() : Path.of(".");
            parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            long filePointer = Files.exists(logPath) ? Files.size(logPath) : 0;
            ArrayDeque<String> buffer = new ArrayDeque<>();

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (changed.getFileName().toString().equals(logPath.getFileName().toString())) {
                        try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
                            raf.seek(filePointer);
                            String line;
                            boolean foundError = false;
                            while ((line = raf.readLine()) != null) {
                                if (buffer.size() >= 100) buffer.pollFirst();
                                buffer.addLast(line);
                                if (line.contains("ERROR")) {
                                    foundError = true;
                                }
                            }
                            filePointer = raf.getFilePointer();

                            if (foundError) {
                                // debounce
                                Thread.sleep(config.getWatchDebounceMs());
                                
                                // read any extra lines that might have come during debounce
                                raf.seek(filePointer);
                                while ((line = raf.readLine()) != null) {
                                    if (buffer.size() >= 100) buffer.pollFirst();
                                    buffer.addLast(line);
                                }
                                filePointer = raf.getFilePointer();

                                // Extract last min(50, size)
                                List<String> last50 = new ArrayList<>();
                                int toSkip = Math.max(0, buffer.size() - 50);
                                int idx = 0;
                                for (String s : buffer) {
                                    if (idx++ >= toSkip) last50.add(s);
                                }

                                System.out.println("=== Groq Analysis (Triggered by ERROR) ===");
                                DebugResult result = assistant.analyzeLines(last50);
                                printResult(result);
                            }
                        } catch (IOException | InterruptedException e) {
                            System.err.println("Error reading file: " + e.getMessage());
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) break;
            }
        } catch (Exception e) {
            System.err.println("Watch service failed: " + e.getMessage());
        }
    }
}
