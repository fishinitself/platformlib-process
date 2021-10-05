package com.platformlib.process.local;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.local.factory.LocalProcessBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * OS specific functional tests.
 * Requires docker be installed.
 * Only for NIX platform.
 */
@Execution(CONCURRENT)
class LocalProcessOutputFunctionalTest {
    @Test
    void testOneSymbolInStdout() {
        final ProcessInstance processInstance = LocalProcessBuilderFactory
                .newLocalProcessBuilder()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .command("echo")
                .build().execute("-n", "Z")
                .toCompletableFuture()
                .join();
        assertThat(processInstance.getExitCode()).isEqualTo(0);
        assertThat(processInstance.getStdOut()).containsExactly("Z");
        assertThat(processInstance.getStdErr()).isEmpty();
    }

    @Test
    void testOneSymbolInStderr() {
        final ProcessInstance processInstance = LocalProcessBuilderFactory
                .newLocalProcessBuilder()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .command("bash")
                .build().execute("-c", "echo -n Z >&2")
                .toCompletableFuture()
                .join();
        assertThat(processInstance.getExitCode()).isEqualTo(0);
        assertThat(processInstance.getStdOut()).isEmpty();
        assertThat(processInstance.getStdErr()).containsExactly("Z");
    }

    @Test
    void testDockerStderr() {
        final ProcessInstance processInstance = LocalProcessBuilderFactory
                .newLocalProcessBuilder()
                .processInstance(ProcessOutputConfigurator::unlimited)
                .build().execute("docker", "container", "run" , "--rm", "--network", "host", "golang:1.17.0-alpine3.14", "go", "build")
                .toCompletableFuture()
                .join();
        assertThat(processInstance.getExitCode()).isNotEqualTo(0);
        assertThat(processInstance.getStdErr()).contains("go: go.mod file not found in current directory or any parent directory; see 'go help modules'");
    }
}
