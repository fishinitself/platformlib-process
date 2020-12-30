package com.platformlib.process.configuration.impl;

import com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;

/**
 * Default implementation {@link ProcessOutputLoggerConfiguration}.
 */
public class DefaultProcessOutputLoggerConfiguration extends DefaultProcessOutputConfiguration implements ProcessOutputLoggerConfiguration {
    private Logger logger;
    private Duration logInterval;
    private ProcessThreadInitializer processThreadInitializer;

    public void setLogger(final Logger logger) {
        this.logger = logger;
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
    public Optional<Duration> getLogInterval() {
        return Optional.ofNullable(logInterval);
    }

    public void setLogInterval(final Duration logInterval) {
        this.logInterval = logInterval;
    }

    public void setProcessThreadInitializer(final ProcessThreadInitializer processThreadInitializer) {
        this.processThreadInitializer = processThreadInitializer;
    }
}
