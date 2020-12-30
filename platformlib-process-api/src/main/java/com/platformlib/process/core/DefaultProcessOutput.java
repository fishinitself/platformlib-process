package com.platformlib.process.core;

import com.platformlib.process.configuration.output.ProcessOutputConfiguration;

import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultProcessOutput implements ProcessOutput, Consumer<String> {
    private final Collection<OutputStream> outputStreams;
    private final Consumer<String> lineConsumer;

    private final Queue<String> headMax;
    private final Queue<String> tailMax;

    private final int headSize;
    private final int tailSize;

    public DefaultProcessOutput(final Collection<OutputStream> outputStreams,
                                final Consumer<String> lineConsumer,
                                final ProcessOutputConfiguration outputConfiguration
                                ) {
        this.outputStreams = outputStreams;
        this.lineConsumer = lineConsumer;
        headSize = outputConfiguration == null ? 0 : outputConfiguration.getHeadSize().orElse(0);
        tailSize = outputConfiguration == null ? 0 : outputConfiguration.getTailSize().orElse(0);
        headMax = new LimitedQueue<>(headSize);
        tailMax = new LimitedQueue<>(tailSize > 0 ? tailSize + 1 : tailSize);
    }

    public Collection<OutputStream> getOutputStreams() {
        return outputStreams;
    }

    public boolean isAcceptReady() {
        return headSize != 0 || tailSize != 0 || lineConsumer != null;
    }

    @Override
    public void accept(final String s) {
        if (headSize < 0 || headSize > 0 && headMax.size() < headSize) {
            headMax.offer(s);
        } else {
            if (tailSize != 0) {
                tailMax.offer(s);
            }
        }
        if (lineConsumer != null) {
            lineConsumer.accept(s);
        }
    }

    @Override
    public boolean isOverflowed() {
        return (headSize > 0 || tailSize > 0) && Math.max(headSize, 0) + Math.max(tailSize, 0) < headMax.size() + tailMax.size();
    }

    @Override
    public Collection<String> getOutput() {
        return Stream.concat(headMax.stream(), tailMax.stream().skip(tailSize > 0 && tailMax.size() > tailSize ? 1 : 0)).collect(Collectors.toList());
    }

    private static class LimitedQueue<E> extends LinkedList<E> {
        private final int limit;

        LimitedQueue(final int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E e) {
            if (limit > 0 && super.size() == limit) {
                super.remove();
            }
            return super.add(e);
        }
    }
}
