package com.platformlib.process.api;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * OS process interface.
 */
public interface OperationSystemProcess extends CompletionStage<ProcessInstance> {
    /**
     * Get process ID.
     * @return Returns process identification number (pid) or {@link Optional#empty()} if pid couldn't be gotten
     */
    Optional<Integer> getPid();
}
