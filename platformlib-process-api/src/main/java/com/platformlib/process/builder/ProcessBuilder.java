package com.platformlib.process.builder;

import com.platformlib.process.configurator.ProcessDryRunConfigurator;
import com.platformlib.process.configurator.ProcessInstanceConfigurator;
import com.platformlib.process.configurator.ProcessLoggerConfigurator;
import com.platformlib.process.executor.ProcessExecutor;
import com.platformlib.process.handler.ProcessDestroyerHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Process builder.
 */
public interface ProcessBuilder {
    String EXE_EXTENSION        = "exe";
    String BAT_EXTENSION        = "bat";
    String CMD_EXTENSION        = "cmd";
    String BASH_EXTENSION       = "bash";
    String SH_EXTENSION         = "sh";

    /**
     * Set process executor formal name.
     * Default value is current thread name.
     * The name will be added as suffix to any started threads.
     * @param name executor name
     * @return Returns this process configurator
     */
    ProcessBuilder name(String name);

    /**
     * Add default mapping command extensions.
     * The full path command extension will be automatically detected in next order:
     * For Windows platform: cmd -&gt; bat -&gt; exe
     * For Unix platform: no extension -&gt; sh -&gt; bash
     * @return process builder
     */
    ProcessBuilder defaultExtensionMapping();

    /**
     * Add {@link #BASH_EXTENSION} extension mapping if process is executed on NIX platform.
     * @return Returns this process configurator
     */
    ProcessBuilder mapBashExtension();

    /**
     * Add {@link #SH_EXTENSION} extension mapping if process is executed on NIX platform.
     * @return Returns this process configurator
     */
    ProcessBuilder mapShExtension();

    /**
     * Add {@link #BAT_EXTENSION} extension mapping if process is executed on WINDOWS platform.
     * @return Returns this process configurator
     */
    ProcessBuilder mapBatExtension();

    /**
     * Add {@link #CMD_EXTENSION} extension mapping if process is executed on WINDOWS platform.
     * @return Returns this process configurator
     */
    ProcessBuilder mapCmdExtension();

    /**
     * Add {@link #EXE_EXTENSION} extension mapping if process is executed on WINDOWS platform..
     * @return Returns this process configurator
     */
    ProcessBuilder mapExeExtension();

    /**
     * Configure extension supplier.
     * The configured supplier will be called if auto-detection is needed, e.g. when command extension depends on platform (on windows is .cmd extension, on NIX no extension).
     * @param supplier Mapper function, first parameter true if windows platform, false non windows
     * @return Returns this process builder
     */
    ProcessBuilder extensionSupplier(Supplier<String> supplier);

    /**
     * Set process standard output consumer.
     * @param stdOutConsumer process stdout consumer
     * @return Returns this process configurator
     */
    ProcessBuilder stdOutConsumer(Consumer<String> stdOutConsumer);

    /**
     * Set process error output consumer.
     * @param stdErrConsumer process error consumer
     * @return Returns this process configurator
     */
    ProcessBuilder stdErrConsumer(Consumer<String> stdErrConsumer);

    /**
     * Set process standard input stream.
     * @param processStandardInputStream stream to process standard input
     * @return Returns this process builder
     */
    ProcessBuilder standardInput(InputStream processStandardInputStream);

    /**
     * Set command to execute.
     * @param command command to execute
     * @return Returns this process builder
     */
    ProcessBuilder command(String command);

    /**
     * Set command and arguments
     * @param commandAndArguments command and arguments to execute
     * @return Returns this process builder
     */
    ProcessBuilder commandAndArguments(Object... commandAndArguments);

    /**
     * Set work directory for process executing.
     * @param workDirectory work directory where the process should be started
     * @return Returns this process builder
     */
    ProcessBuilder workDirectory(String workDirectory);

    /**
     * Set work directory for process executing.
     * @param workDirectory work directory where the process should be started
     * @return Returns this process builder
     */
    ProcessBuilder workDirectory(Path workDirectory);

    /**
     * Execute command in work directory.
     * For UNIX like OS it means adding ./ to command.
     *
     * @return Returns this process builder
     */
    ProcessBuilder executeInWorkDirectory();

    /**
     * Add env variables.
     * @param envVariables env variables
     * @return Returns this process builder
     */
    ProcessBuilder envVariables(Map<String, String> envVariables);

    /**
     * Set java process executor.
     * @param executor executor
     * @return Returns this process builder
     */
    ProcessBuilder withExecutor(Executor executor);

    /**
     * Set execution timeout.
     * By default unlimited.
     * @param timeout command execution timeout
     * @return Returns this process builder
     */
    ProcessBuilder executionTimeout(Duration timeout);

    /**
     * Add mask applier for masking sensitive data before putting it in logs and passing to output consumers.
     * @param maskApplier mask applier
     * @return Returns this process builder
     */
    ProcessBuilder addMaskApplier(Function<String, String> maskApplier);

    /**
     * Redirect process standard output to given stream.
     * @param outputStream output stream to redirect
     * @return Returns this process builder
     */
    ProcessBuilder redirectStandardOutput(OutputStream outputStream);

    /**
     * Redirect process standard error to given stream.
     * @param outputStream output stream to redirect
     * @return Returns this process builder
     */
    ProcessBuilder redirectStandardError(OutputStream outputStream);

    /**
     * Run command as is.
     * Some features such as setting environment variables could be disabled. Depends on platform and implementation.
     * @return Returns this process builder
     */
    ProcessBuilder rawExecution();

    /**
     * Specify destroy/kill process handler.
     * @param processDestroyerHandler process destroy/kill handler.
     * @return Returns this process builder
     */
    ProcessBuilder processDestroyerHandler(ProcessDestroyerHandler processDestroyerHandler);

    /**
     * Configure dry run mode.
     * @param processDryRunConfigurator dry run configurator
     * @return Returns this process builder
     */
    ProcessBuilder dryRun(Consumer<ProcessDryRunConfigurator> processDryRunConfigurator);

    /**
     * Configure process logger configuration.
     * This configuration responds for what should be put in log (command line, process output and etc).
     * @param processLoggerConfigurator process logger configurator
     * @return Returns this process builder
     */
    ProcessBuilder logger(Consumer<ProcessLoggerConfigurator> processLoggerConfigurator);

    /**
     * Configure process instance.
     * Responds what should be included in {@link com.platformlib.process.api.ProcessInstance} which is result of process execution.
     * @param processInstanceConfigurator process execution result configuration
     * @return Returns this process builder
     */
    ProcessBuilder processInstance(Consumer<ProcessInstanceConfigurator> processInstanceConfigurator);

    /**
     * Build process executor.
     * @return Returns process executor
     */
    ProcessExecutor build();
}
