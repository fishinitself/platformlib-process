package com.platformlib.process.local;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dry run mode.
 */
class DryRunTest {
    private final ProcessBuilder dryRunProcessBuilder = ProcessBuilders
            .newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
            .command("non existed command");

    /**
     * Test exit code
     * @param exitCode expected exit code
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 255})
    void testDryRunExitCode(final int exitCode) throws ExecutionException, InterruptedException {
        final ProcessInstance processInstance = dryRunProcessBuilder.dryRun(dryRunConfig -> dryRunConfig.exitCode(exitCode)).build().execute().toCompletableFuture().get();
        assertEquals(exitCode, processInstance.getExitCode());
    }

    @ParameterizedTest
    @MethodSource("commandAndArguments")
    void testDryRunCommandArgumentSupplier(final Collection<String> commandAndArguments) throws ExecutionException, InterruptedException {
        final List<String> suppliedCommandAndArguments = new CopyOnWriteArrayList<>();
        final ProcessInstance processInstance = dryRunProcessBuilder
                .dryRun(dryRunConfig -> dryRunConfig.commandAndArgumentsSupplier(suppliedCommandAndArguments::addAll))
                .build()
                .execute(commandAndArguments.toArray(new Object[0]))
                .toCompletableFuture()
                .get();
        assertEquals(0, processInstance.getExitCode());
        assertThat(suppliedCommandAndArguments).startsWith("non existed command").endsWith(commandAndArguments.toArray(new String[0]));
    }

    static Stream<Arguments> commandAndArguments() {
        return Stream.of(
                Arguments.of(Collections.emptyList()),
                Arguments.of(Collections.singleton("One")),
                Arguments.of(Arrays.asList("one", "two")),
                Arguments.of(Arrays.asList("one ", "two", " ", "four"))
        );
    }

}
