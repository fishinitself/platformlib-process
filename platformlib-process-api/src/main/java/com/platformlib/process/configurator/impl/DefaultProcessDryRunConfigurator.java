package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.dryrun.ProcessDryRunProcessStream;
import com.platformlib.process.configuration.impl.DefaultProcessDryRunConfiguration;
import com.platformlib.process.configurator.ProcessDryRunConfigurator;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default {@link ProcessDryRunConfigurator} implementation.
 */
public class DefaultProcessDryRunConfigurator extends DefaultProcessDryRunConfiguration implements ProcessDryRunConfigurator {

    @Override
    public void failProcessStartup() {
        setStartFailure(true);
    }

    @Override
    public void exitCode(final int exitCode) {
        setExitCode(exitCode);
    }

    @Override
    public void streamSupplier(final Supplier<ProcessDryRunProcessStream> streamSupplier) {
        setStreamSupplier(streamSupplier);
    }

    @Override
    public void commandAndArgumentsSupplier(final Consumer<Collection<String>> commandAndArgumentSupplier) {
        setCommandAndArgumentsConsumer(commandAndArgumentSupplier);
    }
}
