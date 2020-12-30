package com.platformlib.process.configuration.impl;

import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configuration.dryrun.ProcessDryRunProcessStream;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default {@link ProcessDryRunConfiguration} implementation.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class DefaultProcessDryRunConfiguration implements ProcessDryRunConfiguration {
    private boolean startFailure;
    private int exitCode;
    private Supplier<ProcessDryRunProcessStream> streamSupplier;
    private Consumer<Collection<String>> caaConsumer;

    @Override
    public boolean startFailure() {
        return startFailure;
    }

    @Override
    public Optional<Integer> getExitCode() {
        return startFailure ? Optional.empty() : Optional.of(exitCode);
    }

    @Override
    public Optional<Supplier<ProcessDryRunProcessStream>> getStreamSupplier() {
        return Optional.ofNullable(streamSupplier);
    }

    @Override
    public Optional<Consumer<Collection<String>>> getCommandAndArgumentsSupplier() {
        return Optional.ofNullable(caaConsumer);
    }

    /**
     * Configure startup failure.
     * @param startFailure true to fail process starting up, false otherwise
     */
    public void setStartFailure(final boolean startFailure) {
        this.startFailure = startFailure;
    }

    /**
     * Specify process exit code.
     * @param exitCode process exit code
     */
    public void setExitCode(final int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Set process streams supplier.
     * @param streamSupplier process streams supplier.
     */
    public void setStreamSupplier(final Supplier<ProcessDryRunProcessStream> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    /**
     * Set process command and arguments consumer.
     * All arguments to passed consumer are masked (no raw arguments)
     * Use this method when need to print command line and arguments which would be used.
     * @param caaConsumer command and argument consumer.
     */
    public void setCommandAndArgumentsConsumer(final Consumer<Collection<String>> caaConsumer) {
        this.caaConsumer = caaConsumer;
    }
}
