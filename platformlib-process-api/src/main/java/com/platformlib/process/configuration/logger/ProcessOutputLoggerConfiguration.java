package com.platformlib.process.configuration.logger;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;

import java.time.Duration;
import java.util.Optional;

/**
 * Process output's logger configuration.
 */
public interface ProcessOutputLoggerConfiguration extends ProcessOutputConfiguration, ProcessThreadLoggerConfiguration {
    /**
     * Get log interval.
     * If set the output line will be put into log every specified duration. Lines
     * @return Returns log interval if set, {@link Optional#empty()} otherwise
     */
    Optional<Duration> getLogInterval();
}
