package com.platformlib.process.local.factory;

import com.platformlib.process.local.builder.LocalProcessBuilder;
import com.platformlib.process.local.impl.LocalProcessBuilderImpl;

/**
 * Local process builder factory.
 */
public final class LocalProcessBuilderFactory {

    private LocalProcessBuilderFactory() {
    }

    /**
     * Create new local process builder.
     * @return Returns created local process builder
     */
    public static LocalProcessBuilder newLocalProcessBuilder() {
        return new LocalProcessBuilderImpl();
    }

}
