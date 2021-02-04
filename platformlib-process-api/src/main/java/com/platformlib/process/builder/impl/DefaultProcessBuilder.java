package com.platformlib.process.builder.impl;

import com.platformlib.process.configuration.ProcessConfiguration;
import com.platformlib.process.configuration.dryrun.ProcessDryRunConfiguration;
import com.platformlib.process.configurator.ProcessDryRunConfigurator;
import com.platformlib.process.configurator.impl.DefaultProcessDryRunConfigurator;
import com.platformlib.process.configurator.impl.DefaultProcessInstanceConfigurator;
import com.platformlib.process.configuration.instance.ProcessInstanceConfiguration;
import com.platformlib.process.configuration.logger.ProcessLoggerConfiguration;
import com.platformlib.process.configurator.ProcessInstanceConfigurator;
import com.platformlib.process.configurator.ProcessLoggerConfigurator;
import com.platformlib.process.configurator.impl.DefaultProcessLoggerConfigurator;
import com.platformlib.process.handler.ProcessDestroyerHandler;
import com.platformlib.process.enums.ExecutionMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.platformlib.process.builder.ProcessBuilder;

/**
 * Default process builder.
 * Implements most of methods required for process building.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public abstract class DefaultProcessBuilder implements ProcessBuilder, ProcessConfiguration {
    private String name = Thread.currentThread().getName();
    private final Collection<String> winExtensionMapping = new LinkedList<>();
    private final Collection<String> nixExtensionMapping = new LinkedList<>();

    private Consumer<String> stdOutConsumer;
    private Consumer<String> stdErrConsumer;
    private Duration executionTimeout;
    private String command;
    private final Collection<Object> commandAndArguments = new ArrayList<>();

    private boolean rawExecution = true;
    private ProcessDestroyerHandler processDestroyerHandler;

    private InputStream processStandardInputStream;
    private OutputStream stdOutRedirectStream;
    private OutputStream stdErrRedirectStream;

    private Object workDirectory;
    private Executor executor;

    private DefaultProcessDryRunConfigurator processDryRunConfigurator;

    private final Map<String, String> envVariables = new ConcurrentHashMap<>();

    private final Collection<Function<String, String>> maskAppliers = new ArrayList<>();

    private final DefaultProcessLoggerConfigurator processLoggerConfigurator = new DefaultProcessLoggerConfigurator();
    private final DefaultProcessInstanceConfigurator processInstanceConfigurator = new DefaultProcessInstanceConfigurator();

    /**
     * Map of argument class and function to apply to it before putting value into logging.
     * There functions are applied on arguments before their logging.
     * Any sensitive argument such as user passwords should be given as specialized object and configured functions for their processing before putting them into logging system.
     * Without logger argument processors all given arguments are put in the logging as is.
     */
    private final Map<Class<?>, Function<Object, String>> loggerArgumentFunctionsMap = new ConcurrentHashMap<>();

    /**
     * Map of argument class and functions to apply to it before adding to command line argument.
     * These functions are used for creating command line for execution.
     * Any non standard object instances given as command line arguments which don't support ready to use {@link Object#toString()} method should be mapped to argument processor function which is used for converting it into {@link String} for executed command line.
     */
    private final Map<Class<?>, Function<Object, String>> argumentFunctionsMap = new ConcurrentHashMap<>();

    private final Collection<Supplier<String>> extensionSuppliers = new ArrayList<>();

    protected abstract boolean isWindowsPlatform();

    @Override
    public DefaultProcessBuilder name(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public DefaultProcessBuilder workDirectory(final String workDirectory) {
        this.workDirectory = workDirectory;
        return this;
    }

    @Override
    public DefaultProcessBuilder workDirectory(final Path workDirectory) {
        this.workDirectory = workDirectory;
        return this;
    }

    @Override
    public DefaultProcessBuilder envVariables(final Map<String, String> envVariables) {
        this.envVariables.putAll(envVariables);
        return this;
    }

    @Override
    public DefaultProcessBuilder defaultExtensionMapping() {
        winExtensionMapping.add(CMD_EXTENSION);
        winExtensionMapping.add(BAT_EXTENSION);
        winExtensionMapping.add(EXE_EXTENSION);
        nixExtensionMapping.add(SH_EXTENSION);
        nixExtensionMapping.add(BASH_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder mapBashExtension() {
        winExtensionMapping.add(BASH_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder mapShExtension() {
        nixExtensionMapping.add(SH_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder mapBatExtension() {
        winExtensionMapping.add(BAT_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder mapCmdExtension() {
        winExtensionMapping.add(CMD_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder mapExeExtension() {
        winExtensionMapping.add(EXE_EXTENSION);
        return this;
    }

    @Override
    public DefaultProcessBuilder stdOutConsumer(final Consumer<String> stdOutConsumer) {
        this.stdOutConsumer = stdOutConsumer;
        return this;
    }

    @Override
    public DefaultProcessBuilder stdErrConsumer(final Consumer<String> stdErrConsumer) {
        this.stdErrConsumer = stdErrConsumer;
        return this;
    }

    @Override
    public DefaultProcessBuilder standardInput(final InputStream processStandardInputStream) {
        this.processStandardInputStream = processStandardInputStream;
        return this;
    }

    @Override
    public DefaultProcessBuilder command(final String command) {
        this.command = command;
        return this;
    }

    @Override
    public DefaultProcessBuilder commandAndArguments(final Object... commandAndArguments) {
        Collections.addAll(this.commandAndArguments, commandAndArguments);
        return this;
    }

    @Override
    public DefaultProcessBuilder withExecutor(final Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public DefaultProcessBuilder executionTimeout(final Duration timeout) {
        this.executionTimeout = timeout;
        return this;
    }

    @Override
    public DefaultProcessBuilder addMaskApplier(final Function<String, String> maskApplier) {
        maskAppliers.add(maskApplier);
        return this;
    }

    @Override
    public DefaultProcessBuilder redirectStandardOutput(final OutputStream outputStream) {
        this.stdOutRedirectStream = outputStream;
        return this;
    }

    @Override
    public DefaultProcessBuilder redirectStandardError(final OutputStream outputStream) {
        this.stdErrRedirectStream = outputStream;
        return this;
    }

    @Override
    public DefaultProcessBuilder rawExecution() {
        this.rawExecution = true;
        return this;
    }

    @Override
    public DefaultProcessBuilder processDestroyerHandler(final ProcessDestroyerHandler processDestroyerHandler) {
        this.processDestroyerHandler = processDestroyerHandler;
        return this;
    }

    @Override
    public DefaultProcessBuilder dryRun(final Consumer<ProcessDryRunConfigurator> processDryRunConfigurator) {
        if (this.processDryRunConfigurator == null) {
            this.processDryRunConfigurator = new DefaultProcessDryRunConfigurator();
        }
        processDryRunConfigurator.accept(this.processDryRunConfigurator);
        return this;
    }

    @Override
    public Collection<Function<String, String>> getMaskAppliers() {
        return maskAppliers;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public Optional<String> getWorkDirectory() {
        return Optional.ofNullable(workDirectory).map(Object::toString);
    }

    @Override
    public Optional<Executor> getExecutor() {
        return Optional.ofNullable(executor);
    }

    @Override
    public Optional<Duration> getExecutionTimeout() {
        return Optional.ofNullable(executionTimeout);
    }

    @Override
    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    @Override
    public Optional<Consumer<String>> getStdOutConsumer() {
        return Optional.ofNullable(stdOutConsumer);
    }

    @Override
    public Optional<Consumer<String>> getStdErrConsumer() {
        return Optional.ofNullable(stdErrConsumer);
    }

    @Override
    public Collection<Object> getCommandAndArguments() {
        final List<Object> cla = new ArrayList<>();
        if (command != null) {
            cla.add(command);
        }
        cla.addAll(commandAndArguments);
        return cla;
    }

    @Override
    public Collection<OutputStream> getStandardOutputRedirects() {
        return stdOutRedirectStream == null ? Collections.emptyList() : Collections.singleton(stdOutRedirectStream);
    }

    @Override
    public Collection<OutputStream> getStandardErrorRedirects() {
        return stdErrRedirectStream == null ? Collections.emptyList() : Collections.singleton(stdErrRedirectStream);
    }

    @Override
    public ExecutionMode getExecution() {
        return rawExecution ? ExecutionMode.RAW : ExecutionMode.NORMAL;
    }

    @Override
    public Optional<InputStream> getStdIn() {
        return Optional.ofNullable(processStandardInputStream);
    }

    @Override
    public Optional<ProcessDestroyerHandler> getProcessDestroyerHandler() {
        return Optional.ofNullable(processDestroyerHandler);
    }

    @Override
    public Optional<ProcessDryRunConfiguration> getDryRunConfiguration() {
        return Optional.ofNullable(processDryRunConfigurator);
    }

    @Override
    public DefaultProcessBuilder logger(final Consumer<ProcessLoggerConfigurator> processLoggerConfigurator) {
        processLoggerConfigurator.accept(this.processLoggerConfigurator);
        return this;
    }

    @Override
    public DefaultProcessBuilder processInstance(final Consumer<ProcessInstanceConfigurator> processInstanceConfigurator) {
        processInstanceConfigurator.accept(this.processInstanceConfigurator);
        return this;
    }

    protected Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }

    @Override
    public Optional<ProcessInstanceConfiguration> getProcessInstanceConfiguration() {
        return Optional.ofNullable(processInstanceConfigurator);
    }

    @Override
    public Optional<ProcessLoggerConfiguration> getProcessLoggerConfiguration() {
        return Optional.ofNullable(processLoggerConfigurator);
    }

    @Override
    public ProcessBuilder extensionSupplier(final Supplier<String> supplier) {
        extensionSuppliers.add(supplier);
        return this;
    }

    @Override
    public Collection<Supplier<String>> getExtensionMappers() {
        if (isWindowsPlatform()) {
            return Stream.concat(extensionSuppliers.stream(), winExtensionMapping.stream().map(DefaultExtensionSupplier::new)).collect(Collectors.toList());
        }
        return Stream.concat(extensionSuppliers.stream(), nixExtensionMapping.stream().map(DefaultExtensionSupplier::new)).collect(Collectors.toList());
    }

    private static final class DefaultExtensionSupplier implements Supplier<String> {
        private final String extension;

        private DefaultExtensionSupplier(final String extension) {
            this.extension = extension;
        }

        @Override
        public String get() {
            return extension;
        }
    }
}
