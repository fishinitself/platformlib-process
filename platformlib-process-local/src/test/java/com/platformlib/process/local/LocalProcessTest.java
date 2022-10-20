package com.platformlib.process.local;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.enums.ProcessThreadType;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
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
    @DisplayName("Test setting work directory")
    void testWorkDirectory() throws IOException {
        final Path tempDirectory = Files.createTempDirectory("local-process-tmp-");
        try {
            final ProcessInstance processInstance = LocalGroovyCommand
                    .newGroovyCommand("work-directory.groovy")
                    .workDirectory(tempDirectory)
                    .build()
                    .execute()
                    .toCompletableFuture()
                    .join();
            assertThat(processInstance.getStdOut()).containsExactly(tempDirectory.toAbsolutePath().toString());
        } finally {
            Files.delete(tempDirectory);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"one", "two"})
    @DisplayName("Test run application in working directory")
    void testExecuteInWorkDirectory(final String scriptNameSuffix) throws URISyntaxException {
        final Path workDirectory = Paths.get(Objects.requireNonNull(LocalProcessTest.class.getResource("/test-work-directory")).toURI());
        final ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .workDirectory(workDirectory)
                .executeInWorkDirectory()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .defaultExtensionMapping();
        final ProcessInstance processInstance = processBuilder.build().execute("script-" + scriptNameSuffix).toCompletableFuture().join();
        assertThat(processInstance.getExitCode()).isEqualTo(0);
        assertThat(processInstance.getStdOut()).containsExactly("Script " + scriptNameSuffix + " output");
    }

    static Stream<Arguments> withPathInWorkDirectoryTestData() {
        return Stream.of(
                Arguments.of("one", true),
                Arguments.of("two", true),
                Arguments.of("one", false),
                Arguments.of("two", false)
        );
    }

    @ParameterizedTest
    @MethodSource("withPathInWorkDirectoryTestData")
    @DisplayName("Test run application in working directory with absolute path")
    void testExecuteWithAbsolutePathInWorkDirectory(final String scriptNameSuffix, final boolean executeInWorkDirectory) throws URISyntaxException {
        final Path workDirectory = Paths.get(Objects.requireNonNull(LocalProcessTest.class.getResource("/groovy-scripts")).toURI());
        final Path scriptPath = Paths.get(Objects.requireNonNull(LocalProcessTest.class.getResource("/test-work-directory")).toURI());
        ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .workDirectory(workDirectory)
                .processInstance(ProcessOutputConfigurator::unlimited)
                .defaultExtensionMapping();
        if (executeInWorkDirectory) {
            processBuilder = processBuilder.executeInWorkDirectory(); // Test that setting this parameter doesn't affect execution because of different from work directory path
        }
        final ProcessInstance processInstance = processBuilder.build().execute(scriptPath + File.separator + "script-" + scriptNameSuffix).toCompletableFuture().join();
        assertThat(processInstance.getExitCode()).isEqualTo(0);
        assertThat(processInstance.getStdOut()).containsExactly("Script " + scriptNameSuffix + " output");
    }

    @ParameterizedTest
    @MethodSource("withPathInWorkDirectoryTestData")
    @DisplayName("Test run application in working directory with relative path")
    void testExecuteWithRelativePathInWorkDirectory(final String scriptNameSuffix, final boolean executeInWorkDirectory) throws URISyntaxException {
        final Path workDirectory = Paths.get(Objects.requireNonNull(LocalProcessTest.class.getResource("/groovy-scripts")).toURI());
        ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .workDirectory(workDirectory)
                .processInstance(ProcessOutputConfigurator::unlimited)
                .defaultExtensionMapping();
        if (executeInWorkDirectory) {
            processBuilder = processBuilder.executeInWorkDirectory(); // Test that setting this parameter doesn't affect execution because of different from work directory path
        }
        final ProcessInstance processInstance = processBuilder.build().execute(".." + File.separator + "test-work-directory" + File.separator + "script-" + scriptNameSuffix).toCompletableFuture().join();
        assertThat(processInstance.getExitCode()).isEqualTo(0);
        assertThat(processInstance.getStdOut()).containsExactly("Script " + scriptNameSuffix + " output");
    }

    @Test
    @DisplayName("Verify that there is no delay in case of unknown command")
    void testProcessStartExceptionDelay() {
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        final ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND).command("non-existed-command-").withExecutor(executor);
        assertTimeout(Duration.ofSeconds(5), () ->
            assertTrue(processBuilder.build().execute().toCompletableFuture().isCompletedExceptionally())
        );
        assertThat(executor.shutdownNow()).describedAs("There should be no run threads").isEmpty();
    }

    @Test
    @DisplayName("Verify std out/err thread initializer call")
    void testProcessThreadStartListener() throws ExecutionException, InterruptedException {
        final Map<ProcessThreadType, AtomicInteger> threadsData = new ConcurrentHashMap<>();
        final ProcessBuilder processBuilder = ProcessBuilders
                .newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .logger(loggerConf -> loggerConf.onProcessThreadStart(processThreadType -> threadsData.computeIfAbsent(processThreadType, ptt -> new AtomicInteger(0)).incrementAndGet()))
                .command("echo");
        assertThat(processBuilder.build().execute().toCompletableFuture().get().getExitCode()).isEqualTo(0);
        assertThat(threadsData).size().isEqualTo(2);
        assertThat(threadsData).containsOnlyKeys(ProcessThreadType.STDOUT_LISTENER, ProcessThreadType.STDERR_LISTENER);
        assertThat(threadsData.get(ProcessThreadType.STDOUT_LISTENER).get()).isEqualTo(1);
        assertThat(threadsData.get(ProcessThreadType.STDERR_LISTENER).get()).isEqualTo(1);
    }
}
