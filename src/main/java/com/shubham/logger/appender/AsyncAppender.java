package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncAppender implements Appender {
    
    private final Appender wrappedAppender;
    private final BlockingQueue<LogEvent> queue;

    public AsyncAppender(Appender wrappedAppender) {
        this.wrappedAppender = wrappedAppender;
        this.queue = new LinkedBlockingQueue<>(50);

        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    LogEvent event = queue.take();
                    wrappedAppender.append(event.level, event.message, event.source);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void append(Loglevel level, String message, String source) {
        if (!queue.offer(new LogEvent(level, message, source))) {
            System.err.println("Log Queue is full!");
        }
    }
}