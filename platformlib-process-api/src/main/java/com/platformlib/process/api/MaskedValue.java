package com.platformlib.process.api;

import javax.security.auth.Destroyable;

/**
 * A value which should be masked before logging.
 * For example: password.
 * The method {@link Object#toString()} should return masked value.
 */
public interface MaskedValue extends Destroyable {
    /**
     * Get source value.
     * @return Returns source value
     */
    String getSourceValue();
}
