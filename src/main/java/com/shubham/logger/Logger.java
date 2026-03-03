package com.shubham.logger;

import com.shubham.logger.appender.Appender;
import com.shubham.logger.appender.ConsoleAppender;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Logger {
    private static volatile Logger instance;
    private final List<Appender> appenders = new CopyOnWriteArrayList<>();
    private Loglevel currentLevel;

    private Logger() {
        this.appenders.add(new ConsoleAppender((level, msg) -> "[" + level + "] " + msg));
        this.currentLevel = Loglevel.INFO;
    }

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

    public void addAppender(Appender newAppender) {
        this.appenders.add(newAppender);
    }

    public void clearAppenders() {
        this.appenders.clear();
    }

    public void setLevel(Loglevel level) {
        this.currentLevel = level;
    }

    public void log(Loglevel level, String message) {
        if (level.getSeverity() >= currentLevel.getSeverity()) {
            for (Appender appender : appenders) {
                appender.append(level, message);
            }
        }
    }
}