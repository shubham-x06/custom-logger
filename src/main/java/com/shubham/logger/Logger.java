package com.shubham.logger;

import com.shubham.logger.appender.Appender;
import com.shubham.logger.appender.ConsoleAppender;


public class Logger {
    private static volatile Logger instance;;
    private  Appender appender;

    private Logger() {
        this.appender = new ConsoleAppender();
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

    public void log(Loglevel level, String message) {
        String logMessage = "[" + level + "] " + message;
        appender.append(logMessage);
    }
}
