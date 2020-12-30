package com.platformlib.process.local.impl;

import com.platformlib.process.core.DefaultOperationSystemProcess;
import com.platformlib.process.handler.ProcessDestroyerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local OS process implements .
 */
@SuppressWarnings({"unchecked", "PMD.LawOfDemeter"})
public class LocalOperationSystemProcess extends DefaultOperationSystemProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalOperationSystemProcess.class);
    private Process process;
    private final ProcessDestroyerHandler processDestroyerHandler;

    /**
     * Constructor.
     * @param processDestroyerHandler Process destroyer handler.
     */
    public LocalOperationSystemProcess(final ProcessDestroyerHandler processDestroyerHandler) {
        super();
        this.processDestroyerHandler = processDestroyerHandler;
    }

    /**
     * Set spawned process.
     * @param process spawned process
     */
    public void setProcess(final Process process) {
        this.process = process;
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (process == null) {
            LOGGER.warn("There is no assigned OS process");
            return super.cancel(mayInterruptIfRunning);
        }
        if (!getPid().isPresent() || processDestroyerHandler == null) {
            LOGGER.warn("The process doesn't have assigned PID. Use JVM method to stop process (subprocesses may not be killed/destroyed");
            process.destroyForcibly();
        } else {
            processDestroyerHandler.accept(getPid().get());
        }
        return true;
    }
}
