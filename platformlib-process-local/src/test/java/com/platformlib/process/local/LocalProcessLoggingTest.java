package com.platformlib.process.local;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Execution(CONCURRENT)
public class LocalProcessLoggingTest {

    /**
     * Test logging for base output messages.
     */
    @Test
    void testBaseLoggingOutput() {
        final ArgumentCaptor<String> formatCaptor = ArgumentCaptor.forClass(String.class);
        final Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        final ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .command("echo")
                .logger(configuration -> configuration.logger(logger));
        final ProcessInstance processInstance = processBuilder.build().execute("1").toCompletableFuture().join();
        assertEquals(0, processInstance.getExitCode());
        verify(logger, atLeastOnce()).isDebugEnabled();
        verify(logger, atLeastOnce()).debug(formatCaptor.capture(), any(), any());
        assertThat(formatCaptor.getAllValues()).contains("Start [{}] the local OS process {}", "PID [{}] is {}", "[{}] Process output: {}", "The local OS process [{}] has been finished with exit code {}");
    }

    @Test
    void testProcessOutputLogging() {
        final ArgumentCaptor<String> formatCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> firstArgument = ArgumentCaptor.forClass(Object.class);
        final ArgumentCaptor<Object> secondArgument = ArgumentCaptor.forClass(Object.class);
        final Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);

        ProcessBuilder processBuilder = ProcessBuilders.newProcessBuilder(LocalProcessSpec.CURRENT_JAVA_COMMAND)
                .commandAndArguments("-jar", LocalGroovyCommand.GROOVY_JAR_PATH, LocalGroovyCommand.groovyScript("process-output.groovy"))
                .logger(configuration -> configuration.logger(logger));
        final ProcessInstance processInstance = processBuilder.build().execute().toCompletableFuture().join();
        assertEquals(0, processInstance.getExitCode());
        verify(logger, atLeastOnce()).isDebugEnabled();
        verify(logger, atLeastOnce()).debug(formatCaptor.capture(), firstArgument.capture(), secondArgument.capture());
        assertThat(firstArgument.getAllValues()).contains("stdout", "stderr");
        assertThat(secondArgument.getAllValues()).contains("Second", "First", "Third");
    }
}
