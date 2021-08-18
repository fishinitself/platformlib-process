package com.platformlib.process.ssh.exception;

public class SshConnectException extends SshBaseException {
    public SshConnectException(String message) {
        super(message);
    }

    public SshConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
