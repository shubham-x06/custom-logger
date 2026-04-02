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

        logger.clearAppenders();

        logger.setLevel(Loglevel.DEBUG);

        logger.addAppender(new ConsoleAppender((level, msg, src) -> "[" + level + "] [" + src + "] " + msg));
        logger.addAppender(new FileAppender("simulation_logs.txt", new DetailedFormatter()));

        logger.log(Loglevel.INFO, "Booting up Network Simulator...");

        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 10; i++) {
            logger.log(Loglevel.DEBUG, "Dispatching packet #" + i + " to worker pool...");
            threadPool.execute(new PacketHandler(i));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
            logger.log(Loglevel.INFO, "Server shutdown complete. All packets resolved.");
        } catch (InterruptedException e) {
            logger.log(Loglevel.ERROR, "Forced shutdown interrupted the queue.");
        }
        logger.log(Loglevel.INFO, "Starting log rotation stress test...");

        for (int i = 0; i < 500; i++) {
            logger.log(Loglevel.DEBUG, "Stress log message number " + i);
        }

        logger.log(Loglevel.INFO, "Stress test completed.");
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
            Thread.sleep(random.nextInt(500) + 100);

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