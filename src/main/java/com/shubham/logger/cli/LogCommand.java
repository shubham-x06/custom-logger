package com.shubham.logger.cli;

import com.shubham.logger.Logger;
import com.shubham.logger.Loglevel;
import com.shubham.logger.appender.ConsoleAppender;
import com.shubham.logger.appender.FileAppender;
import com.shubham.logger.formatter.DetailedFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Command(name = "log", description = "Emit a log message")
public class LogCommand implements Runnable {

    @Option(names = {"--level"}, description = "Log level (DEBUG, INFO, ERROR)", required = true)
    private String levelArg;

    @Option(names = {"--message"}, description = "Message to log", required = true)
    private String message;

    @Option(names = {"--appender"}, description = "Where to log (console or file)", required = true)
    private String appenderArg;

    @Override
    public void run() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("logger-cli.properties")) {
            properties.load(in);
        } catch (IOException e) {
            // Ignore if missing, we'll fall back or fail if lacking args
        }

        String configuredLevelStr = properties.getProperty("logLevel", "INFO");
        String configuredPath = properties.getProperty("logPath", "app.log");

        Logger logger = Logger.getInstance();
        logger.clearAppenders();
        
        try {
            Loglevel baseLevel = Loglevel.valueOf(configuredLevelStr.toUpperCase());
            logger.setLevel(baseLevel);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid base level in configs. Defaulting to INFO");
            logger.setLevel(Loglevel.INFO);
        }

        if ("file".equalsIgnoreCase(appenderArg)) {
            FileAppender fileAppender = new FileAppender(configuredPath, new DetailedFormatter());
            logger.addAppender(fileAppender);
            
            logAndClose(logger, fileAppender);
        } else if ("console".equalsIgnoreCase(appenderArg)) {
            logger.addAppender(new ConsoleAppender(new DetailedFormatter()));
            logAndClose(logger, null);
        } else {
            System.err.println("Invalid appender! Use 'console' or 'file'.");
        }
    }

    private void logAndClose(Logger logger, FileAppender closeableAppender) {
        try {
            Loglevel level = Loglevel.valueOf(levelArg.toUpperCase());
            logger.log(level, message);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid passed --level. Use DEBUG, INFO, or ERROR.");
        }

        if (closeableAppender != null) {
            closeableAppender.close();
        }
    }
}
