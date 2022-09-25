package com.platformlib.process.initializer;

import com.platformlib.process.enums.ProcessThreadType;

import java.util.function.Consumer;

/**
 * Process's thread initializer.
 */
public interface ProcessThreadInitializer extends Consumer<ProcessThreadType> {
}
