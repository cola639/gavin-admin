package com.api.framework.aspectj;

/**
 * Permission context holder using ThreadLocal (JPA friendly).
 * <p>
 * Stores and retrieves permission strings for the current thread.
 * Safer and more flexible than RequestContextHolder-based approach.
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private DataScopeContextHolder() {
    }

    /**
     * Set permission string for the current thread.
     *
     * @param permission permission string
     */
    public static void setContext(String permission) {
        CONTEXT.set(permission);
    }

    /**
     * Get permission string for the current thread.
     *
     * @return permission string or null if not set
     */
    public static String getContext() {
        return CONTEXT.get();
    }

    /**
     * Clear context after request is processed.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
