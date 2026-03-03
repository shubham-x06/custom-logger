package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncAppender implements Appender {
    @SuppressWarnings("unused")
    private final Appender wrappedAppender;
    private final BlockingQueue<LogEvent> queue;

    public AsyncAppender(Appender wrappedAppender) {
        this.wrappedAppender = wrappedAppender;
        this.queue = new LinkedBlockingQueue<>(50);

        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    LogEvent event = queue.take();
                    wrappedAppender.append(event.level, event.message);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void append(Loglevel level, String message) {
        if (!queue.offer(new LogEvent(level, message))) {
            System.err.println("Log Queue is full!");
        }
    }
}