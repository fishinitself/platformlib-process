package com.platformlib.process.local;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@Execution(CONCURRENT)
class LocalProcessCommandLineArgumentsTest {
    private ProcessBuilder dumpAllCommandLineArgumentsProcessBuilder = LocalGroovyCommand.newGroovyCommand("dump-all-command-line-arguments.groovy");

    @ParameterizedTest
    @MethodSource("commandAndArguments")
    void testCommandLineArguments(final Collection<String> commandAndArguments) {
        final ProcessInstance processInstance = dumpAllCommandLineArgumentsProcessBuilder
                .build()
                .execute(commandAndArguments.toArray(new Object[0]))
                .toCompletableFuture()
                .join();
        assertEquals(0, processInstance.getExitCode());
        assertThat(processInstance.getStdOut())
                .containsExactlyElementsOf(commandAndArguments.stream().map(argument -> "#" + argument + "#").collect(Collectors.toList()));
    }

    static Stream<Arguments> commandAndArguments() {
        return Stream.of(
                Arguments.of(Collections.emptyList()),
                Arguments.of(Collections.singleton("One")),
                Arguments.of(Arrays.asList("one", "two")),
                Arguments.of(Collections.singleton(" ")),
                Arguments.of(Arrays.asList(" ", " ")),
                Arguments.of(Arrays.asList("'")),
                Arguments.of(Arrays.asList("''")),
                Arguments.of(Arrays.asList("'''")),
                Arguments.of(Arrays.asList("''''")),
                Arguments.of(Arrays.asList("'", "'")),
                Arguments.of(Arrays.asList("''", "''")),
                Arguments.of(Arrays.asList("'''", "'''")),
                Arguments.of(Arrays.asList("''''", "''''")),
                Arguments.of(Arrays.asList("''", "'")),
                Arguments.of(Arrays.asList("'''", "''")),
                Arguments.of(Arrays.asList("''''", "'''")),
                Arguments.of(Arrays.asList("'''''", "''''")),
                Arguments.of(Arrays.asList("'", "''")),
                Arguments.of(Arrays.asList("''", "'''")),
                Arguments.of(Arrays.asList("'''", "''''")),
                Arguments.of(Arrays.asList("''''", "'''''")),
                Arguments.of(Arrays.asList("\"")),
                Arguments.of(Arrays.asList("\"\"")),
                Arguments.of(Arrays.asList("\"\"\"")),
                Arguments.of(Arrays.asList("\"\"\"\"")),
                Arguments.of(Arrays.asList("\"", "\"")),
                Arguments.of(Arrays.asList("\"\"", "\"\"")),
                Arguments.of(Arrays.asList("\"\"\"", "\"\"\"")),
                Arguments.of(Arrays.asList("\"\"\"\"\"", "\"\"\"\"")),
                Arguments.of(Arrays.asList("\"\"", "\"")),
                Arguments.of(Arrays.asList("\"\"\"", "\"\"")),
                Arguments.of(Arrays.asList("\"\"\"\"", "\"\"\"")),
                Arguments.of(Arrays.asList("\"\"\"\"", "\"\"\"\"")),
                Arguments.of(Arrays.asList("\"", "\"\"")),
                Arguments.of(Arrays.asList("\"\"", "\"\"\"")),
                Arguments.of(Arrays.asList("\"\"\"", "\"\"\"\"")),
                Arguments.of(Arrays.asList("\"\"\"\"", "\"\"\"\"\"")),
                Arguments.of(Arrays.asList("\"", "'")),
                Arguments.of(Arrays.asList("\"\"", "'")),
                Arguments.of(Arrays.asList("\"", "''")),
                Arguments.of(Arrays.asList("\" \"")),
                Arguments.of(Arrays.asList("\" \"", "' '")),
                Arguments.of(Arrays.asList("\" \"", "' '", "\" \"")),
                Arguments.of(Arrays.asList("' '", "\" \"", "' ;")),
                Arguments.of(Arrays.asList("' ", "\" ", "' ")),
                Arguments.of(Arrays.asList("\" ", "' ", "\" ")),
                Arguments.of(Arrays.asList(" '", " \"", " '")),
                Arguments.of(Arrays.asList(" \"", " '", " \"")),
                Arguments.of(Arrays.asList(" ", " ", " "))
        );
    }
}
