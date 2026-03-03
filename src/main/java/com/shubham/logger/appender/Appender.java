package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;

public interface Appender {
    void append(Loglevel level, String message);
}