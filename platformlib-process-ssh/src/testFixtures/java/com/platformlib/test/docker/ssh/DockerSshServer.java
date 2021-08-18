package com.platformlib.test.docker.ssh;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.configurator.ProcessOutputLoggerConfigurator;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DockerSshServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerSshServer.class);
    public static final String DOCKER_IMAGE = "linuxserver/openssh-server:version-8.6_p1-r2";
    private static OperationSystemProcess dockerProcess;
    public static final int DOCKER_SSH_PORT = 2222;

    public static void startDockerSshServer() {
        final Path sshPublicKeysPath;
        try {
            sshPublicKeysPath = Paths.get(Objects.requireNonNull(DockerSshServer.class.getResource("/ssh-public-keys")).toURI());
        } catch (final URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean startingServices = new AtomicBoolean(false);
        dockerProcess = ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                .logger(logger -> logger.logger(LOGGER))
                .logger(ProcessOutputLoggerConfigurator::unlimited)
                .processInstance(ProcessOutputConfigurator::unlimited)
                .stdOutConsumer(stdout -> {
                    if ("[services.d] starting services".equals(stdout)) {
                        startingServices.set(true);
                    } else if (startingServices.get() && "[services.d] done.".equals(stdout)) {
                        startLatch.countDown();
                    }
                })
                .commandAndArguments(
                        "docker",
                        "run",
                        "--net", "host",
                        "--name=openssh-server",
                        "--hostname=openssh-server",
                        "-e", "PUID=1000",
                        "-e", "PGID=1000",
                        "-e", "TZ=Europe/London",
                        "-e", "PASSWORD_ACCESS=true",
                        "-e", "USER_PASSWORD=secret",
                        "-e", "USER_NAME=ssh-user",
                        "-p", "127.0.0.1:" + DOCKER_SSH_PORT + ":" + DOCKER_SSH_PORT,
                        "-v", sshPublicKeysPath + ":/host-ssh-public-keys",
                        "--env", "PUBLIC_KEY_DIR=/host-ssh-public-keys",
                        DOCKER_IMAGE
                ).build().execute();
        dockerProcess.whenComplete((r, e) -> startLatch.countDown());
        try {
            if (!startLatch.await(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Fail to start docker container");
            }
            if (dockerProcess.toCompletableFuture().isDone()) {
                throw new IllegalStateException("Fail to start docker container");
            }
            LOGGER.debug("The docker container has been started");
        } catch (final InterruptedException interruptedException) {
            throw new IllegalStateException("Docker container starting gas been interrupted", interruptedException);
        }

/*
                        "-v", "/path/to/appdata/config:/config",
                        "-e", "PUBLIC_KEY=yourpublickey",
                        "  -e PUBLIC_KEY_FILE=/path/to/file `#optional` \\\n" +
                        "  -e PUBLIC_KEY_DIR=/path/to/directory/containing/_only_/pubkeys `#optional` \\\n" +
                        "  -e SUDO_ACCESS=false `#optional` \\\n" +

 */
    }

    public static void stopDockerSshServer() {
        try {
            ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                    .logger(logger -> logger.logger(LOGGER))
                    .logger(ProcessOutputLoggerConfigurator::unlimited)
                    .processInstance(ProcessOutputConfigurator::unlimited)
                    .commandAndArguments("docker", "stop", "openssh-server")
                    .build()
                    .execute().toCompletableFuture().get();
            ProcessBuilders.newProcessBuilder(LocalProcessSpec.LOCAL_COMMAND)
                    .logger(logger -> logger.logger(LOGGER))
                    .logger(ProcessOutputLoggerConfigurator::unlimited)
                    .processInstance(ProcessOutputConfigurator::unlimited)
                    .commandAndArguments("docker", "rm", "openssh-server")
                    .build()
                    .execute().toCompletableFuture().get();
            dockerProcess.toCompletableFuture().get().getExitCode();
        } catch (final ExecutionException | InterruptedException exception) {
            LOGGER.warn("An error stopping and removing docker", exception);
        }
    }
}
