package com.shubham.logger.debug;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DebugConfig {
    private String model = "gemini-2.5-flash";
    private int maxLines = 50;
    private int watchDebounceMs = 2000;
    private String logDirectory = "./logs";

    public DebugConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("logger-debug.properties")) {
            if (in != null) {
                props.load(in);
                this.model = props.getProperty("gemini.model", this.model);
                this.maxLines = Integer.parseInt(props.getProperty("gemini.max_lines", String.valueOf(this.maxLines)));
                this.watchDebounceMs = Integer.parseInt(props.getProperty("gemini.watch_debounce_ms", String.valueOf(this.watchDebounceMs)));
                this.logDirectory = props.getProperty("logger.log-directory", this.logDirectory);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load logger-debug.properties, using defaults.");
        }
    }

    public String getModel() { return model; }
    public int getMaxLines() { return maxLines; }
    public int getWatchDebounceMs() { return watchDebounceMs; }
    public String getLogDirectory() { return logDirectory; }
}
