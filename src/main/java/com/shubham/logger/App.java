package com.shubham.logger;

import com.shubham.logger.appender.FileAppender;

public class App {
    public static void main(String[] args) {

        Logger logger = Logger.getInstance();

        // 1. By default, it has ConsoleAppender.
        logger.log(Loglevel.INFO, "Message 1: This is on the Console only.");

        // 2. Add a FileAppender to the mix
        // Now it has [ConsoleAppender, FileAppender]
        logger.setAppender(new FileAppender("broadcaster_test.txt"));

        System.out.println("--- Broadcasting Started ---");

        // 3. This message should appear on BOTH
        logger.log(Loglevel.ERROR, "Message 2: This is on Console AND File!");

        System.out.println("Check 'broadcaster_test.txt'. Did Message 2 appear there?");
    }
}