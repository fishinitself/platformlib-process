package com.platformlib.process.core;

import com.platformlib.process.api.MaskedValue;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Password which value is masked with ***** in logs.
 */
public final class MaskedPassword implements MaskedValue {
    private final byte[] value;
    private boolean destroyed;

    /**
     * Instance factory.
     * @param value to be masked in logs
     * @return Return new instance
     */
    public static MaskedPassword of(final String value) {
        return new MaskedPassword(value.getBytes(Charset.defaultCharset()));
    }

    /**
     * Constructor.
     * @param value value to be masked in logs
     */
    public MaskedPassword(final byte[] value) {
        this.value = new byte[value.length];
        System.arraycopy(value, 0, this.value, 0, value.length);
    }

    @Override
    public String getSourceValue() {
        return new String(value, Charset.defaultCharset());
    }

    @Override
    public String toString() {
        return "*****";
    }

    @Override
    public void destroy() {
        //Not enough, do it twice and avoid compiler optimization
        Arrays.fill(value, (byte) 0);
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
}
