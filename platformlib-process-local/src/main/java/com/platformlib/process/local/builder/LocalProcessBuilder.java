package com.platformlib.process.local.builder;

import com.platformlib.process.builder.ProcessBuilder;

/**
 * Local process builder.
 */
public interface LocalProcessBuilder extends ProcessBuilder {
    /**
     * Use current java as command.
     * @return Returns this process builder
     */
    LocalProcessBuilder useCurrentJava();
}
