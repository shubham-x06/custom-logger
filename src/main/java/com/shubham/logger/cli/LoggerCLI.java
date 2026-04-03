package com.shubham.logger.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "logger", mixinStandardHelpOptions = true, version = "1.0",
        description = "High-performance CLI logger",
        subcommands = {
                ConfigCommand.class,
                LogCommand.class,
                TailCommand.class,
                DebugCommand.class
        })
public class LoggerCLI implements Runnable {

    @Override
    public void run() {
        // Automatically default to printing usage if no subcommands are provided
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LoggerCLI()).execute(args);
        System.exit(exitCode);
    }
}
