package com.platformlib.process.ssh;

import com.platformlib.test.docker.ssh.DockerSshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractProcessSshTest {
    static String CONTAINER_ID;

    @BeforeAll
    public static void startSshServer() {
        CONTAINER_ID = DockerSshServer.startDockerSshServer();
    }

    @AfterAll
    public static void stopSshServer() {
        DockerSshServer.stopDockerSshServer();
    }
}
