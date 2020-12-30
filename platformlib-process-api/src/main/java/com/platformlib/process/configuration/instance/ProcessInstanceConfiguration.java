package com.platformlib.process.configuration.instance;

import java.util.Optional;

/**
 * Process {@link com.platformlib.process.api.ProcessInstance} configuration.
 */
public interface ProcessInstanceConfiguration {
    /**
     * Get process instance standard out configuration.
     * @return Return process instance standard out configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessInstanceOutputConfiguration> getStdOutConfiguration();

    /**
     * Get process instance standard error configuration.
     * @return Return process instance standard error configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessInstanceOutputConfiguration> getStdErrConfiguration();
}
