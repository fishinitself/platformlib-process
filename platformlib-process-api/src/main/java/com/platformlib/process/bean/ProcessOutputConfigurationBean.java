package com.platformlib.process.bean;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;

import java.util.Optional;

/**
 * Bean class for {@link ProcessOutputConfiguration}.
 */
public class ProcessOutputConfigurationBean implements ProcessOutputConfiguration  {
    private Integer headSize;
    private Integer tailSize;

    @Override
    public Optional<Integer> getHeadSize() {
        return Optional.ofNullable(headSize);
    }

    @Override
    public Optional<Integer> getTailSize() {
        return Optional.ofNullable(tailSize);
    }

    public void setHeadSize(final Integer headSize) {
        this.headSize = headSize;
    }

    public void setTailSize(final Integer tailSize) {
        this.tailSize = tailSize;
    }
}
