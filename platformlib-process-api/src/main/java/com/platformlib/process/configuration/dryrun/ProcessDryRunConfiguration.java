package com.platformlib.process.configuration.dryrun;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Process dry run configuration.
 */
public interface ProcessDryRunConfiguration {
    /**
     * Should process be failed on startup or not.
     * @return Returns true if the process should be marked as startup failed, false otherwise
     */
    boolean startFailure();

    /**
     * Process exit code.
     * Default value is 0 is not {@link #startFailure}.
     * Returns {@link Optional#empty()} if configured {#link startFailure}.
     * @return Returns process exit code if {@link #startFailure} not set, otherwise {@link Optional#empty()}
     */
    Optional<Integer> getExitCode();

    /**
     * Get stream supplier.
     * @return Returns stream supplier
     */
    Optional<Supplier<ProcessDryRunProcessStream>> getStreamSupplier();

    /**
     * Get command and arguments supplier.
     * If command and arguments should be processed, e.g. printed on console
     * @return Returns command and arguments supplier
     */
    Optional<Consumer<Collection<String>>> getCommandAndArgumentsSupplier();
}
