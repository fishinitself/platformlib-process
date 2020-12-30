package com.platformlib.process.configuration.output;

import java.util.Optional;

/**
 * Process output configuration.
 */
public interface ProcessOutputConfiguration {
    /**
     * Size of head log.
     * How many output lines keep since start.
     * @return Returns number of lines to keep from start
     */
    Optional<Integer> getHeadSize();

    /**
     * Size of tail log.
     * @return Returns size of tail log
     */
    Optional<Integer> getTailSize();
}
