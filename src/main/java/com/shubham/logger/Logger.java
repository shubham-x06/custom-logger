package com.shubham.logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.shubham.logger.appender.Appender;
import com.shubham.logger.appender.ConsoleAppender;

public class Logger {
    private static volatile Logger instance;

    private final List<Appender> appenders = new CopyOnWriteArrayList<>();
    private Loglevel currentLevel;

    private Logger() {
        this.appenders.add(new ConsoleAppender());

        this.currentLevel = Loglevel.INFO;
    }

    // thread-safe singleton
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void setAppender(Appender appender2) {
        this.appenders.add(appender2);
    }

    public void clearAppenders() {
        this.appenders.clear();
    }

    public void setLevel(Loglevel level) {
        this.currentLevel = level;
    }

    public void log(Loglevel level, String message) {

        if (level.getSeverity() >= currentLevel.getSeverity()) {

            String formattedMessage = "[" + level + "] " + message;

            for (Appender appender : appenders) {
                appender.append(formattedMessage);
            }
        }
    }
}