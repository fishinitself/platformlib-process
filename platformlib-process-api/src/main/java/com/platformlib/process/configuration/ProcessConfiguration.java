package com.platformlib.process.configuration;

import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configuration.instance.ProcessInstanceConfiguration;
import com.platformlib.process.configuration.logger.ProcessLoggerConfiguration;
import com.platformlib.process.handler.ProcessDestroyerHandler;
import com.platformlib.process.enums.ExecutionMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Process configuration.
 */
public interface ProcessConfiguration {
    /**
     * Get process name.
     * @return Returns process name if set, {@link Optional#empty()} otherwise
     */
    Optional<String> getName();

    /**
     * Get process standard out consumer.
     * @return Returns process standard out consumer if set, null otherwise
     */
    Optional<Consumer<String>> getStdOutConsumer();

    /**
     * Get process standard error consumer.
     * @return Returns process standard error consumer if set, null otherwise
     */
    Optional<Consumer<String>> getStdErrConsumer();

    /**
     * Get command and arguments to execute.
     * @return Returns command and arguments to execute.
     */
    Collection<Object> getCommandAndArguments();

    /**
     * Get mask appliers.
     * Used for masking data in logs.
     * @return Returns collections of mask appliers.
     */
    Collection<Function<String, String>> getMaskAppliers();

    /**
     * Get process work directory where the process should be started.
     * @return Returns process working directory if set, {@link Optional#empty()} otherwise
     */
    Optional<String> getWorkDirectory();

    /**
     * Get process environment variables.
     * @return Returns process environment variables
     */
    Map<String, String> getEnvVariables();

    /**
     * Get process executor.
     * @return Returns process executor, {@link Optional#empty()} otherwise
     */
    Optional<Executor> getExecutor();

    /**
     * Get process execution timeout.
     * @return Returns process execution timeout
     */
    Optional<Duration> getExecutionTimeout();

    /**
     * Get output streams to redirect process standard output stream.
     * @return Returns collections of stream to redirect process standard output
     */
    Collection<OutputStream> getStandardOutputRedirects();

    /**
     * Get output streams to redirect process standard error stream.
     * @return Returns collections of stream to redirect process standard error
     */
    Collection<OutputStream> getStandardErrorRedirects();

    /**
     * Get process execution mode.
     * @return Returns execution mode
     */
    ExecutionMode getExecution();

    /**
     * Get process standard input stream.
     * @return Returns process standard input stream if set, {@link Optional#empty()} otherwise
     */
    Optional<InputStream> getStdIn();

    /**
     * Get process destroy handler.
     * @return Process destroy handler if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessDestroyerHandler> getProcessDestroyerHandler();

    /**
     * Get dry run configuration.
     * @return Returns dry run configuration if set, {@link Optional#empty()} otherwise
     */
    Optional<ProcessDryRunConfiguration> getDryRunConfiguration();

    /**
     * Get process output configuration which will be available via {@link com.platformlib.process.api.ProcessInstance}.
     * @return Returns process instance configuration
     */
    Optional<ProcessInstanceConfiguration> getProcessInstanceConfiguration();

    /**
     * Get process logger configuration.
     * @return Returns process logger configuration
     */
    Optional<ProcessLoggerConfiguration> getProcessLoggerConfiguration();

    Collection<Supplier<String>> getExtensionMappers();

}
