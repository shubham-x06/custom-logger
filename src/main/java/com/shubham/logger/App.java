package com.shubham.logger;

import com.shubham.logger.appender.AsyncAppender;
import com.shubham.logger.appender.FileAppender;

public class App {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getInstance();
        
        FileAppender fileAppender = new FileAppender("async_logs.txt");

        AsyncAppender asyncLogger = new AsyncAppender(fileAppender);
        
        logger.setAppender(asyncLogger);

        System.out.println("Starting high-speed logging ->");
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= 50; i++) {
            logger.log(Loglevel.INFO, "Message number-> " + i);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Finished sending 50 messages in " + (endTime - startTime) + "ms");

        Thread.sleep(1000); 
        System.out.println("Check 'async_logs.txt' to see if all messages arrived.");

        // Logger logger2 = Logger.getInstance();
        // System.out.println(logger == logger2); // should print true since both are same instance same object == checks reference  points to same memory location
    }
}