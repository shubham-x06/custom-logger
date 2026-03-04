package com.shubham.logger.appender;

import com.shubham.logger.Loglevel;
import com.shubham.logger.formatter.Formatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileAppender implements Appender {

    private final String filePath;
    private final Formatter formatter;

    private final long maxFileSize = 1024 * 1024; // 1 MB

    public FileAppender(String filePath, Formatter formatter) {
        this.filePath = filePath;
        this.formatter = formatter;
    }

    private void rotate() {
        File file = new File(filePath);

        if (file.exists() && file.length() > maxFileSize) {
            File rotated = new File(filePath + ".1");
            if (rotated.exists()) {
                rotated.delete();
            }
            file.renameTo(rotated);
        }
    }

    @Override
    public void append(Loglevel level, String message) {

        rotate();

        try (FileWriter fw = new FileWriter(filePath, true);
                PrintWriter pw = new PrintWriter(fw)) {

            pw.println(formatter.format(level, message));

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not write to log file");
        }
    }
}