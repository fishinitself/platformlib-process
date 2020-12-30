package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.instance.ProcessInstanceConfiguration;
import com.platformlib.process.configuration.instance.ProcessInstanceOutputConfiguration;
import com.platformlib.process.configurator.ProcessInstanceConfigurator;
import com.platformlib.process.configurator.ProcessInstanceOutputConfigurator;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Default {@link ProcessInstanceConfigurator} implementation.
 */
public class DefaultProcessInstanceConfigurator extends DefaultProcessInstanceOutputConfigurator implements ProcessInstanceConfigurator, ProcessInstanceConfiguration {
    private DefaultProcessInstanceOutputConfigurator stdOutInstanceOutputConfigurator = new DefaultProcessInstanceOutputConfigurator();
    private DefaultProcessInstanceOutputConfigurator stdErrInstanceOutputConfigurator = new DefaultProcessInstanceOutputConfigurator();

    @Override
    public void unlimited() {
        stdOutInstanceOutputConfigurator.unlimited();
        stdErrInstanceOutputConfigurator.unlimited();
    }

    @Override
    public void headLimit(int limit) {
        stdOutInstanceOutputConfigurator.headLimit(limit);
        stdErrInstanceOutputConfigurator.headLimit(limit);
    }

    @Override
    public void tailLimit(int limit) {
        stdOutInstanceOutputConfigurator.tailLimit(limit);
        stdErrInstanceOutputConfigurator.tailLimit(limit);
    }

    @Override
    public void limit(int limit) {
        super.limit(limit);
        stdOutInstanceOutputConfigurator.limit(limit);
        stdErrInstanceOutputConfigurator.limit(limit);
    }

    @Override
    public Optional<ProcessInstanceOutputConfiguration> getStdOutConfiguration() {
        return Optional.ofNullable(stdOutInstanceOutputConfigurator);
    }

    @Override
    public Optional<ProcessInstanceOutputConfiguration> getStdErrConfiguration() {
        return Optional.ofNullable(stdErrInstanceOutputConfigurator);
    }

    @Override
    public void stdOut(final Consumer<ProcessInstanceOutputConfigurator> stdOutInstanceConfigurator) {
        stdOutInstanceConfigurator.accept(this.stdOutInstanceOutputConfigurator);
    }

    @Override
    public void stdErr(final Consumer<ProcessInstanceOutputConfigurator> stdErrInstanceConfigurator) {
        stdErrInstanceConfigurator.accept(this.stdErrInstanceOutputConfigurator);
    }
}
