package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration;
import com.platformlib.process.configurator.ProcessOutputLoggerConfigurator;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;

public class DefaultProcessOutputLoggerConfigurator extends DefaultProcessOutputConfigurator implements ProcessOutputLoggerConfigurator, ProcessOutputLoggerConfiguration {
    private Logger logger;
    private Duration logInterval;
    private ProcessThreadInitializer processThreadInitializer;

    @Override
    public Optional<Duration> getLogInterval() {
        return Optional.ofNullable(logInterval);
    }

    @Override
    public Optional<Logger> getLogger() {
        return Optional.ofNullable(logger);
    }

    @Override
    public Optional<ProcessThreadInitializer> getProcessThreadInitializer() {
        return Optional.ofNullable(processThreadInitializer);
    }

    @Override
    public void logger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void logInterval(final Duration logInterval) {
        this.logInterval = logInterval;
    }

    @Override
    public void processThreadInitializer(ProcessThreadInitializer processThreadInitializer) {
        this.processThreadInitializer = processThreadInitializer;
    }
}
