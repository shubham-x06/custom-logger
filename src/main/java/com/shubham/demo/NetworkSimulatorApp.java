package com.shubham.demo;

import com.shubham.logger.Logger;
import com.shubham.logger.Loglevel;
import com.shubham.logger.appender.ConsoleAppender;
import com.shubham.logger.appender.FileAppender;
import com.shubham.logger.formatter.DetailedFormatter;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkSimulatorApp {

    public static void main(String[] args) {
        
        Logger logger = Logger.getInstance();
        
        // 1. Wipe out any default appenders so we have a clean slate
        logger.clearAppenders();
        
        // 2. Set the Gatekeeper to let everything through
        logger.setLevel(Loglevel.DEBUG); 
        
        // 3. Add our Appenders (Console gets basic, File gets Enterprise timestamps)
        logger.addAppender(new ConsoleAppender((level, msg) -> "[" + level + "] " + msg));
        logger.addAppender(new FileAppender("simulation_logs.txt", new DetailedFormatter()));

        logger.log(Loglevel.INFO, "Booting up Network Simulator...");

        // 4. Setup Thread Pool (Simulating 3 active concurrent connections)
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        // 5. Fire 10 concurrent packets into the server
        for (int i = 1; i <= 10; i++) {
            logger.log(Loglevel.DEBUG, "Dispatching packet #" + i + " to worker pool...");
            threadPool.execute(new PacketHandler(i));
        }

        // 6. Clean shutdown
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
            logger.log(Loglevel.INFO, "Server shutdown complete. All packets resolved.");
        } catch (InterruptedException e) {
            logger.log(Loglevel.ERROR, "Forced shutdown interrupted the queue.");
        }
    }
}

class PacketHandler implements Runnable {
    private final int packetId;
    private final Random random = new Random();
    private final Logger logger = Logger.getInstance();

    public PacketHandler(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public void run() {
        logger.log(Loglevel.DEBUG, "Receiving payload for Packet #" + packetId);
        
        try {
            // Simulate variable network latency
            Thread.sleep(random.nextInt(500) + 100);

            // Simulate a random collision/drop
            if (random.nextInt(5) == 0) {
                logger.log(Loglevel.ERROR, "Collision detected! Dropping Packet #" + packetId);
            } else {
                logger.log(Loglevel.INFO, "Packet #" + packetId + " routed successfully.");
            }
        } catch (InterruptedException e) {
            logger.log(Loglevel.ERROR, "Thread interrupted while processing Packet #" + packetId);
        }
    }
}