package com.platformlib.process.core;

import com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration;

import java.util.Objects;
import java.util.function.Consumer;

public class ProcessOutputLogger implements Consumer<String> {
    private final ProcessOutputLoggerConfiguration processLoggerConfiguration;

    public ProcessOutputLogger(final ProcessOutputLoggerConfiguration processLoggerConfiguration) {
        this.processLoggerConfiguration = Objects.requireNonNull(processLoggerConfiguration);
    }

    @Override
    public void accept(final String s) {
        //TODO Implement
    }
}
