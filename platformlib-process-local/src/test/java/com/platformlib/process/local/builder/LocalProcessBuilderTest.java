package com.platformlib.process.local.builder;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.local.impl.LocalProcessBuilderImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalProcessBuilderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProcessBuilderTest.class);

    /**
     * Test {@link LocalProcessBuilder#useCurrentJava()} method.
     */
    @Test
    void testUseCurrentJava() {
        final LocalProcessBuilder localProcessBuilder = new LocalProcessBuilderImpl();
        final OperationSystemProcess osProcess = localProcessBuilder
                .useCurrentJava()
                .logger(logger -> logger.logger(LOGGER))
                .build()
                .execute("-version");
        final ProcessInstance processInstance = osProcess.toCompletableFuture().join();
        assertEquals(0, processInstance.getExitCode());
    }
}
