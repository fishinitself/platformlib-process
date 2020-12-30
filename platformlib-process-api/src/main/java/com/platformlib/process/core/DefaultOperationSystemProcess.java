package com.platformlib.process.core;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.api.ProcessInstance;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class DefaultOperationSystemProcess extends CompletableFuture<ProcessInstance> implements OperationSystemProcess {
    private final CountDownLatch pidCountDownLatch = new CountDownLatch(1);
    private Integer pid = null;

    public void setPid(final Integer pid) {
        this.pid = pid;
        pidCountDownLatch.countDown();
    }

    @Override
    public Optional<Integer> getPid() {
        try {
            pidCountDownLatch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(pid);
    }

    @Override
    public boolean completeExceptionally(final Throwable ex) {
        pidCountDownLatch.countDown();
        return super.completeExceptionally(ex);
    }

    @Override
    public boolean complete(final ProcessInstance value) {
        pidCountDownLatch.countDown();
        return super.complete(value);
    }
}
