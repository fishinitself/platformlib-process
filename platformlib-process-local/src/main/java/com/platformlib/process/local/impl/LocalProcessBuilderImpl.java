package com.platformlib.process.local.impl;

import com.platformlib.process.builder.impl.DefaultProcessBuilder;
import com.platformlib.process.executor.ProcessExecutor;
import com.platformlib.process.local.builder.LocalProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Implementation {@link LocalProcessBuilder}.
 */
@SuppressWarnings({"unchecked", "PMD.LawOfDemeter", "PMD.AvoidFieldNameMatchingMethodName"})
public class LocalProcessBuilderImpl extends DefaultProcessBuilder implements LocalProcessBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProcessBuilderImpl.class);

    private boolean useCurrentJava;

    @Override
    public LocalProcessBuilder useCurrentJava() {
        this.useCurrentJava = true;
        return this;
    }

    @Override
    public ProcessExecutor build() {
        if (useCurrentJava && getCommand().isPresent()) {
            LOGGER.error("Mutual exclusive using current java and command {}", getCommand().get());
            throw new IllegalStateException("Mutual exclusive set command and using current java");
        }
        if (useCurrentJava) {
            final String javaCommand = System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator + "java" + (isWindowsPlatform() ? ".exe" : "");
            LOGGER.trace("Use as command current java {}", javaCommand);
            command(javaCommand);
        }
        return new LocalProcessExecutor(this);
    }

    @Override
    protected boolean isWindowsPlatform() {
        return ";".equals(File.pathSeparator);
    }
}
