package com.platformlib.process.ssh.exception;

public class SshBaseException extends RuntimeException {
    public SshBaseException(String message) {
        super(message);
    }

    public SshBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
