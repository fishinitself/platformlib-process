package com.platformlib.process.bean;

import com.platformlib.process.configuration.instance.ProcessInstanceConfiguration;
import com.platformlib.process.configuration.instance.ProcessInstanceOutputConfiguration;
import com.platformlib.process.configurator.ProcessInstanceOutputConfigurator;

import java.util.Optional;

/**
 * Bean class for {@link ProcessInstanceConfiguration}.
 */
public class ProcessInstanceConfigurationBean implements ProcessInstanceConfiguration {
    private final ProcessInstanceOutputConfigurationBean stdOutConfiguration;
    private final ProcessInstanceOutputConfigurationBean stderrConfiguration;

    /**
     * Default bean constructor.
     * @param stdOutConfiguration process stdout configuration
     * @param stderrConfiguration process stderr configuration
     */
    public ProcessInstanceConfigurationBean(final ProcessInstanceOutputConfigurationBean stdOutConfiguration,
                                            final ProcessInstanceOutputConfigurationBean stderrConfiguration) {
        this.stdOutConfiguration = stdOutConfiguration;
        this.stderrConfiguration = stderrConfiguration;
    }

    /**
     * Configure process instance output.
     * @param outputConfigurator  process instance output configurator
     */
    public void configure(final ProcessInstanceOutputConfigurator outputConfigurator) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Optional<ProcessInstanceOutputConfiguration> getStdOutConfiguration() {
        return Optional.ofNullable(stdOutConfiguration);
    }

    @Override
    public Optional<ProcessInstanceOutputConfiguration> getStdErrConfiguration() {
        return Optional.ofNullable(stderrConfiguration);
    }
}
