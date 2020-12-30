package com.platformlib.process.configurator;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;

/**
 * Process output configurator.
 * Configure {@link ProcessOutputConfiguration}.
 */
public interface ProcessOutputConfigurator {
    /**
     * Specify how many first process output lines will be kept.
     * @param limit process output head limit
     */
    void headLimit(int limit);

    /**
     * Specify how many last process output lines will be kept.
     * @param limit process output tail limit
     */
    void tailLimit(int limit);

    /**
     * Specify to keep all process output.
     */
    void unlimited();
}
