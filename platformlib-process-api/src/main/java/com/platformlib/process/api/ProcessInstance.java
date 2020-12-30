package com.platformlib.process.api;

import java.util.Collection;

/**
 * Process instance.
 * Contains process information which is available after process finish.
 */
public interface ProcessInstance {
    /**
     * Get process exit code.
     * @return Returns process exit code
     */
    int getExitCode();

    /**
     * Get process standard output.
     * @return Returns process standard out output
     * @throws com.platformlib.process.exception.OutputOverflowException if process standard out output is more than was limited for process
     */
    Collection<String> getStdOut();

    /**
     * Get process error output.
     * @return Returns process standard error output
     * @throws com.platformlib.process.exception.OutputOverflowException if process standard error output is more than was limited for process
     */
    Collection<String> getStdErr();
}
