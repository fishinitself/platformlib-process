package com.platformlib.process.configurator;

import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

/**
 * Process input configurator.
 */
public interface ProcessInputLoggerConfigurator {
    /**
     * Specify logger.
     * @param logger logger
     */
    void logger(Logger logger);

    /**
     * Specify process thread initializer.
     * Initializer is called on thread startup.
     * @param processThreadInitializer process thread initializer.
     */
    void processThreadInitializer(ProcessThreadInitializer processThreadInitializer);
}
