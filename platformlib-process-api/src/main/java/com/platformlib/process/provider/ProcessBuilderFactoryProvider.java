package com.platformlib.process.provider;

import com.platformlib.process.builder.ProcessBuilder;

public interface ProcessBuilderFactoryProvider<T extends ProcessBuilder> {
    boolean isSuitable(Object specification);
    T newProcessBuilder(Class<T> clazz);
    T newProcessBuilder(Object specification);
}
