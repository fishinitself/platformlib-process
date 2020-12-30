package com.platformlib.process.core;

import com.platformlib.process.api.ProcessInstance;
import com.platformlib.process.exception.OutputOverflowException;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("PMD.LawOfDemeter")
public class DefaultProcessInstance implements ProcessInstance {
    private final int exitCode;
    private final Collection<String> stdOut;
    private final boolean stdOutOverflow;
    private final Collection<String> stdErr;
    private final boolean stdErrOverflow;

    public DefaultProcessInstance(final int exitCode,
                                  final ProcessOutput processStdOut,
                                  final ProcessOutput processStdErr) {
        this.exitCode = exitCode;
        this.stdOut = new ArrayList<>(processStdOut.getOutput());
        this.stdOutOverflow = processStdOut.isOverflowed();
        this.stdErr = new ArrayList<>(processStdErr.getOutput());
        this.stdErrOverflow = processStdErr.isOverflowed();
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public Collection<String> getStdOut() {
        if (stdOutOverflow) {
            throw new OutputOverflowException();
        }
        return stdOut;
    }

    @Override
    public Collection<String> getStdErr() {
        if (stdErrOverflow) {
            throw new OutputOverflowException();
        }
        return stdErr;
    }
}
