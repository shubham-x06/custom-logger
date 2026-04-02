package com.shubham.logger.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Command(name = "tail", description = "Tail the last N lines of the log file")
public class TailCommand implements Runnable {

    @Option(names = {"--lines", "-n"}, defaultValue = "20", description = "Number of lines to show")
    private int lines;

    @Override
    public void run() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("logger-cli.properties")) {
            properties.load(in);
        } catch (IOException e) {
            // Ignore if missing, will use default
        }

        String logPath = properties.getProperty("logPath", "app.log");
        File file = new File(logPath);

        if (!file.exists()) {
            System.err.println("Log file not found: " + logPath);
            return;
        }

        try {
            List<String> lastLines = tailFile(file, lines);
            for (String line : lastLines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read log file: " + e.getMessage());
        }
    }

    private List<String> tailFile(File file, int linesToRead) throws IOException {
        List<String> result = new ArrayList<>();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();
            
            // Read from end to start
            for (long pointer = fileLength; pointer >= 0; pointer--) {
                randomAccessFile.seek(pointer);
                char c = (char) randomAccessFile.read();
                
                if (c == '\n') {
                    if (pointer != fileLength) {
                        result.add(sb.reverse().toString());
                        sb = new StringBuilder();
                        if (result.size() == linesToRead) {
                            break;
                        }
                    }
                } else if (c != '\r') {
                    sb.append(c);
                }
            }
            
            // If the buffer has content and we haven't reached quota yet
            if (sb.length() > 0 && result.size() < linesToRead) {
                result.add(sb.reverse().toString());
            }
        }
        
        Collections.reverse(result);
        return result;
    }
}
