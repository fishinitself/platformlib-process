package com.platformlib.process.ssh.impl;

import com.platformlib.process.builder.impl.DefaultProcessBuilder;
import com.platformlib.process.exception.ProcessConfigurationException;
import com.platformlib.process.executor.ProcessExecutor;
import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.builder.SshClientSessionBuilder;
import com.platformlib.process.ssh.builder.SshProcessBuilder;
import com.platformlib.process.ssh.specification.SshOsSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation {@link SshProcessBuilder}.
 */
@SuppressWarnings({"unchecked", "PMD.LawOfDemeter", "PMD.AvoidFieldNameMatchingMethodName"})
public class SshProcessBuilderImpl extends DefaultProcessBuilder implements SshProcessBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshProcessBuilderImpl.class);
    private SshClientSession sshClientSession;
    private SshConnection sshConnection;
    private SshOsSpec sshOsSpecification;

    public SshProcessBuilderImpl() {
    }

    public SshProcessBuilderImpl(final SshClientSession sshClientSession) {
        this.sshClientSession = sshClientSession;
    }

    @Override
    public ProcessExecutor build() {
        if (sshClientSession == null) {
            if (sshConnection == null) {
                throw new ProcessConfigurationException("No ssh connection is specified");
            }
            sshClientSession = SshClientSessionBuilder.defaultClient(sshConnection).autoClose().build();
        }
        return new SshProcessExecutor(sshClientSession, this, sshOsSpecification);
    }

    @Override
    protected boolean isWindowsPlatform() {
        if (sshOsSpecification == null) {
            throw new ProcessConfigurationException("The process execution requires SSH OS specification, but no specification was provided");
        }
        return sshOsSpecification.isWindowsBasedOs();
    }

    @Override
    public SshProcessBuilder connectTo(final SshConnection sshConnection) {
        this.sshConnection = sshConnection;
        return this;
    }

    @Override
    public SshProcessBuilder sshOsSpecification(final SshOsSpec sshOsSpecification) {
        this.sshOsSpecification = sshOsSpecification;
        return this;
    }

    @Override
    public void close() {
        sshClientSession.close();
    }
}
