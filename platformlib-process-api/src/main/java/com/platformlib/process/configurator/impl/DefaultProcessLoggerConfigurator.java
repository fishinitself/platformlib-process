package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.logger.ProcessInputLoggerConfiguration;
import com.platformlib.process.configuration.logger.ProcessLoggerConfiguration;
import com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration;
import com.platformlib.process.configurator.ProcessInputLoggerConfigurator;
import com.platformlib.process.configurator.ProcessLoggerConfigurator;
import com.platformlib.process.configurator.ProcessOutputLoggerConfigurator;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;

public class DefaultProcessLoggerConfigurator extends DefaultProcessOutputLoggerConfigurator implements ProcessLoggerConfigurator, ProcessLoggerConfiguration {
    private ProcessThreadInitializer processThreadInitializer;
    private DefaultProcessInputLoggerConfigurator stdInLoggerConfigurator = new DefaultProcessInputLoggerConfigurator();
    //
    private DefaultProcessOutputLoggerConfigurator stdOutLoggerConfigurator = new DefaultProcessOutputLoggerConfigurator();
    private DefaultProcessOutputLoggerConfigurator stdErrLoggerConfigurator = new DefaultProcessOutputLoggerConfigurator();

    @Override
    public void logger(final Logger logger) {
        super.logger(logger);
        if (!stdOutLoggerConfigurator.getLogger().isPresent()) {
            stdOutLoggerConfigurator.logger(logger);
        }
        if (!stdErrLoggerConfigurator.getLogger().isPresent()) {
            stdErrLoggerConfigurator.logger(logger);
        }
        if (!stdInLoggerConfigurator.getLogger().isPresent()) {
            stdInLoggerConfigurator.logger(logger);
        }
    }

    @Override
    public Optional<ProcessInputLoggerConfiguration> getLoggerStdInConfiguration() {
        return Optional.of(stdInLoggerConfigurator);
    }

    @Override
    public Optional<ProcessOutputLoggerConfiguration> getLoggerStdOutConfiguration() {
        return Optional.ofNullable(stdOutLoggerConfigurator);
    }

    @Override
    public Optional<ProcessOutputLoggerConfiguration> getLoggerStdErrConfiguration() {
        return Optional.ofNullable(stdErrLoggerConfigurator);
    }

    @Override
    public Optional<ProcessThreadInitializer> getProcessThreadInitializer() {
        return Optional.ofNullable(processThreadInitializer);
    }

    @Override
    public void stdIn(final Consumer<ProcessInputLoggerConfigurator> stdInLoggerConfigurator) {

    }

    @Override
    public void stdOut(final Consumer<ProcessOutputLoggerConfigurator> stdOutLoggerConfigurator) {
        stdOutLoggerConfigurator.accept(this.stdOutLoggerConfigurator);
    }

    @Override
    public void stdErr(final Consumer<ProcessOutputLoggerConfigurator> stdErrLoggerConfigurator) {
        stdErrLoggerConfigurator.accept(this.stdErrLoggerConfigurator);
    }

    @Override
    public void onProcessThreadStart(final ProcessThreadInitializer onProcessThreadStartPayload) {
        this.processThreadInitializer = onProcessThreadStartPayload;
        if (!stdOutLoggerConfigurator.getProcessThreadInitializer().isPresent()) {
            stdOutLoggerConfigurator.processThreadInitializer(onProcessThreadStartPayload);
        }
        if (!stdErrLoggerConfigurator.getProcessThreadInitializer().isPresent()) {
            stdErrLoggerConfigurator.processThreadInitializer(onProcessThreadStartPayload);
        }
    }
}
