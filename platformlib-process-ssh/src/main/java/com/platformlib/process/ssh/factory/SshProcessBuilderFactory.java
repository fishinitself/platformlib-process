package com.platformlib.process.ssh.factory;

import com.platformlib.process.ssh.builder.SshProcessBuilder;
import com.platformlib.process.ssh.impl.SshClientSession;
import com.platformlib.process.ssh.impl.SshProcessBuilderImpl;

/**
 * Local process builder factory.
 */
public final class SshProcessBuilderFactory {

    private SshProcessBuilderFactory() {
    }

    /**
     * Create new local process builder.
     * @return Returns created local process builder
     */
    public static SshProcessBuilder newSshProcessBuilder() {
        return new SshProcessBuilderImpl();
    }

    public static SshProcessBuilder newSshProcessBuilder(final SshClientSession sshClientSession) {
        return new SshProcessBuilderImpl(sshClientSession);
    }
}
