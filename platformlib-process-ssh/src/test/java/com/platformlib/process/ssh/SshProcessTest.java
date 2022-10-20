package com.platformlib.process.ssh;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.core.MaskedPassword;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import com.platformlib.process.ssh.factory.SshProcessBuilderFactory;
import com.platformlib.process.ssh.util.SshOsSpecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test ssh exec.
 */
public class SshProcessTest extends AbstractProcessSshTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshProcessTest.class);

    ProcessBuilder newSshProcessBuilder() {
        final SshConnection sshConnection = new SshConnection("localhost", "ssh-user");
        sshConnection.setUserPassword(MaskedPassword.of("secret"));
        sshConnection.setPort(2222);
        return SshProcessBuilderFactory
                .newSshProcessBuilder()
                .connectTo(sshConnection)
                .sshOsSpecification(SshOsSpecs.POSIX)
                .logger(loggerConf -> loggerConf.logger(LOGGER))
                .processInstance(ProcessOutputConfigurator::unlimited);
    }

    ProcessInstance sshExec(final Consumer<ProcessBuilder> processBuilderConsumer, final Object... commandAndArgument) {
        final ProcessBuilder processBuilder = newSshProcessBuilder();
        processBuilderConsumer.accept(processBuilder);
        return processBuilder.build().execute(commandAndArgument).toCompletableFuture().join();
    }

    void sshCopy(final Path source, final String destination) {
        assertThat(
                ProcessBuilders
                        .newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                        .commandAndArguments("docker", "cp", source, CONTAINER_ID + ":" + destination)
                        .build()
                        .execute()
                        .toCompletableFuture()
                        .join()
                        .getExitCode()
        ).isEqualTo(0);
    }

    @Test
    @DisplayName("Test setting work directory")
    void testWorkDirectory() {
        final ProcessInstance processInstance = sshExec(builder -> builder.workDirectory("/tmp").command("pwd"));
        assertThat(processInstance.getStdOut()).containsExactly("/tmp");
    }

    static Stream<Arguments> withPathInWorkDirectoryTestData() {
        return Stream.of(
                Arguments.of("script-one", "/tmp/work-scripts", true, 0),
                Arguments.of("script-one", "/tmp/work-scripts", false, 127),
                Arguments.of("script-two.sh", "/tmp/work-scripts", true, 0),
                Arguments.of("script-two.sh", "/tmp/work-scripts", false, 127),
                Arguments.of("../work-scripts/script-one", "/tmp/work-directory", true, 0),
                Arguments.of("../work-scripts/script-one", "/tmp/work-directory", false, 0),
                Arguments.of("../work-scripts/script-two.sh", "/tmp/work-directory", true, 0),
                Arguments.of("../work-scripts/script-two.sh", "/tmp/work-directory", false, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("withPathInWorkDirectoryTestData")
    @DisplayName("Test setting work directory")
    void testExecuteInWorkDirectory(
            final String command,
            final String workDir,
            final boolean runInWorkDir,
            final int expectedExitCode) throws URISyntaxException {
        assertThat(sshExec(builder -> builder.commandAndArguments("mkdir", "-p", "/tmp/work-directory")).getExitCode()).isEqualTo(0);
        assertThat(sshExec(builder -> builder.commandAndArguments("mkdir", "-p", "/tmp/work-scripts")).getExitCode()).isEqualTo(0);
        final Path scriptsPath = Paths.get(Objects.requireNonNull(SshProcessTest.class.getResource("/scripts")).toURI());
        sshCopy(scriptsPath.resolve("script-one"), "/tmp/work-scripts");
        sshCopy(scriptsPath.resolve("script-two.sh"), "/tmp/work-scripts");
        final ProcessInstance processInstance = sshExec(builder -> {
            builder.command(command).workDirectory(workDir);
            if (runInWorkDir) {
                builder.executeInWorkDirectory();
            }
        });
        assertThat(processInstance.getExitCode()).isEqualTo(expectedExitCode);
    }
}
