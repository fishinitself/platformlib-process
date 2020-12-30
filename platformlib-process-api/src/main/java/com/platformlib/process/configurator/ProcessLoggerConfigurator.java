package com.platformlib.process.configurator;

import com.platformlib.process.initializer.ProcessThreadInitializer;

import java.util.function.Consumer;

/**
 * Process logger configurator.
 * Configure {@link com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration}.
 */
public interface ProcessLoggerConfigurator extends ProcessOutputLoggerConfigurator {
    /**
     * Configure process standard out stream logging.
     * @param stdInLoggerConfigurator process standard input stream logger configurator
     */
    void stdIn(Consumer<ProcessInputLoggerConfigurator> stdInLoggerConfigurator);

    /**
     * Configure process standard out stream logging.
     * @param stdOutLoggerConfigurator process standard out stream logger configurator
     */
    void stdOut(Consumer<ProcessOutputLoggerConfigurator> stdOutLoggerConfigurator);

    /**
     * Configure process standard error stream logging.
     * @param stdErrConfigurator process standard error stream logger configurator
     */
    void stdErr(Consumer<ProcessOutputLoggerConfigurator> stdErrConfigurator);

    /**
     * Specify process thread consumer.
     * The consumer will be called after each thread starting required for process execution.
     * @param onProcessThreadStartPayload payload to execute after thread starting up. Accepts thread type and execution identifier
     */
    void onProcessThreadStart(ProcessThreadInitializer onProcessThreadStartPayload);
}
