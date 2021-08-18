package com.platformlib.process.ssh;

import com.platformlib.process.core.MaskedPassword;

import java.nio.file.Path;
import java.util.Optional;

public class SshConnection {
    public static final int DEFAULT_SSH_PORT = 22;
    private final String hostname;
    private int port = DEFAULT_SSH_PORT;
    private final String username;
    private MaskedPassword userPassword;
    private Path keyPairFile;
    private MaskedPassword keyPairFilePassword;

    public SshConnection(final String hostname, final String username) {
        this.hostname = hostname;
        this.username = username;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserPassword(final MaskedPassword userPassword) {
        this.userPassword = userPassword;
    }

    public void setKeyPairFile(Path keyPairFile) {
        this.keyPairFile = keyPairFile;
    }

    public void setKeyPairFilePassword(MaskedPassword keyPairFilePassword) {
        this.keyPairFilePassword = keyPairFilePassword;
    }

    public String getUsername() {
        return username;
    }

    public Optional<MaskedPassword> getUserPassword() {
        return Optional.ofNullable(userPassword);
    }

    public Optional<Path> getKeyPairFile() {
        return Optional.ofNullable(keyPairFile);
    }

    public Optional<MaskedPassword> getKeyPairFilePassword() {
        return Optional.ofNullable(keyPairFilePassword);
    }


    public String getDisplayLabel() {
        return String.format("%s@%s [%s]", hostname, username, port);
    }
}
