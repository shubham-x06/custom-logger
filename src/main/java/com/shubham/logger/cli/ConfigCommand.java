package com.shubham.logger.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Command(name = "config", description = "Configure the CLI logger default properties")
public class ConfigCommand implements Runnable {

    @Option(names = {"--path"}, description = "Path to the default log file", required = true)
    private String logPath;

    @Option(names = {"--level"}, description = "Default minimum logging level (DEBUG, INFO, ERROR)", required = true)
    private String logLevel;

    @Override
    public void run() {
        Properties properties = new Properties();
        properties.setProperty("logPath", logPath);
        properties.setProperty("logLevel", logLevel.toUpperCase());

        try (FileOutputStream out = new FileOutputStream("logger-cli.properties")) {
            properties.store(out, "Logger CLI Configurations");
            System.out.println("Configuration saved to logger-cli.properties successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }
}
