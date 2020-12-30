package com.platformlib.process.configuration.instance;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;

import java.util.Optional;

/**
 * Process instance output configuration.
 */
public interface ProcessInstanceOutputConfiguration extends ProcessOutputConfiguration {
    /**
     * Limit process instance output.
     * -1 means to keep all process output.
     * @return Returns process instance output limit if set, {@link Optional#empty()} otherwise
     */
    Optional<Integer> getLimit();
}
