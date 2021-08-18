package com.platformlib.process.ssh.builder;

import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.specification.SshOsSpec;

public interface SshProcessBuilder extends ProcessBuilder, AutoCloseable {
    SshProcessBuilder connectTo(SshConnection sshConnection);
    SshProcessBuilder sshOsSpecification(SshOsSpec sshOsSpecification);
    @Override
    void close();
}
