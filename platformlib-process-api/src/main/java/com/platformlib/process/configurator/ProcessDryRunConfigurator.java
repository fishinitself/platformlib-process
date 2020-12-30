package com.platformlib.process.configurator;


import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configuration.dryrun.ProcessDryRunProcessStream;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Process dry run configurator.
 * Configure {@link ProcessDryRunConfiguration}.
 */
public interface ProcessDryRunConfigurator  {
    /**
     * Do process startup failure.
     */
    void failProcessStartup();

    /**
     * Specify process exit code.
     * @param exitCode process exit code
     */
    void exitCode(int exitCode);

    /**
     * Set process streams suppliers.
     * The supplier will be requested until null returned.
     * @param streamSupplier process stream supplier
     */
    void streamSupplier(Supplier<ProcessDryRunProcessStream> streamSupplier);

    /**
     * St command and arguments.
     * @param commandAndArgumentSupplier command and argument supplier.
     */
    void commandAndArgumentsSupplier(Consumer<Collection<String>> commandAndArgumentSupplier);
}
