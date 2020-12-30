package com.platformlib.process.configurator;

import java.util.function.Consumer;

/**
 * Process instance configurator.
 * Configure {@link com.platformlib.process.configuration.instance.ProcessInstanceConfiguration}.
 */
public interface ProcessInstanceConfigurator extends ProcessInstanceOutputConfigurator {
    /**
     * Configure process standard output.
     * @param stdOutInstanceConfigurator process standard output configurator
     */
    void stdOut(Consumer<ProcessInstanceOutputConfigurator> stdOutInstanceConfigurator);

    /**
     * Configure process standard error.
     * @param stdErrInstanceConfigurator process standard error configurator
     */
    void stdErr(Consumer<ProcessInstanceOutputConfigurator> stdErrInstanceConfigurator);
}
