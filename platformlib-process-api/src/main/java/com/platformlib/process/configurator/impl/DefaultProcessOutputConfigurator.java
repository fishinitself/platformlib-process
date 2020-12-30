package com.platformlib.process.configurator.impl;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;
import com.platformlib.process.configurator.ProcessOutputConfigurator;

import java.util.Optional;

public class DefaultProcessOutputConfigurator implements ProcessOutputConfigurator, ProcessOutputConfiguration {
    private Integer headSize;
    private Integer tailSize;

    @Override
    public void headLimit(final int limit) {
        this.headSize = limit;
    }

    @Override
    public void tailLimit(final int limit) {
        this.tailSize = limit;
    }

    @Override
    public void unlimited() {
        headLimit(-1);
        tailLimit(-1);
    }

    @Override
    public Optional<Integer> getHeadSize() {
        return Optional.ofNullable(headSize);
    }

    @Override
    public Optional<Integer> getTailSize() {
        return Optional.ofNullable(tailSize);
    }
}
