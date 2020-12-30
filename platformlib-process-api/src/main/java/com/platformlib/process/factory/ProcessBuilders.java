package com.platformlib.process.factory;

import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.provider.ProcessBuilderFactoryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class ProcessBuilders {
    private ProcessBuilders() {
    }

    @SuppressWarnings({"unchecked", "PMD.LawOfDemeter"})
    public static <T extends ProcessBuilder> T newProcessBuilder(final Object specification) {
        final ServiceLoader<ProcessBuilderFactoryProvider> providers = ServiceLoader.load(ProcessBuilderFactoryProvider.class);
        final List<ProcessBuilder> processBuilders = new ArrayList<>();
        providers.forEach(provider -> {
            if (provider.isSuitable(specification)) {
                processBuilders.add(provider.newProcessBuilder(specification));
            }
        });
        if (processBuilders.isEmpty()) {
            throw new IllegalStateException("No process builder provider has been found for " + specification);
        }
        if (processBuilders.size() > 1) {
            throw new IllegalStateException("Too many providers have been found for " + specification + ": " + processBuilders);
        }
        return (T) processBuilders.get(0);
    }
}
