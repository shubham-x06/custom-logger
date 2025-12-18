package com.shubham.logger.appender;

public class ConsoleAppender implements Appender {
    @Override
    public void append(String message) {
        System.out.println(message);
    }
    
}
