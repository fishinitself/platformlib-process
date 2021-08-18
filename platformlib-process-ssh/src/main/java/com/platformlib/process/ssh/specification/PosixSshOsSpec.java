package com.platformlib.process.ssh.specification;

public class PosixSshOsSpec implements SshOsSpec {
    @Override
    public boolean isWindowsBasedOs() {
        return false;
    }
}
