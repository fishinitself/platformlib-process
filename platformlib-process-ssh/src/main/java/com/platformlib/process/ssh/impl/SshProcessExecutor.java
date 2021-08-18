package com.platformlib.process.ssh.impl;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.configuration.ProcessConfiguration;
import com.platformlib.process.configuration.logger.ProcessLoggerConfiguration;
import com.platformlib.process.core.AsyncProcessOutputListener;
import com.platformlib.process.core.DefaultOperationSystemProcess;
import com.platformlib.process.core.DefaultProcessInstance;
import com.platformlib.process.exception.ProcessConfigurationException;
import com.platformlib.process.executor.DefaultProcessExecutor;
import com.platformlib.process.ssh.specification.SshOsSpec;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SshProcessExecutor extends DefaultProcessExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshProcessExecutor.class);
    private static final AtomicLong EXECUTION_COUNTER = new AtomicLong(0L);
    private final SshClientSession sshClientSession;
    private AsyncProcessOutputListener stdOutListener;
    private AsyncProcessOutputListener stdErrListener;
    private final ProcessConfiguration processConfiguration;
    private final SshOsSpec sshOsSpecification;

    /**
     * Default constructor.
     *
     * @param sshClientSession opened and authenticated ssh client session
     * @param processConfiguration process configuration
     */
    public SshProcessExecutor(final SshClientSession sshClientSession,
                              final ProcessConfiguration processConfiguration,
                              final SshOsSpec sshOsSpecification) {
        super(processConfiguration);
        this.sshClientSession = sshClientSession;
        this.processConfiguration = processConfiguration;
        this.sshOsSpecification = sshOsSpecification;
    }

    private SshOsSpec getSshOsSpecification() {
        if (sshOsSpecification == null) {
            throw new ProcessConfigurationException("The process execution requires SSH OS specification, but no specification was provided");
        }
        return sshOsSpecification;
    }

    @Override
    public OperationSystemProcess execute(final Object... commandAndArguments) {
        final DefaultOperationSystemProcess operationSystemProcess = new DefaultOperationSystemProcess();
        final String callerThreadName = Thread.currentThread().getName();
        final long executionId = EXECUTION_COUNTER.incrementAndGet();

        final Collection<String> sshCommandAndArguments = new ArrayList<>();
        processConfiguration.getWorkDirectory().ifPresent(workDirectory -> {
            //TODO Support directory with spaces
            sshCommandAndArguments.add("cd " + workDirectory + (getSshOsSpecification().isWindowsBasedOs() ? "&&" : ";"));
        });
        sshCommandAndArguments.addAll(getUnmaskedCommandAndArguments(commandAndArguments));
       final String commandToExecute = String.join(" ", sshCommandAndArguments);
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
        getExecutor().execute(() -> {
            Thread.currentThread().setName("ssh-execute-" + callerThreadName + "-" + executionId);
            final long processThreadStartNanoTime = System.nanoTime();
            try {
                try (ChannelExec channelExec = sshClientSession.getClientSession().createExecChannel(commandToExecute)) {
                    LOGGER.trace("Execute [{}] remote command: {}", executionId, commandToExecute);
                    final PipedInputStream stdOutInputStream = new PipedInputStream();
                    final PipedOutputStream stdOutOutputStream = new PipedOutputStream(stdOutInputStream);
                    channelExec.setOut(stdOutOutputStream);
                    stdOutListener.startListening(stdOutInputStream);
                    final PipedInputStream stdErrInputStream = new PipedInputStream();
                    final PipedOutputStream stdErrOutputStream = new PipedOutputStream(stdErrInputStream);
                    channelExec.setErr(stdErrOutputStream);
                    stdErrListener.startListening(stdErrInputStream);

                    //TODO Control timeout
                    channelExec.open().verify(1, TimeUnit.MINUTES);
                    final Set<ClientChannelEvent> channelEvents = channelExec.waitFor(EnumSet.of(ClientChannelEvent.TIMEOUT, ClientChannelEvent.EXIT_SIGNAL, ClientChannelEvent.EXIT_STATUS, ClientChannelEvent.CLOSED), processConfiguration.getExecutionTimeout().orElse(null));
                    if (channelEvents.contains(ClientChannelEvent.EXIT_STATUS)) {
                        //TODO Control timeout
                        final Set<ClientChannelEvent> events = channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.MINUTES.toMillis(1L));
                        if (channelExec.getExitStatus() == null) {
                            if (events == null) {
                                LOGGER.error("The closed event has not been received");
                            } else {
                                events.forEach(event -> LOGGER.error("Received event {}", event));
                            }
                            LOGGER.error("The is no exit code received in {} ms", Duration.ofNanos(processThreadStartNanoTime - System.nanoTime()).toMillis());
                            operationSystemProcess.completeExceptionally(new IOException("There is no SSH exit status"));
                        } else {
                            LOGGER.debug("The process execution took {} ms, exit code {}", Duration.ofNanos(processThreadStartNanoTime - System.nanoTime()).toMillis(), channelExec.getExitStatus());
                            operationSystemProcess.complete(new DefaultProcessInstance(channelExec.getExitStatus(), getProcessStdOut(), getProcessStdErr()));
                        }
                    } else if (channelEvents.contains(ClientChannelEvent.EXIT_SIGNAL)) {
                        operationSystemProcess.completeExceptionally(new IOException("The channel received EXIT signal"));
                    } else if (channelEvents.contains(ClientChannelEvent.CLOSED)) {
                        operationSystemProcess.completeExceptionally(new IOException("The channel received CLOSED signal"));
                    } else {
                        channelEvents.forEach(event -> LOGGER.error("The channel received {} event", event));
                        operationSystemProcess.completeExceptionally(new IOException("An unexpected problem with channel"));
                    }
                }
            } catch (final Throwable throwable) {
                LOGGER.error("The SSH remote OS process execution failed", throwable);
                operationSystemProcess.completeExceptionally(throwable);
            } finally {
                try {
                    stdOutListener.close();
                } catch (final IOException ioException) {
                    LOGGER.warn("Fail to close stdout listener", ioException);
                }
                try {
                    stdErrListener.close();
                } catch (final IOException ioException) {
                    LOGGER.warn("Fail to close stderr listener", ioException);
                }
                if (sshClientSession.isAutoCLose()) {
                    sshClientSession.close();
                }
            }
        });
        return operationSystemProcess;
    }

}
