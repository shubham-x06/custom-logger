package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;

import java.io.FileWriter;
import java.io.IOException;

public class FileAppender implements Appender, AutoCloseable {

    private final Formatter formatter;
    private FileWriter writer;

    public FileAppender(String filePath, Formatter formatter) {
        this.formatter = formatter;
        try {
            this.writer = new FileWriter(filePath, true); // Append mode
        } catch (IOException e) {
            System.err.println("Failed to initialize FileAppender: " + e.getMessage());
        }
    }

    @Override
    public synchronized void append(Loglevel level, String message) {
        if (writer != null) {
            try {
                writer.write(formatter.format(level, message) + System.lineSeparator());
                writer.flush();
            } catch (IOException e) {
                System.err.println("Failed to write to file: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close FileWriter: " + e.getMessage());
            }
        }
    }
}