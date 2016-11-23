package org.jahia.osgi;

import java.util.HashMap;

import org.osgi.framework.Bundle;

/**
 * OSGi bundle states.
 */
public enum BundleState {

    UNINSTALLED(Bundle.UNINSTALLED),
    INSTALLED(Bundle.INSTALLED),
    RESOLVED(Bundle.RESOLVED),
    STARTING(Bundle.STARTING),
    STOPPING(Bundle.STOPPING),
    ACTIVE(Bundle.ACTIVE);

    int value;

    private static final HashMap<Integer, BundleState> STATE_BY_VALUE = new HashMap<Integer, BundleState>();
    static {
        for (BundleState state : BundleState.values()) {
            STATE_BY_VALUE.put(state.toInt(), state);
        }
    }

    private BundleState(int value) {
        this.value = value;
    }

    /**
     * @return org.osgi.framework.Bundle state value corresponding to this bundle state
     */
    public int toInt() {
        return value;
    }

    /**
     * Get bundle state corresponding to an org.osgi.framework.Bundle state value.
     *
     * @param value org.osgi.framework.Bundle state constant value
     * @return Corresponding bundle state
     */
    public static BundleState fromInt(int value) {
        BundleState state = STATE_BY_VALUE.get(value);
        if (state == null) {
            throw new IllegalArgumentException(String.format("Unknown bundle state value: %s", value));
        }
        return state;
    }
}
