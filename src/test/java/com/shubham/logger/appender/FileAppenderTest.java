package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileAppenderTest {

    @Test
    void testFileAppenderWritesToFile() throws IOException {
        Path tempFile = Files.createTempFile("testLog", ".txt");
        Formatter mockFormatter = (level, msg, src) -> level + " [" + src + "] - " + msg;

        try (FileAppender appender = new FileAppender(tempFile.toString(), mockFormatter)) {
            appender.append(Loglevel.DEBUG, "Debug log", "test");
            appender.append(Loglevel.ERROR, "Error log", "test");
        } 

        // File is flushed and written properly inside close() (or during each append).
        String content = Files.readString(tempFile);
        
        assertTrue(content.contains("DEBUG [test] - Debug log"));
        assertTrue(content.contains("ERROR [test] - Error log"));

        Files.delete(tempFile);
    }
}
