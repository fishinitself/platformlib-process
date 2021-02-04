package com.platformlib.process.executor;

import com.platformlib.process.api.OperationSystemProcess;
import com.platformlib.process.builder.impl.DefaultProcessBuilder;
import com.platformlib.process.configuration.ProcessConfiguration;
import com.platformlib.process.core.DefaultOperationSystemProcess;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultProcessExecutorTest {

    @ParameterizedTest
    @MethodSource("extensionMappingData")
    void testDefaultExtensionMapping(final boolean isWindows, final String mvnVersion, final String expectedCommand) throws Exception {
        final Path mavenHome = Paths.get(DefaultProcessExecutorTest.class.getResource("/" + mvnVersion).toURI());
        final DefaultProcessExecutorStub processExecutor = new DefaultProcessExecutorStub(new DefaultProcessBuilderStub(isWindows)
                .defaultExtensionMapping()
                .commandAndArguments());
        assertThat(processExecutor.getCommandAndArgumentsToExecute(FileSystems.getDefault(), Collections.singleton(mavenHome.resolve("mvn").toString()))).containsExactly(mavenHome.resolve(expectedCommand).toString());
    }

    static Stream<Arguments> extensionMappingData() {
        return Stream.of(
                Arguments.of(true, "maven-3.0.5", "mvn.bat"),
                Arguments.of(true, "maven-3.3.9", "mvn.cmd"),
                Arguments.of(false, "maven-3.0.5", "mvn"),
                Arguments.of(false, "maven-3.3.9", "mvn"),
                Arguments.of(false, "maven-sh", "mvn.sh"),
                Arguments.of(false, "maven-bash", "mvn.bash"),
                Arguments.of(true, "maven-exe", "mvn.exe"),
                Arguments.of(false, "maven-exe", "mvn")
        );
    }

    private static final class DefaultProcessExecutorStub extends DefaultProcessExecutor {
        public DefaultProcessExecutorStub(final ProcessConfiguration processConfiguration) {
            super(processConfiguration);
        }
        @Override
        public OperationSystemProcess execute(final Object... commandAndArguments) {
            return new DefaultOperationSystemProcess();
        }
    }

    private static final class DefaultProcessBuilderStub extends DefaultProcessBuilder {
        private final boolean windowsPlatform;

        public DefaultProcessBuilderStub(final boolean windowsPlatform) {
            this.windowsPlatform = windowsPlatform;
        }

        @Override
        public ProcessExecutor build() {
            return new DefaultProcessExecutorStub(this);
        }

        @Override
        protected boolean isWindowsPlatform() {
            return windowsPlatform;
        }
    }
}
