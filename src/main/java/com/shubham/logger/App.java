package com.shubham.logger;

import com.shubham.logger.appender.FileAppender;
import com.shubham.logger.formatter.DetailedFormatter;

public class App {
    public static void main(String[] args) {

        Logger logger = Logger.getInstance();

        logger.addAppender(new FileAppender("enterprise_logs.txt", new DetailedFormatter()));

        System.out.println("--- Testing Appender-Specific Formatting ---");

        logger.log(Loglevel.INFO, "User login successful.");
        logger.log(Loglevel.ERROR, "Payment gateway timeout.");

        System.out.println("Check 'enterprise_logs.txt'. It should have timestamps, unlike the console!");
    }
}