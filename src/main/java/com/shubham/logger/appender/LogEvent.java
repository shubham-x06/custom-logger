package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;

public class LogEvent {
    public final Loglevel level;
    public final String message;
    public final String source;

    public LogEvent(Loglevel level, String message, String source) {
        this.level = level;
        this.message = message;
        this.source = source;
    }
}