package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;

public class ConsoleAppender implements Appender {

    private final Formatter formatter;

    public ConsoleAppender(Formatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void append(Loglevel level, String message, String source) {
        System.out.println(formatter.format(level, message, source));
    }
}