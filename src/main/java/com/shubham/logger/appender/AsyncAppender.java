package com.shubham.logger.appender;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncAppender implements Appender {

    private final Appender wrappedAppender;
    private final BlockingQueue<String> queue;

    public AsyncAppender(Appender wrappedAppender) {
        this.wrappedAppender = wrappedAppender;
        this.queue = new LinkedBlockingQueue<>(50);
        
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    String message = queue.take();
                    wrappedAppender.append(message);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        worker.setDaemon(true); 
        worker.start();
    }

    @Override
    public void append(String message) {
       
        if (!queue.offer(message)) {
            System.err.println("Log Queue is full so Dropping message: " + message);
        }
    }
}