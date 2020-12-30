package com.platformlib.process.configuration.dryrun;

import com.platformlib.process.enums.ProcessStandardStream;

/**
 * Stream for dry configuration.
 * Responds data which should be put in specified stream.
 */
public interface ProcessDryRunProcessStream {
    /**
     * Dry run stream.
     * @return Returns dry run process stream.
     */
    ProcessStandardStream getStream();

    /**
     * Data to put in stream.
     * @return returns byte buffer to put to stream
     */
    byte[] getData();
}
