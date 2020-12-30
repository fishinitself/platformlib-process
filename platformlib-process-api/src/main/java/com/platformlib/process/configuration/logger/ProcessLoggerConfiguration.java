package com.platformlib.process.configuration.logger;

import java.util.Optional;

/**
 * Process logger configuration.
 */
public interface ProcessLoggerConfiguration extends ProcessThreadLoggerConfiguration {
    /**
     * Get process standard input stream's logger configuration.
     * @return Returns process standard input stream's logger configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessInputLoggerConfiguration> getLoggerStdInConfiguration();

    /**
     * Get process standard out stream's logger configuration.
     * @return Returns process standard out stream's logger configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessOutputLoggerConfiguration> getLoggerStdOutConfiguration();

    /**
     * Get process standard error stream's logger configuration.
     * @return Returns process standard error stream's logger configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessOutputLoggerConfiguration> getLoggerStdErrConfiguration();
}
