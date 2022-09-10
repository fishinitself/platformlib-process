package com.platformlib.process.local;

import com.platformlib.process.builder.ProcessBuilder;
import com.platformlib.process.configurator.ProcessOutputConfigurator;
import com.platformlib.process.configurator.ProcessOutputLoggerConfigurator;
import com.platformlib.process.factory.ProcessBuilders;
import com.platformlib.process.local.specification.LocalProcessSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class LocalGroovyCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalGroovyCommand.class);
    private static final String GROOVY_JAR_RESOURCE_NAME = "/groovy/groovy-3.0.12.jar";
    public static final Path GROOVY_JAR_PATH;
    private static final Map<String, Path> GROOVY_SCRIPS = new ConcurrentHashMap<>();

    static {
        try {
            GROOVY_JAR_PATH = Paths.get(Objects.requireNonNull(LocalGroovyCommand.class.getResource(GROOVY_JAR_RESOURCE_NAME), "Groovy library hasn't been found in project resources").toURI());
            if (!Files.isRegularFile(GROOVY_JAR_PATH)) {
                throw new IllegalStateException("The groovy jar hasn't been found in resources at '"  + GROOVY_JAR_PATH + "'. Check that " + GROOVY_JAR_RESOURCE_NAME + " resource is acceptable in java");
            }
        } catch (final URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
    }

    public static ProcessBuilder newGroovyCommand(final String scriptName, final Object... commandLineArguments) {
        return ProcessBuilders.newProcessBuilder(LocalProcessSpec.CURRENT_JAVA_COMMAND)
                .logger(logger -> logger.logger(LOGGER))
                .logger(ProcessOutputLoggerConfigurator::unlimited)
                .processInstance(ProcessOutputConfigurator::unlimited)
                .commandAndArguments("-jar", GROOVY_JAR_PATH, groovyScript(scriptName))
                .commandAndArguments(commandLineArguments);
    }

    static Path groovyScript(final String scriptName) {
        return GROOVY_SCRIPS.computeIfAbsent(scriptName, scriptResourceName -> {
            try {
                final Path groovyScriptPath = Paths.get(LocalGroovyCommand.class.getResource("/groovy-scripts/" + scriptResourceName).toURI());
                if (!Files.isRegularFile(groovyScriptPath)) {
                    throw new IllegalStateException("The script " + scriptName + " hasn't been found at " + groovyScriptPath);
                }
                return groovyScriptPath;
            } catch (final URISyntaxException uriSyntaxException) {
                throw new IllegalStateException(uriSyntaxException);
            }
        });
    }
}
