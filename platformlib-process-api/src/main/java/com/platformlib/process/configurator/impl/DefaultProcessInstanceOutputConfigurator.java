package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.instance.ProcessInstanceOutputConfiguration;
import com.platformlib.process.configurator.ProcessInstanceOutputConfigurator;

import java.util.Optional;

public class DefaultProcessInstanceOutputConfigurator extends DefaultProcessOutputConfigurator implements ProcessInstanceOutputConfigurator, ProcessInstanceOutputConfiguration {
    private Integer limit;

    @Override
    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    @Override
    public void limit(int limit) {
        if (limit == -1) {
            unlimited();
        } else {
            headLimit(limit);
            tailLimit(limit);
        }
        this.limit = limit;
    }
}
