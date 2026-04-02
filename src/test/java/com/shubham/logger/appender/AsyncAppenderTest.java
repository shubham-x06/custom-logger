package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncAppenderTest {

    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream errStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setErr(new PrintStream(errStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setErr(standardErr);
    }

    @Test
    void testAsyncAppenderDrainsQueue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        List<String> messages = new ArrayList<>();
        
        Appender mockAppender = (level, message) -> {
            synchronized (messages) {
                messages.add(message);
            }
            latch.countDown();
        };

        AsyncAppender asyncAppender = new AsyncAppender(mockAppender);
        asyncAppender.append(Loglevel.INFO, "Msg 1");
        asyncAppender.append(Loglevel.INFO, "Msg 2");
        asyncAppender.append(Loglevel.INFO, "Msg 3");

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for async processing");
        
        synchronized (messages) {
            assertEquals(3, messages.size());
            assertTrue(messages.contains("Msg 1"));
            assertTrue(messages.contains("Msg 3"));
        }
    }

    @Test
    void testQueueFullBehavior() throws InterruptedException {
        CountDownLatch hangLatch = new CountDownLatch(1);
        CountDownLatch releasedLatch = new CountDownLatch(1);

        // This appender blocks completely, meaning the queue will stop draining
        Appender blockingAppender = (level, message) -> {
            try {
                // First event comes here and waits. Worker thread is now blocked.
                hangLatch.await(5, TimeUnit.SECONDS); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                releasedLatch.countDown();
            }
        };

        AsyncAppender asyncAppender = new AsyncAppender(blockingAppender);

        // Put 1 event, which gets taken by the background thread, so queue is empty but thread is blocked.
        asyncAppender.append(Loglevel.INFO, "Initial blocking message");

        // Now queue (capacity 50) is free to fill up.
        for (int i = 0; i < 50; i++) {
            asyncAppender.append(Loglevel.INFO, "Filler " + i);
        }

        // Now queue is full (50 elements inside). The next append should fail and print to System.err
        asyncAppender.append(Loglevel.ERROR, "Overflow message");

        assertTrue(errStreamCaptor.toString().contains("Log Queue is full!"));

        // cleanup
        hangLatch.countDown();
        releasedLatch.await(1, TimeUnit.SECONDS);
    }
}
