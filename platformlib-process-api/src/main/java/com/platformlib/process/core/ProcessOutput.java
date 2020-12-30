package com.platformlib.process.core;

import java.util.Collection;

public interface ProcessOutput {
    boolean isOverflowed();
    Collection<String> getOutput();
}
