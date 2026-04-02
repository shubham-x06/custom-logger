package com.shubham.logger.formatter;

import com.shubham.logger.Loglevel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailedFormatter implements Formatter {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(Loglevel level, String message, String source) {
        String timestamp = dtf.format(LocalDateTime.now());
        String threadName = Thread.currentThread().getName();

        return String.format("[%s] [%s] [%s] [%s] %s", timestamp, threadName, source, level, message);
    }
}