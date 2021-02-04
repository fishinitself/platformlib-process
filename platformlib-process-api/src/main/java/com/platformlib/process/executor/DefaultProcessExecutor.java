package com.platformlib.process.executor;

import com.platformlib.process.configuration.ProcessConfiguration;
import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configuration.instance.ProcessInstanceConfiguration;
import com.platformlib.process.configuration.logger.ProcessThreadLoggerConfiguration;
import com.platformlib.process.core.DefaultProcessOutput;
import com.platformlib.process.handler.ProcessDestroyerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default process executor.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public abstract class DefaultProcessExecutor implements ProcessExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProcessExecutor.class);
    private static final AtomicLong EXECUTION_COUNTER = new AtomicLong(0L);
    private final long executionId = EXECUTION_COUNTER.incrementAndGet();

    private final ProcessConfiguration processConfiguration;
    private final ExecutorService executorService;
    private PipedInputStream stdOutPipedInputStream = null;
    private PipedOutputStream stdOutPipedOutputStream = null;

    private PipedInputStream stdErrPipedInputStream = null;
    private PipedOutputStream stdErrPipedOutputStream = null;

    private final DefaultProcessOutput processStdOut;
    private final DefaultProcessOutput processStdErr;

    /**
     * Default constructor.
     * @param processConfiguration process configuration
     */
    public DefaultProcessExecutor(final ProcessConfiguration processConfiguration) {
        //TODO Make it thread safe. Do not store processConfiguration, extract all necessary configuration properties
        this.processConfiguration = processConfiguration;
        this.executorService = processConfiguration.getExecutor().isPresent() ? null : Executors.newCachedThreadPool(r -> {
                final Thread thread = new Thread(r, "process-exec-" + (processConfiguration.getName().map(name -> name + "-" + executionId) .orElseGet(() -> String.valueOf(executionId))));
                thread.setDaemon(true);
                return thread;
            });
        processStdOut = new DefaultProcessOutput(
                processConfiguration.getStandardOutputRedirects(),
                processConfiguration.getStdOutConsumer().orElse(null),
                processConfiguration.getProcessInstanceConfiguration().flatMap(ProcessInstanceConfiguration::getStdOutConfiguration).orElse(null));
        processStdErr = new DefaultProcessOutput(
                processConfiguration.getStandardErrorRedirects(),
                processConfiguration.getStdErrConsumer().orElse(null),
                processConfiguration.getProcessInstanceConfiguration().flatMap(ProcessInstanceConfiguration::getStdErrConfiguration).orElse(null));
    }

    @Override
    public synchronized InputStream getStdOutInputStream() {
        if (stdOutPipedInputStream == null) {
            stdOutPipedInputStream = new PipedInputStream();
            stdOutPipedOutputStream = new PipedOutputStream();
            try {
                stdOutPipedOutputStream.connect(stdOutPipedInputStream);
            } catch (final IOException ioException) {
                throw new IllegalStateException("Fail to create piped input stream for process stdout", ioException);
            }
        }
        return stdOutPipedInputStream;
    }

    @Override
    public synchronized InputStream getStdErrInputStream() {
        if (stdErrPipedInputStream == null) {
            stdErrPipedInputStream = new PipedInputStream();
            stdErrPipedOutputStream = new PipedOutputStream();
            try {
                stdErrPipedOutputStream.connect(stdErrPipedInputStream);
            } catch (final IOException ioException) {
                throw new IllegalStateException("Fail to create piped input stream for process stderr", ioException);
            }
        }
        return stdErrPipedInputStream;
    }

    protected Executor getExecutor() {
        return processConfiguration.getExecutor().orElse(executorService);
    }

    protected Optional<Logger> getLogger() {
        return processConfiguration.getProcessLoggerConfiguration().flatMap(ProcessThreadLoggerConfiguration::getLogger);
    }

    public long getExecutionId() {
        return executionId;
    }

    protected Optional<InputStream> getStdIn() {
        return processConfiguration.getStdIn();
    }

    protected Map<String, String> getEnvVariables() {
        return processConfiguration.getEnvVariables();
    }

    protected Optional<String> getWorkDirectory() {
        return processConfiguration.getWorkDirectory();
    }

    protected ProcessDestroyerHandler getProcessDestroyerHandler() {
        return processConfiguration.getProcessDestroyerHandler().orElse(null);
    }

    protected Optional<ProcessDryRunConfiguration> getDryRunConfiguration() {
        return processConfiguration.getDryRunConfiguration();
    }

    /**
     * Get command and argument to execute.
     * The command could be changed because of configuration, e.g. extension auto detection
     * @param fileSystem file system where the command should be executed
     * @param commandLineAndArguments command and arguments to execute
     * @return Returns unmasked command line and argument to execute
     */
    protected List<String> getCommandAndArgumentsToExecute(final FileSystem fileSystem, final Collection<String> commandLineAndArguments) {
        final List<String> cla = new ArrayList<>(commandLineAndArguments);
        final String command = cla.get(0);
        if (!command.contains("/") && !command.contains("\\")) {
            return cla;
        }
        for (final Supplier<String> extensionSupplier: processConfiguration.getExtensionMappers()) {
            final String extension = extensionSupplier.get();
            if (extension == null) {
                continue;
            }
            if (Files.isRegularFile(fileSystem.getPath(command + "." + extension))) {
                LOGGER.debug("Map extension {} provided by {}", extension, extensionSupplier);
                cla.set(0, command + "." + extension);
                break;
            }
        }
        return cla;
    }

    /**
     * Get command line and arguments to execute.
     * Contains unmasked values.
     * @param commandAndArguments command and arguments to execute. If process is already configured with command line/arguments then given arguments will be added to the command arguments
     * @return Returns unmasked command line and argument to execute
     */
    protected List<String> getUnmaskedCommandAndArguments(final Object... commandAndArguments) {
        final List<Object> caa = new ArrayList<>(processConfiguration.getCommandAndArguments());
        Collections.addAll(caa, commandAndArguments);
        //TODO Apply command argument function to pass unmasked argument
        return caa.stream().map(Object::toString).collect(Collectors.toList());
    }

    protected List<String> getMaskedCommandAndArguments(final Object... commandAndArguments) {
        final List<Object> caa = new ArrayList<>(processConfiguration.getCommandAndArguments());
        Collections.addAll(caa, commandAndArguments);
        //TODO Apply logger argument function if present to each argument to prevent log sensitive information
        return caa.stream().map(Object::toString).collect(Collectors.toList());
    }

    protected void dumpCommandAndArguments(final Object... commandAndArguments) {
        if (!LOGGER.isTraceEnabled() && !getLogger().orElse(LOGGER).isDebugEnabled()) {
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            final List<String> unmaskedCommandAndArguments = getUnmaskedCommandAndArguments(commandAndArguments);
            LOGGER.trace("Start [{}] the local OS process {}", getExecutionId(), unmaskedCommandAndArguments);
        }
        if (!getLogger().isPresent() && LOGGER.isTraceEnabled()) {
            return;
        }
        final Logger logger = getLogger().orElse(LOGGER);
        if (logger.isDebugEnabled()) {
            final List<String> maskedCommandAndArguments = getMaskedCommandAndArguments(commandAndArguments);
            logger.debug("Start [{}] the local OS process {}", getExecutionId(), maskedCommandAndArguments);
        }
    }

    protected DefaultProcessOutput getProcessStdOut() {
        return processStdOut;
    }

    protected DefaultProcessOutput getProcessStdErr() {
        return processStdErr;
    }

    protected void close() {
        processConfiguration.getStdIn().ifPresent(this::closeResource);
        closeResource(stdErrPipedOutputStream);
        closeResource(stdErrPipedInputStream);
        closeResource(stdOutPipedOutputStream);
        closeResource(stdOutPipedInputStream);
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    protected void closeResource(final Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (final Exception exception) {
            getLogger().orElse(LOGGER).warn("An error on closing piped stream", exception);
        }
    }
}
