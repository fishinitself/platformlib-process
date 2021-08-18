package com.platformlib.process.ssh.impl;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SshClientSession implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshClientSession.class);
    private final SshClient sshClient;
    private final ClientSession clientSession;
    private final boolean autoCLose;
    private boolean isClosed = false;

    public SshClientSession(final SshClient sshClient, final ClientSession clientSession, final boolean autoCLose) {
        this.sshClient = sshClient;
        this.clientSession = clientSession;
        this.autoCLose = autoCLose;
    }

    public SshClient getSshClient() {
        return sshClient;
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public boolean isAutoCLose() {
        return autoCLose;
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }
        try {
            sshClient.close();
        } catch (final IOException ioException) {
            LOGGER.warn("Error while closing connection", ioException);
        } finally {
            isClosed = true;
        }
    }
}
