package com.shubham.logger;

public enum Loglevel {
    DEBUG(10),
    INFO(20),
    ERROR(30);

    private final int severity;

    Loglevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }
}