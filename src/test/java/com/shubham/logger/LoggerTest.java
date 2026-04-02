package com.shubham.logger;

import com.shubham.logger.appender.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggerTest {

    private Logger logger;
    private final List<String> capturedMessages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        logger = Logger.getInstance();
        logger.clearAppenders();
        capturedMessages.clear();

        Appender mockAppender = (level, message) -> capturedMessages.add(level + ": " + message);
        logger.addAppender(mockAppender);
    }

    @Test
    void testLoggerFiltering() {
        // Set logger level to INFO (Severity 20)
        logger.setLevel(Loglevel.INFO);

        // This should be filtered out
        logger.log(Loglevel.DEBUG, "This is a debug message");

        // These should be logged
        logger.log(Loglevel.INFO, "This is an info message");
        logger.log(Loglevel.ERROR, "This is an error message");

        assertEquals(2, capturedMessages.size());
        assertEquals("INFO: This is an info message", capturedMessages.get(0));
        assertEquals("ERROR: This is an error message", capturedMessages.get(1));
    }
}
