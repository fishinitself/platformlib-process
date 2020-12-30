package com.platformlib.process.configurator;

/**
 * Process instance output configurator.
 * Configure {@link com.platformlib.process.configuration.instance.ProcessInstanceOutputConfiguration}.
 */
public interface ProcessInstanceOutputConfigurator extends ProcessOutputConfigurator {
    /**
     * Limit process output size which will be kept.
     * @param limit limit in number of lines
     */
    void limit(int limit);
}
