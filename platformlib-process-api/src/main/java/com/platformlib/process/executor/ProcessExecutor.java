package com.platformlib.process.executor;

import com.platformlib.process.api.OperationSystemProcess;

import java.io.InputStream;

/**
 * OS process executor.
 */
public interface ProcessExecutor {
    /**
     * Get process standard output stream.
     * @return Returns {@link InputStream} provides process standard output
     */
    InputStream getStdOutInputStream();

    /**
     * Get process standard error stream.
     * @return Returns {@link InputStream} provides process standard error
     */
    InputStream getStdErrInputStream();

    /**
     * Execute command with arguments asynchronously.
     * If the command is already specified in process configuration then command arguments will be extended with given parameters.
     * @param commandAndArguments command and arguments
     * @return Started OS process
     */
    OperationSystemProcess execute(Object... commandAndArguments);
}
