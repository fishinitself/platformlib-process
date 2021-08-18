package com.platformlib.process.ssh.builder;

import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.configuration.SshClientConfiguration;
import com.platformlib.process.ssh.exception.SshAuthenticationException;
import com.platformlib.process.ssh.exception.SshConnectException;
import com.platformlib.process.ssh.impl.SshClientSession;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.compression.BuiltinCompressions;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SshClientSessionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshClientSessionBuilder.class);
    public static final Duration DEFAULT_AUTHENTICATION_TIMEOUT = Duration.ofMinutes(1);
    private final SshConnection sshConnection;
    private SshClientConfiguration sshClientConfiguration;
    private Duration authenticationTimeout;
    private boolean autoClose = false;

    public SshClientSessionBuilder(final SshConnection sshConnection) {
        this.sshConnection = sshConnection;
    }

    public static SshClientSessionBuilder defaultClient(final SshConnection sshConnection) {
        return new SshClientSessionBuilder(sshConnection).configure(new SshClientConfiguration());
    }

    public SshClientSessionBuilder configure(final SshClientConfiguration sshClientConfiguration) {
        this.sshClientConfiguration = sshClientConfiguration;
        return this;
    }

    public SshClientSessionBuilder autoClose() {
        this.autoClose = true;
        return this;
    }

    Optional<Duration> getAuthenticationTimeout() {
        return Optional.of(authenticationTimeout == null ? DEFAULT_AUTHENTICATION_TIMEOUT : authenticationTimeout);
    }

    public SshClientSession build() {
        final SshClient client = SshClient.setUpDefaultClient();
        final List<UserAuthFactory> authFactories = new ArrayList<>();
        sshConnection.getUserPassword().ifPresent(userPassword -> {
            authFactories.add(UserAuthPasswordFactory.INSTANCE);
            client.addPasswordIdentity(userPassword.getSourceValue());
        });
        sshConnection.getKeyPairFile().ifPresent(keyPairFile -> {
            authFactories.add(UserAuthPublicKeyFactory.INSTANCE);
            final FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(keyPairFile);
            sshConnection.getKeyPairFilePassword().ifPresent(keyPairFilePassword -> keyPairProvider.setPasswordFinder(FilePasswordProvider.of(keyPairFilePassword.getSourceValue())));
            try {
                for (final String keyType: keyPairProvider.getKeyTypes(null)) {
                    client.addPublicKeyIdentity(keyPairProvider.loadKey(null, keyType));
                }
            } catch (IOException | GeneralSecurityException exception) {
                throw new RuntimeException(exception);
            }
        });
        client.setUserAuthFactories(authFactories);

        getAuthenticationTimeout().ifPresent(authenticationTimeout -> CoreModuleProperties.AUTH_TIMEOUT.set(client, authenticationTimeout));
        if (sshClientConfiguration != null) {
            if (sshClientConfiguration.isCompressionEnabled()) {
                client.setCompressionFactories(Collections.singletonList(BuiltinCompressions.zlib));
            }
            sshClientConfiguration.getIdleTime().ifPresent(idleTime -> {
                CoreModuleProperties.NIO2_READ_TIMEOUT.set(client, idleTime);
                CoreModuleProperties.IDLE_TIMEOUT.set(client, idleTime);
            });
            //TODO CHeck for configuration: CoreModuleProperties.NIO2_MIN_WRITE_TIMEOUT.set(client, ???);

            sshClientConfiguration.getNioBufferSize().ifPresent(nioBufferSize -> CoreModuleProperties.NIO2_READ_BUFFER_SIZE.set(client, nioBufferSize));

            sshClientConfiguration.getTcpConfiguration().isNoDelay().ifPresent(noDelay -> CoreModuleProperties.TCP_NODELAY.set(client, noDelay));
            sshClientConfiguration.getSocketConfiguration().isKeepAlive().ifPresent(keepAlive -> CoreModuleProperties.SOCKET_KEEPALIVE.set(client, keepAlive));
            sshClientConfiguration.getSocketConfiguration().getSndbuf().ifPresent(bufferSize -> CoreModuleProperties.SOCKET_SNDBUF.set(client, bufferSize));
            sshClientConfiguration.getSocketConfiguration().getRcvbuf().ifPresent(bufferSize -> CoreModuleProperties.SOCKET_RCVBUF.set(client, bufferSize));

            sshClientConfiguration.getSftpConfiguration().getWriteChunkSize().ifPresent(chunkSize -> SftpModuleProperties.WRITE_CHUNK_SIZE.set(client, chunkSize));
            sshClientConfiguration.getSftpConfiguration().getReadBufferSize().ifPresent(readBufferSize -> SftpModuleProperties.READ_BUFFER_SIZE.set(client, readBufferSize));
            sshClientConfiguration.getSftpConfiguration().getWriteBufferSize().ifPresent(writeBufferSize -> SftpModuleProperties.WRITE_BUFFER_SIZE.set(client, writeBufferSize));

            if (sshClientConfiguration.isKeyReExchangeDisabled()) {
                CoreModuleProperties.REKEY_BYTES_LIMIT.set(client, -1L);
                CoreModuleProperties.REKEY_TIME_LIMIT.set(client, Duration.ofSeconds(-1));
                CoreModuleProperties.REKEY_PACKETS_LIMIT.set(client, -1L);
                CoreModuleProperties.REKEY_BLOCKS_LIMIT.set(client, -1L);
            }
        }

        client.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
        client.start();
        final ClientSession session;
        try {
            final ConnectFuture connectFuture = client.connect(sshConnection.getUsername(), sshConnection.getHostname(), sshConnection.getPort());
            connectFuture.await(getAuthenticationTimeout().get().toMillis());
            session = connectFuture.getSession();
        } catch (final Exception exception) {
            throw fail(client, () -> new SshConnectException("Fail to connect to " + sshConnection.getDisplayLabel(), exception));
        }
        if (session == null) {
            throw fail(client, () -> new IOException("Unable to get session"));
        }
        try {
            session.auth().verify(getAuthenticationTimeout().orElseThrow(IllegalStateException::new).toMillis()).verify();
        } catch (final IOException exception) {
            throw fail(client, () -> new SshAuthenticationException("Authentication failure on " + sshConnection.getDisplayLabel(), exception));
        }
        return new SshClientSession(client, session, autoClose);
    }

    private RuntimeException fail(final SshClient client, final Supplier<Exception> exceptionSupplier) {
        try {
            client.close();
        } catch (final IOException ioException) {
            LOGGER.warn("An unexpected error while closing ssh client", ioException);
        }
        final Exception exception = exceptionSupplier.get();
        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        }
        return new RuntimeException(exception);
    }

}
