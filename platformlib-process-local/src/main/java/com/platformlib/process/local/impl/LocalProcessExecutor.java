package com.platformlib.process.local.impl;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.configuration.ProcessConfiguration;
import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configuration.logger.ProcessLoggerConfiguration;
import com.platformlib.process.core.AsyncProcessOutputListener;
import com.platformlib.process.core.DefaultProcessInstance;
import com.platformlib.process.executor.DefaultProcessExecutor;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Local process executor.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class LocalProcessExecutor extends DefaultProcessExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProcessExecutor.class);
    private static final boolean PID_METHOD = Arrays.stream(Process.class.getMethods()).anyMatch(method -> "pid".equals(method.getName()));
    private static final int STDIN_BUFFER_SIZE = 128;
    private final AsyncProcessOutputListener stdOutListener;
    private final AsyncProcessOutputListener stdErrListener;

    /**
     * Default constructor.
     *
     * @param processConfiguration process configuration
     */
    public LocalProcessExecutor(final ProcessConfiguration processConfiguration) {
        super(processConfiguration);
        stdOutListener = new AsyncProcessOutputListener(
                getExecutor(),
                "stdout",
                processConfiguration.getProcessLoggerConfiguration().flatMap(ProcessLoggerConfiguration::getLoggerStdOutConfiguration).orElse(null),
                getProcessStdOut());
        stdErrListener = new AsyncProcessOutputListener(
                getExecutor(),
                "stderr",
                processConfiguration.getProcessLoggerConfiguration().flatMap(ProcessLoggerConfiguration::getLoggerStdErrConfiguration).orElse(null),
                getProcessStdErr());
    }

    @SuppressWarnings({"PMD.ConfusingTernary", "PMD.AvoidCatchingThrowable"})
    @Override
    public OperationSystemProcess execute(final Object... commandAndArguments) {
        final ProcessBuilder processBuilder = new ProcessBuilder(getUnmaskedCommandAndArguments(commandAndArguments));
        getWorkDirectory().ifPresent(workDirectory -> processBuilder.directory(Paths.get(workDirectory).toFile()));
        if (!getEnvVariables().isEmpty()) {
            processBuilder.environment().putAll(getEnvVariables());
        }
        final LocalOperationSystemProcess operationSystemProcess = new LocalOperationSystemProcess(getProcessDestroyerHandler());
        try {
            dumpCommandAndArguments(commandAndArguments);
            final Process process;
            if (getDryRunConfiguration().isPresent()) {
                getLogger().orElse(LOGGER).debug("Dry run process");
                process = new DryRunProcess(getDryRunConfiguration().get());
                getDryRunConfiguration().flatMap(ProcessDryRunConfiguration::getCommandAndArgumentsSupplier).ifPresent(commandAndArgumentsSupplier -> commandAndArgumentsSupplier.accept(getMaskedCommandAndArguments(commandAndArguments)));
            } else {
                process = processBuilder.start();
            }
            operationSystemProcess.setProcess(process);
            final Integer pid = getProcessId(process);
            operationSystemProcess.setPid(pid);
            if (pid != null) {
                getLogger().orElse(LOGGER).debug("PID [{}] is {}", getExecutionId(), pid);
            }
            stdOutListener.startListening(process.getInputStream());
            stdErrListener.startListening(process.getErrorStream());

            getExecutor().execute(() -> {
                Throwable processExecException = null;
                Integer exitCode = null;
                try {
                    getStdIn().ifPresent(stdIn ->
                            getExecutor().execute(() -> {
                                getLogger().orElse(LOGGER).trace("Start asynchronous writing to process standard input");
                                final byte[] buffer = new byte[STDIN_BUFFER_SIZE];
                                int len;
                                try {
                                    while ((len = stdIn.read(buffer)) != -1) {
                                        getLogger().orElse(LOGGER).trace("Write to process standard input {}", buffer);
                                        process.getOutputStream().write(buffer, 0, len);
                                        process.getOutputStream().flush();
                                    }
                                } catch (final IOException ioException) {
                                    getLogger().orElse(LOGGER).error("Unable to write to process standard input. Stop writing...", ioException);
                                }
                            })
                    );
                    exitCode = process.waitFor();
                    getLogger().orElse(LOGGER).debug("The local OS process [{}] has been finished with exit code {}", getExecutionId(), exitCode);
                } catch (final Throwable throwable) {
                    processExecException = throwable;
                    getLogger().orElse(LOGGER).debug("The local OS process [" + getExecutionId() + "] threw unexpected exception", throwable);
                }
                close();
                if (processExecException != null) {
                    operationSystemProcess.completeExceptionally(processExecException);
                } else if (operationSystemProcess.isCancelled()) {
                    operationSystemProcess.completeExceptionally(new InterruptedException("The os process execution [" + getExecutionId() + "] has been cancelled"));
                } else if (exitCode == null) {
                    operationSystemProcess.completeExceptionally(new IllegalStateException("The os process exit code hasn't been gotten"));
                } else {
                    operationSystemProcess.complete(new DefaultProcessInstance(exitCode, getProcessStdOut(), getProcessStdErr()));
                }
            });
        } catch (final IOException ioException) {
            getLogger().orElse(LOGGER).error("Fail to start process", ioException);
            operationSystemProcess.completeExceptionally(ioException);
            close();
        } catch (final Throwable exception) {
            getLogger().orElse(LOGGER).error("Unexpected error", exception);
            operationSystemProcess.completeExceptionally(exception);
            close();
        }
        return operationSystemProcess;
    }

    @Override
    protected void close() {
        closeResource(stdOutListener);
        closeResource(stdErrListener);
        super.close();
    }

    private static boolean isWindowsBasedOs() {
        return ';' == File.pathSeparatorChar;
    }

    @SuppressWarnings({"PMD.OnlyOneReturn", "PMD.AvoidThrowingRawExceptionTypes", "PMD.AvoidCatchingGenericException"})
    private Integer getProcessId(final Process process) {
        if (process instanceof DryRunProcess) {
            return -1;
        }
        if (PID_METHOD) {
            try {
                final Method method = Process.class.getMethod("pid");
                return ((Long) method.invoke(process)).intValue();
            } catch (final Exception exception) {
                getLogger().orElse(LOGGER).warn("A problem with getting process PID", exception);
            }
        }
        try {
            if (isWindowsBasedOs()) {
                final Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                final long handle = field.getLong(process);
                getLogger().orElse(LOGGER).trace("Process handler {}", handle);
                final int pid = Kernel32.INSTANCE.GetProcessId(new WinNT.HANDLE(new Pointer(handle)));
                if (pid == 0) {
                    if (getLogger().orElse(LOGGER).isTraceEnabled()) {
                        final int lastError = Kernel32.INSTANCE.GetLastError();
                        getLogger().orElse(LOGGER).debug("No pid detected, KERNEL32.getProcessId() error is {}", lastError);
                    }
                    return null;
                }
                return pid;
            } else {
                final Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                return field.getInt(process);
            }
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Dry run process.
     * Fake process that doesn't anything.
     */
    private static final class DryRunProcess extends Process {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final byte[] stdInBuffer = new byte[0];
        private final ByteArrayInputStream stdInStream = new ByteArrayInputStream(stdInBuffer);
        private final byte[] stdErrBuffer = new byte[0];
        private final ByteArrayInputStream stdErrStream = new ByteArrayInputStream(stdErrBuffer);
        private int exitCode;

        private DryRunProcess(final ProcessDryRunConfiguration processDryRunConfiguration) {
            super();
            processDryRunConfiguration.getExitCode().ifPresent(exitCode -> this.exitCode = exitCode);
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public InputStream getInputStream() {
            return stdInStream;
        }

        @Override
        public InputStream getErrorStream() {
            return stdErrStream;
        }

        @Override
        public int waitFor() {
            return exitCode;
        }

        @Override
        public int exitValue() {
            return exitCode;
        }

        @Override
        public void destroy() {
            //Nothing to do
        }
    }
}
