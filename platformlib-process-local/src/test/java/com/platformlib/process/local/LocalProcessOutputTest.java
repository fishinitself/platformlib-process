package com.platformlib.process.local;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
class LocalProcessOutputTest {
    private ProcessBuilder generateReadableOutputProcessBuilder = LocalGroovyCommand.newGroovyCommand("generate-readable-output.groovy").processInstance(processInstance -> processInstance.limit(5));

    @ParameterizedTest
    @MethodSource("readableOutput")
    void testCommandOutput(final int stdOutSize, final int stdErrSize) {
        final ProcessInstance processInstance = generateReadableOutputProcessBuilder.build().execute(stdOutSize, stdErrSize).toCompletableFuture().join();
        assertEquals(0, processInstance.getExitCode());
        assertThat(processInstance.getStdOut()).containsExactlyElementsOf(IntStream.range(0, stdOutSize).boxed().map(value -> "#" + value + "#").collect(Collectors.toList()));
        assertThat(processInstance.getStdErr()).containsExactlyElementsOf(IntStream.range(0, stdErrSize).boxed().map(value -> "*" + value + "*").collect(Collectors.toList()));
    }

    static Stream<Arguments> readableOutput() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(0, 1),
                Arguments.of(1, 0),
                Arguments.of(1, 1),
                Arguments.of(0, 5),
                Arguments.of(5, 0),
                Arguments.of(5, 5)
        );
    }

    @Test
    void testSymbolsInStdInCaseOfNonZeroExitCode() {
        final ProcessInstance processInstance = LocalGroovyCommand.newGroovyCommand("std-output-with-1-exit-code.groovy")
                .processInstance(ProcessOutputConfigurator::unlimited)
                .build().execute().toCompletableFuture().join();
        assertThat(processInstance.getExitCode()).isEqualTo(1);
        assertThat(processInstance.getStdOut()).containsExactly("A");
        assertThat(processInstance.getStdErr()).containsExactly("B");
    }
}
