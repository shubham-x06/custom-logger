package com.shubham.logger;

import com.shubham.logger.appender.FileAppender;

public class App {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.log(Loglevel.INFO, "info message to console.");
        // logger.log(Loglevel.DEBUG, "debug message.");
        // logger.log(Loglevel.ERROR, "error message.");
        logger.setAppender(new FileAppender("app_logs.txt"));
        logger.log(Loglevel.ERROR, "error message to file.");
        logger.log(Loglevel.INFO, "info message to file.");

        System.out.println("Checking 'app_logs.txt'");

        


        // Logger logger2 = Logger.getInstance();
        // System.out.println(logger == logger2); // should print true since both are same instance same object == checks reference  points to same memory location
    }
}