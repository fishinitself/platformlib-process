package com.platformlib.process.ssh;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.core.MaskedPassword;
import com.platformlib.process.ssh.exception.SshAuthenticationException;
import com.platformlib.process.ssh.factory.SshProcessBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSshConnection extends AbstractProcessSshTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSshConnection.class);

    /**
     * Test password authentication failure.
     */
    @ParameterizedTest
    @CsvSource({"ssh-user,bad secret", "bad-ssh-user,secret", "bad-ssh-user,bad secret", "ssh-User,Secret"})
    void testPasswordAuthenticationFailure(final String username, final String password) {
        final SshConnection sshConnection = new SshConnection("localhost", username);
        sshConnection.setUserPassword(MaskedPassword.of(password));
        sshConnection.setPort(2222);
        final SshAuthenticationException sshAuthenticationException = assertThrows(SshAuthenticationException.class, ()-> SshProcessBuilderFactory
                .newSshProcessBuilder()
                .connectTo(sshConnection)
                .logger(loggerConf -> loggerConf.logger(LOGGER))
                .build());
        assertEquals("Authentication failure on localhost@" + username + " [2222]", sshAuthenticationException.getMessage());
    }

    /**
     * Test authentication by provided password.
     */
    @Test
    void testPasswordAuthentication() throws ExecutionException, InterruptedException {
        final SshConnection sshConnection = new SshConnection("localhost", "ssh-user");
        sshConnection.setUserPassword(MaskedPassword.of("secret"));
        sshConnection.setPort(2222);
        final ProcessInstance processInstance = SshProcessBuilderFactory
                .newSshProcessBuilder()
                .connectTo(sshConnection)
                .logger(loggerConf -> loggerConf.logger(LOGGER))
                .build()
                .execute("pwd")
                .toCompletableFuture()
                .get();
        assertEquals(0, processInstance.getExitCode());
    }

    /**
     * Test key-based authentication.
     */
    @Test
    void testKeyBasedAuthentication() throws ExecutionException, InterruptedException, URISyntaxException {
        final SshConnection sshConnection = new SshConnection("localhost", "ssh-user");
        final Path sshPrivateKeyFilePath = Paths.get(Objects.requireNonNull(TestSshConnection.class.getResource("/ssh-private-keys/platformlib-ssh-rsa")).toURI());
        sshConnection.setKeyPairFile(sshPrivateKeyFilePath);
        sshConnection.setPort(2222);
        final ProcessInstance processInstance = SshProcessBuilderFactory
                .newSshProcessBuilder()
                .connectTo(sshConnection)
                .logger(loggerConf -> loggerConf.logger(LOGGER))
                .build()
                .execute("pwd")
                .toCompletableFuture()
                .get();
        assertEquals(0, processInstance.getExitCode());
    }

    /**
     * Test password protected key-based by provided password.
     */
    @Test
    void testPasswordProtectedKeyBasedAuthentication() throws ExecutionException, InterruptedException, URISyntaxException {
        final SshConnection sshConnection = new SshConnection("localhost", "ssh-user");
        final Path sshPrivateKeyFilePath = Paths.get(Objects.requireNonNull(TestSshConnection.class.getResource("/ssh-private-keys/platformlib-encrypted-ssh-ecdsa")).toURI());
        sshConnection.setKeyPairFile(sshPrivateKeyFilePath);
        sshConnection.setKeyPairFilePassword(MaskedPassword.of("A secret"));
        sshConnection.setPort(2222);
        final ProcessInstance processInstance = SshProcessBuilderFactory
                .newSshProcessBuilder()
                .connectTo(sshConnection)
                .logger(loggerConf -> loggerConf.logger(LOGGER))
                .build()
                .execute("pwd")
                .toCompletableFuture()
                .get();
        assertEquals(0, processInstance.getExitCode());
    }
}
