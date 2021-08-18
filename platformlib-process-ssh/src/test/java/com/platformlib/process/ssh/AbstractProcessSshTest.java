package com.platformlib.process.ssh;

import com.platformlib.test.docker.ssh.DockerSshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractProcessSshTest {
    @BeforeAll
    public static void startSshServer() {
        DockerSshServer.startDockerSshServer();
    }

    @AfterAll
    public static void stopSshServer() {
        DockerSshServer.stopDockerSshServer();
    }
}
