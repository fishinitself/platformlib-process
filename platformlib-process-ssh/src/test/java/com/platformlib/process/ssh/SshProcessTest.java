package com.platformlib.process.ssh;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.core.MaskedPassword;
import com.platformlib.process.ssh.factory.SshProcessBuilderFactory;
import com.platformlib.process.ssh.util.SshOsSpecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

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

    @Test
    @DisplayName("Test setting work directory")
    void testWorkDirectory() {
        final ProcessInstance processInstance = sshExec(builder -> builder.workDirectory("/tmp").command("pwd"));
        assertThat(processInstance.getStdOut()).containsExactly("/tmp");
    }
}
