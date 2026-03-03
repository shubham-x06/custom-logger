package com.shubham.logger;

import com.shubham.logger.appender.Appender;
import com.shubham.logger.appender.ConsoleAppender;

public class Logger {
    private static volatile Logger instance;
    private Appender appender;

    private Loglevel currentLevel;

    private Logger() {
        this.appender = new ConsoleAppender();

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
        this.appender = appender2;
    }

    public void setLevel(Loglevel level) {
        this.currentLevel = level;
    }

    public void log(Loglevel level, String message) {

        if (level.getSeverity() >= currentLevel.getSeverity()) {

            String formattedMessage = "[" + level + "] " + message;

            appender.append(formattedMessage);
        }
    }
}