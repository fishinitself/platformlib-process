package com.platformlib.process.configuration.logger;

import com.platformlib.process.initializer.ProcessThreadInitializer;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Process thread's logger configuration.
 */
public interface ProcessThreadLoggerConfiguration {
    /**
     * Get logger.
     * @return Returns logger
     */
    Optional<Logger> getLogger();

    /**
     * Get thread initialize payload.
     * @return Returns thread initializer
     */
    Optional<ProcessThreadInitializer> getProcessThreadInitializer();
}
