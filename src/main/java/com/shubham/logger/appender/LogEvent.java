package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;

public class LogEvent {
    public final Loglevel level;
    public final String message;

    public LogEvent(Loglevel level, String message) {
        this.level = level;
        this.message = message;
    }
}