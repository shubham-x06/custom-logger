package com.shubham.logger;

public class App {
    public static void main(String[] args) {

        Logger logger = Logger.getInstance();

        System.out.println("--- TESTING LEVEL: INFO (Default) ---");
        // INFO is 20. DEBUG is 10. ERROR is 30.
        logger.log(Loglevel.DEBUG, "1. [DEBUG] You should NOT see this.");
        logger.log(Loglevel.INFO, "2. [INFO]  You SHOULD see this.");
        logger.log(Loglevel.ERROR, "3. [ERROR] You SHOULD see this.");

        System.out.println("\n--- TESTING LEVEL: ERROR ---");
        logger.setLevel(Loglevel.ERROR); // Now only ERROR passes (30)
        logger.log(Loglevel.INFO, "4. [INFO]  You should NOT see this anymore.");
        logger.log(Loglevel.ERROR, "5. [ERROR] You SHOULD see this.");

        System.out.println("\n--- TESTING LEVEL: DEBUG (Turn on everything) ---");
        logger.setLevel(Loglevel.DEBUG); // Now everything passes (10+)
        logger.log(Loglevel.DEBUG, "6. [DEBUG] You SHOULD see this now!");
    }
}