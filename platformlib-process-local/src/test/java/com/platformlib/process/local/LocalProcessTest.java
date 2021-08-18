package com.platformlib.process.local;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * Common test.
 */
@Execution(CONCURRENT)
class LocalProcessTest {
    private final ProcessBuilder exitCodeProcessBuilder = LocalGroovyCommand.newGroovyCommand("exit-code.groovy");
    private static final int PROCESS_SPAWN_SIZE = 512;

    /**
     * Spawn few process for fail fast testing concurrency.
     * Start fastest resourceless process.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testProcessSpawn() throws InterruptedException, ExecutionException {
        final ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND).command("echo").processInstance(processInstanceConfigurator -> processInstanceConfigurator.headLimit(2));
        final ExecutorService executor = Executors.newCachedThreadPool();
        final List<Future<OperationSystemProcess>> processes = IntStream.range(0, PROCESS_SPAWN_SIZE).mapToObj(i -> executor.submit(() -> processBuilder.build().execute(i))).collect(Collectors.toList());
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.MINUTES));
        assertEquals(PROCESS_SPAWN_SIZE, processes.size());
        for (int i = 0; i < PROCESS_SPAWN_SIZE; i++) {
            assertTrue(processes.get(i).isDone(), "Process execution " + i);
            final ProcessInstance processInstance = processes.get(i).get().toCompletableFuture().get();
            assertEquals(0, processInstance.getExitCode(), "Process execution " + i);
            final Collection<String> processStdOut = processInstance.getStdOut();
            assertEquals(1, processStdOut.size(), "Process execution " + i);
            assertEquals(String.valueOf(i), processStdOut.iterator().next(), "Process execution " + i);
        }
    }

    /**
     * Test process exit code.
     * @param exitCode expected exit code
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 255})
    void testExitCode(final int exitCode) {
        final ProcessInstance processInstance = exitCodeProcessBuilder.build().execute(exitCode).toCompletableFuture().join();
        assertEquals(exitCode, processInstance.getExitCode());
    }

    @Test
    void testWorkDirectory() throws IOException {
        final Path tempDirectory = Files.createTempDirectory("local-process-tmp-");
        try {
            final ProcessInstance processInstance = LocalGroovyCommand.newGroovyCommand("work-directory.groovy").workDirectory(tempDirectory).build().execute().toCompletableFuture().join();
            assertFalse(processInstance.getStdOut().isEmpty());
            assertEquals(tempDirectory.toAbsolutePath().toString(), processInstance.getStdOut().iterator().next());
        } finally {
            Files.delete(tempDirectory);
        }
    }

}
