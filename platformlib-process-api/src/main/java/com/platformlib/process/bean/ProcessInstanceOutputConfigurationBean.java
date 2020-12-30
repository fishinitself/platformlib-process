package com.platformlib.process.bean;

import com.platformlib.process.configuration.instance.ProcessInstanceOutputConfiguration;

import java.util.Optional;

/**
 * Bean class for {@link ProcessInstanceOutputConfiguration}.
 */
public class ProcessInstanceOutputConfigurationBean extends ProcessOutputConfigurationBean implements ProcessInstanceOutputConfiguration  {
    private Integer limit;

    @Override
    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    /**
     * Configure process instance output.
     * @param processInstanceOutputConfiguration process output configuration
     */
    @SuppressWarnings("PMD.LawOfDemeter")
    public void configure(final ProcessInstanceOutputConfiguration processInstanceOutputConfiguration) {
        processInstanceOutputConfiguration.getLimit().ifPresent(this::setLimit);
        processInstanceOutputConfiguration.getHeadSize().ifPresent(this::setHeadSize);
        processInstanceOutputConfiguration.getTailSize().ifPresent(this::setTailSize);
    }
}
