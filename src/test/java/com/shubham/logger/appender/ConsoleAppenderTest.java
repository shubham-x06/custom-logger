package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleAppenderTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void testConsoleAppenderFormatsAndPrints() {
        Formatter mockFormatter = (level, msg, src) -> level + " [" + src + "]: " + msg;
        ConsoleAppender appender = new ConsoleAppender(mockFormatter);

        appender.append(Loglevel.INFO, "Test message", "test");

        // System.out.println appends line separator
        assertEquals("INFO [test]: Test message" + System.lineSeparator(), outputStreamCaptor.toString());
    }
}
