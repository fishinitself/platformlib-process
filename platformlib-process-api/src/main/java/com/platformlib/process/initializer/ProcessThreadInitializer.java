package com.platformlib.process.initializer;

import com.platformlib.process.enums.ProcessThreadType;

import java.util.function.BiConsumer;

/**
 * Process's thread initializer.
 */
public interface ProcessThreadInitializer extends BiConsumer<ProcessThreadType, String> {
}
