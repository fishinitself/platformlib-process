package com.platformlib.process.ssh.exception;

public class SshAuthenticationException extends SshBaseException {
    public SshAuthenticationException(String message) {
        super(message);
    }

    public SshAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
