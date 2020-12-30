package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.logger.ProcessInputLoggerConfiguration;
import com.platformlib.process.configurator.ProcessInputLoggerConfigurator;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.util.Optional;

public class DefaultProcessInputLoggerConfigurator implements ProcessInputLoggerConfigurator, ProcessInputLoggerConfiguration {
    private Logger logger;
    private ProcessThreadInitializer processThreadInitializer;

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
    public void processThreadInitializer(ProcessThreadInitializer processThreadInitializer) {
        this.processThreadInitializer = processThreadInitializer;
    }
}
