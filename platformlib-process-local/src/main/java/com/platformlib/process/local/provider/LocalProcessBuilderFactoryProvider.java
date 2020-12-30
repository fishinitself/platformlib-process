package com.platformlib.process.local.provider;

import com.platformlib.process.local.factory.LocalProcessBuilderFactory;
import com.platformlib.process.provider.ProcessBuilderFactoryProvider;
import com.platformlib.process.local.builder.LocalProcessBuilder;
import com.platformlib.process.local.specification.LocalProcessSpec;

/**
 * Local OS process builder factory provider.
 */
public final class LocalProcessBuilderFactoryProvider implements ProcessBuilderFactoryProvider<LocalProcessBuilder> {
    @Override
    public boolean isSuitable(final Object specification) {
        return specification == null
                || specification instanceof LocalProcessSpec
                || specification instanceof Class && LocalProcessBuilder.class.isAssignableFrom((Class<?>) specification);
    }

    @Override
    public LocalProcessBuilder newProcessBuilder(final Class<LocalProcessBuilder> clazz) {
        return LocalProcessBuilderFactory.newLocalProcessBuilder();
    }

    @Override
    public LocalProcessBuilder newProcessBuilder(final Object specification) {
        final LocalProcessBuilder localProcessBuilder = newProcessBuilder(LocalProcessBuilder.class);
        if (specification instanceof LocalProcessSpec && ((LocalProcessSpec) specification).isUseCurrentJava()) {
            localProcessBuilder.useCurrentJava();
        }
        return localProcessBuilder;
    }
}
