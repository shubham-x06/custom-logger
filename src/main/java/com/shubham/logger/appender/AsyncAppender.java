package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncAppender implements Appender {
    
    private final Appender wrappedAppender;
    private final BlockingQueue<LogEvent> queue;

    private volatile boolean running = true;
    private final Thread worker;

    public void shutdown() {
        running = false;
        queue.offer(new LogEvent(null, null, null));
        try {
            worker.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public AsyncAppender(Appender wrappedAppender) {
        this.wrappedAppender = wrappedAppender;
        this.queue = new LinkedBlockingQueue<>(50);

        this.worker = new Thread(() -> {
            while (running) {
                try {
                    LogEvent event = queue.take();
                    if (event.level == null) break;
                    wrappedAppender.append(event.level, event.message, event.source);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        this.worker.setDaemon(true);
        this.worker.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void append(Loglevel level, String message, String source) {
        if (!queue.offer(new LogEvent(level, message, source))) {
            System.err.println("Log Queue is full!");
        }
    }
}