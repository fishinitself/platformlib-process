package com.platformlib.process.ssh.configuration;

import java.time.Duration;
import java.util.Optional;

public class SshClientConfiguration {
    private Duration idleTime = Duration.ZERO;
    private boolean useCompression = false;
    private Integer nioBufferSize;
    private boolean keyReExchange = true;

    private final SshTcpConfiguration tcpConfiguration = new SshTcpConfiguration();
    private final SshSftpConfiguration sftpConfiguration = new SshSftpConfiguration();
    private final SshSocketConfiguration socketConfiguration = new SshSocketConfiguration();

    public SshClientConfiguration() {
    }

    public Optional<Duration> getIdleTime() {
        return Optional.ofNullable(idleTime);
    }

    public void setIdleTime(Duration idleTime) {
        this.idleTime = idleTime;
    }

    public void enableCompression() {
        useCompression = true;
    }

    public boolean isCompressionEnabled() {
        return useCompression;
    }

    public Optional<Integer> getNioBufferSize() {
        return Optional.ofNullable(nioBufferSize);
    }

    public void setNioBufferSize(Integer nioBufferSize) {
        this.nioBufferSize = nioBufferSize;
    }

    public SshTcpConfiguration getTcpConfiguration() {
        return tcpConfiguration;
    }

    public SshSftpConfiguration getSftpConfiguration() {
        return sftpConfiguration;
    }

    public SshSocketConfiguration getSocketConfiguration() {
        return socketConfiguration;
    }

    public static final class SshTcpConfiguration {
        private Boolean noDelay;

        public Optional<Boolean> isNoDelay() {
            return Optional.ofNullable(noDelay);
        }

        public void setNoDelay(boolean noDelay) {
            this.noDelay = noDelay;
        }
    }

    public void disableKeyReExchange() {
        keyReExchange = false;
    }

    public boolean isKeyReExchangeDisabled() {
        return !keyReExchange;
    }

    public static final class SshSftpConfiguration {
        private Integer writeChunkSize;
        private Integer readBufferSize;
        private Integer writeBufferSize;

        public Optional<Integer> getWriteChunkSize() {
            return Optional.ofNullable(writeChunkSize);
        }

        public void setWriteChunkSize(Integer writeChunkSize) {
            this.writeChunkSize = writeChunkSize;
        }

        public Optional<Integer> getReadBufferSize() {
            return Optional.ofNullable(readBufferSize);
        }

        public void setReadBufferSize(Integer readBufferSize) {
            this.readBufferSize = readBufferSize;
        }

        public Optional<Integer> getWriteBufferSize() {
            return Optional.ofNullable(writeBufferSize);
        }

        public void setWriteBufferSize(Integer writeBufferSize) {
            this.writeBufferSize = writeBufferSize;
        }
    }

    public static final class SshSocketConfiguration {
        private Boolean keepAlive;
        private Integer sndbuf;
        private Integer rcvbuf;

        public Optional<Boolean> isKeepAlive() {
            return Optional.ofNullable(keepAlive);
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

        public Optional<Integer> getSndbuf() {
            return Optional.ofNullable(sndbuf);
        }

        public void setSndbuf(Integer sndbuf) {
            this.sndbuf = sndbuf;
        }

        public Optional<Integer> getRcvbuf() {
            return Optional.ofNullable(rcvbuf);
        }

        public void setRcvbuf(Integer rcvbuf) {
            this.rcvbuf = rcvbuf;
        }
    }

}
