package com.shubham.logger.formatter;

import com.shubham.logger.Loglevel;

public interface Formatter {
    String format(Loglevel level, String message);
}