package com.platformlib.process.enums;

public enum ProcessThreadType {
    STDOUT_LISTENER("stdout"),
    STDERR_LISTENER("stderr"),
    STDIN_LISTENER("stdin");

    private final String threadName;

    ProcessThreadType(final String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }
}
