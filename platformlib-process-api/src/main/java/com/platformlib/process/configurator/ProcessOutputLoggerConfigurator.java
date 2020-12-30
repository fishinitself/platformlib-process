package com.platformlib.process.configurator;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;
import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Process output logger configurator.
 * Configure {@link ProcessOutputConfiguration}.
 */
public interface ProcessOutputLoggerConfigurator extends ProcessOutputConfigurator {
    /**
     * Specify logger to use.
     * @param logger logger to use
     */
    void logger(Logger logger);

    /**
     * Specify log interval to put process output to log.
     * @param logInterval interval to put output to log
     */
    void logInterval(Duration logInterval);

    /**
     * Thread initializer.
     * Should be used for logger configuration.
     * @param processThreadInitializer process thread initializer
     */
    void processThreadInitializer(ProcessThreadInitializer processThreadInitializer);
}
