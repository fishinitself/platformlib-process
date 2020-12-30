package com.platformlib.process.local.specification;

/**
 * Local process specification.
 */
public final class LocalProcessSpec {
    private final boolean useCurrentJava;
    public static final LocalProcessSpec CURRENT_JAVA_COMMAND = new LocalProcessSpec(true);
    public static final LocalProcessSpec LOCAL_COMMAND = new LocalProcessSpec(false);

    /**
     * Default constructor.
     */
    public LocalProcessSpec() {
        this(false);
    }

    /**
     * Specify to use or not current java.
     * @param useCurrentJava true to use current java, false otherwise
     */
    public LocalProcessSpec(final boolean useCurrentJava) {
        this.useCurrentJava = useCurrentJava;
    }

    /**
     * Get local java using set.
     * @return Returns true is current java is specified for using, false otherwise
     */
    public boolean isUseCurrentJava() {
        return useCurrentJava;
    }
}
