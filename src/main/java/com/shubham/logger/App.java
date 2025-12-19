package com.shubham.logger;

public class App {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.log(Loglevel.INFO, "info message.");
        logger.log(Loglevel.DEBUG, "debug message.");
        logger.log(Loglevel.ERROR, "error message.");

        Logger logger2 = Logger.getInstance();
        System.out.println(logger == logger2); // should print true since both are same instance same object == checks reference  points to same memory location
    }
}