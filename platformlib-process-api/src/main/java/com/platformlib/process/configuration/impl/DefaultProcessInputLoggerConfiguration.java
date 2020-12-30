package com.platformlib.process.configuration.impl;

import com.platformlib.process.configuration.logger.ProcessInputLoggerConfiguration;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Default implementation {@link ProcessInputLoggerConfiguration}.
 */
public class DefaultProcessInputLoggerConfiguration implements ProcessInputLoggerConfiguration {
    private Logger logger;
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

    public void setProcessThreadInitializer(final ProcessThreadInitializer processThreadInitializer) {
        this.processThreadInitializer = processThreadInitializer;
    }
}
