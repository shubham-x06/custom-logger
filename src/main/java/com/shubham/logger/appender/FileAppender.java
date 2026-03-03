package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileAppender implements Appender {

    private final String filePath;
    private final Formatter formatter;

    public FileAppender(String filePath, Formatter formatter) {
        this.filePath = filePath;
        this.formatter = formatter;
    }

    @Override
    public void append(Loglevel level, String message) {
        try (FileWriter fw = new FileWriter(filePath, true);
                PrintWriter pw = new PrintWriter(fw)) {

            pw.println(formatter.format(level, message));

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not write to log file");
        }
    }
}