package com.platformlib.process.core;

import com.platformlib.process.configuration.logger.ProcessOutputLoggerConfiguration;
import com.platformlib.process.enums.ProcessThreadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

@SuppressWarnings("PMD.LawOfDemeter")
public class AsyncProcessOutputListener implements Runnable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProcessOutputListener.class);
    private static final int BUFFER_SIZE = 8192;
    private final byte[] readBytesBuffer = new byte[BUFFER_SIZE];
    private InputStream inputStream = null;
    private final CountDownLatch runLatch = new CountDownLatch(1);
    private final CountDownLatch completeLatch = new CountDownLatch(1);
    //TODO Make it configurable
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    private final Function<String, String> stdOutFirstLineFunction;
    private boolean firstLineConsumed;

    private final DefaultProcessOutput processOutput;
    private final boolean outputAcceptReady;

    private final ProcessOutputLoggerConfiguration processOutputLoggerConfiguration;
    private final CycledBuffer<String> tailBuffer;
    private int headProcessed;
    private final ProcessThreadType processThreadType;

    public AsyncProcessOutputListener(final Executor executor,
                                      final ProcessThreadType processThreadType,
                                      final ProcessOutputLoggerConfiguration processOutputLoggerConfiguration,
                                      final DefaultProcessOutput processOutput) {
        this(executor, processThreadType, processOutputLoggerConfiguration, processOutput, null);
    }

    public AsyncProcessOutputListener(final Executor executor,
                                      final ProcessThreadType processThreadType,
                                      final ProcessOutputLoggerConfiguration processOutputLoggerConfiguration,
                                      final DefaultProcessOutput processOutput,
                                      final Function<String, String> stdOutFirstLineFunction) {
        this.processThreadType = processThreadType;
        this.processOutputLoggerConfiguration = Objects.requireNonNull(processOutputLoggerConfiguration);
        this.processOutput = processOutput;
        executor.execute(this);
        this.stdOutFirstLineFunction = stdOutFirstLineFunction;
        outputAcceptReady = processOutput.isAcceptReady();
        tailBuffer = processOutputLoggerConfiguration.getTailSize().orElse(0) > 0 ? new CycledBuffer<>(processOutputLoggerConfiguration.getTailSize().get()) : null;
    }

    public void startListening(final InputStream inputStream) {
        this.inputStream = inputStream;
        runLatch.countDown();
    }

    @Override
    public void run() {
        processOutputLoggerConfiguration.getProcessThreadInitializer().ifPresent(initializer -> initializer.accept(processThreadType));
        final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            runLatch.await();
            LOGGER.trace("[{}] Start process output listening", processThreadType.getThreadName());
            while (inputStream != null) {
                final int len = inputStream.read(readBytesBuffer);
                if (len < 0) {
                    byteBuffer.flip();
                    if (byteBuffer.hasRemaining()) {
                        consumeLine(processThreadType.getThreadName(), byteBuffer);
                    }
                    byteBuffer.clear();
                    break;
                }
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("[{}] Read {} byte(s) {}", processThreadType.getThreadName(), len, Arrays.copyOf(readBytesBuffer, len));
                }
                if (len > 0) {
                    //TODO We can stuck here if the receiver will not read. Do asynchronous call here
                    for (OutputStream outputStream: processOutput.getOutputStreams()) {
                        outputStream.write(readBytesBuffer, 0, len);
                    }
                }
                for (int i = 0; i < len; i++) {
                    if (readBytesBuffer[i] == '\n') {
                        byteBuffer.flip();
                            if (stdOutFirstLineFunction != null && !firstLineConsumed) {
                                firstLineConsumed = true;
                                final String firstLine =  getStringLine(byteBuffer);
                                final String appliedLine = stdOutFirstLineFunction.apply(firstLine);
                                if (appliedLine != null) {
                                    consumeLine(processThreadType.getThreadName(), appliedLine);
                                } else {
                                    LOGGER.trace("First line consumer accepted: {}", firstLine);
                                }
                            } else {
                                consumeLine(processThreadType.getThreadName(), byteBuffer);
                            }
                        byteBuffer.clear();
                    } else if (readBytesBuffer[i] != '\r') {
                        if (byteBuffer.remaining() > 0) {
                            byteBuffer.put(readBytesBuffer[i]);
                        } else {
                            byteBuffer.flip();
                            final String line = getStringLine(byteBuffer) + "<<No new line break>>";
                            LOGGER.warn(line);
                            consumeLine(processThreadType.getThreadName(), line);
                            byteBuffer.clear();
                            byteBuffer.put(readBytesBuffer[i]);
                        }
                    }
                }
            }
        } catch (final RuntimeException | IOException | InterruptedException exception) {
            LOGGER.error("[" + processThreadType.getThreadName() + "] Fail to read channel", exception);
            byteBuffer.flip();
            if (byteBuffer.hasRemaining()) {
                consumeLine(processThreadType.getThreadName(), byteBuffer);
            }
            byteBuffer.clear();
        } finally {
            completeLatch.countDown();
            LOGGER.trace("[{}] Stop process output listening", processThreadType.getThreadName());
        }
    }

    private static String getStringLine(final ByteBuffer byteBuffer) {
        //TODO Take into process output encoding
        return new String(byteBuffer.array(), 0, byteBuffer.remaining(), StandardCharsets.UTF_8);
    }

    private void consumeLine(final String name, final ByteBuffer byteBuffer) {
        consumeLine(name, getStringLine(byteBuffer));
    }

    private void consumeLine(final String name, final String line) {
        if (processOutputLoggerConfiguration.getLogger().orElse(LOGGER) == LOGGER && LOGGER.isTraceEnabled()) {
            LOGGER.trace("[{}] Process output: {}", name, line);
        }
        if (!processOutputLoggerConfiguration.getHeadSize().isPresent() && !processOutputLoggerConfiguration.getTailSize().isPresent()) {
            processOutputLoggerConfiguration.getLogger().ifPresent(logger -> logger.debug("[{}] Process output: {}", name, line));
        } else if (processOutputLoggerConfiguration.getHeadSize().isPresent() && (processOutputLoggerConfiguration.getHeadSize().get() < 0 || headProcessed < processOutputLoggerConfiguration.getHeadSize().get())) {
            headProcessed++;
            processOutputLoggerConfiguration.getLogger().orElse(LOGGER).debug("[{}] Process output: {}", name, line);
        } else if (processOutputLoggerConfiguration.getTailSize().orElse(0) > 0) {
            tailBuffer.add(line);
        }
        if (outputAcceptReady) {
            processOutput.accept(line);
        }
    }

    @Override
    public void close() throws IOException {
        runLatch.countDown();
        try {
            completeLatch.await(SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException interruptedException) {
            processOutputLoggerConfiguration.getLogger().orElse(LOGGER).debug("[{}] Process output listener wasn't shut down due to timeout", processThreadType.getThreadName());
        }
        if (tailBuffer != null) {
            tailBuffer.getValues().forEach(line -> processOutputLoggerConfiguration.getLogger().orElse(LOGGER).debug("[{}] Process output: {}", processThreadType.getThreadName(), line));
        }
        //TODO The same thing with stuck, make this call asynchronously
        //TODO be careful when closing, catch exception and process it correctly
        for (final OutputStream outputStream: processOutput.getOutputStreams()) {
            outputStream.flush();
            outputStream.close();
        }
        inputStream = null;
    }

    private static class CycledBuffer<T> {
        private int position;
        private final Object[] items;

        CycledBuffer(final int size) {
            this.items = new Object[size];
        }

        void add(final T item) {
            items[position++ % items.length] = item;
        }

        @SuppressWarnings("unchecked")
        Collection<T> getValues() {
            final int len = Math.min(position, items.length);
            final Collection<T> result = new ArrayList<>(len);
            int resultPosition = position - len;
            for (int i = 0; i < len; i++) {
                result.add((T) items[resultPosition++ % items.length]);
            }
            return result;
        }
    }
}
