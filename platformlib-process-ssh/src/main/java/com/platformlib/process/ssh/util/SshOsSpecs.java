package com.platformlib.process.ssh.util;

import com.platformlib.process.ssh.specification.PosixSshOsSpec;
import com.platformlib.process.ssh.specification.SshOsSpec;

public final class SshOsSpecs {
    public static final SshOsSpec POSIX = new PosixSshOsSpec();

    private SshOsSpecs() {
    }
}
