package org.jahia.test.services.readonly;

import org.jahia.settings.readonlymode.ReadOnlyModeCapable;

/**
 * A ReadOnlyModeCapable that should be used as a Spring bean to be recognizable by the ReadOnlyModeController, but whose actual implementation is supplied as a parameter.
 *
 * Created by Kevan
 */
public class ReadOnlyModeCapablePlaceholder implements ReadOnlyModeCapable {

    private ReadOnlyModeSwitchImplementation readOnlyModeSwitchImplementation;

    @Override
    public void onReadOnlyModeChanged(boolean enable, long timeout) {
        if (readOnlyModeSwitchImplementation != null) {
            readOnlyModeSwitchImplementation.switchReadOnlyMode(enable);
        }
    }

    @Override
    public int getReadOnlyModePriority() {
        return 0;
    }

    void setReadOnlyModeSwitchImplementation(ReadOnlyModeSwitchImplementation readOnlyModeSwitchImplementation) {
        this.readOnlyModeSwitchImplementation = readOnlyModeSwitchImplementation;
    }

    /**
     * Actual read only mode switch implementation.
     */
    interface ReadOnlyModeSwitchImplementation {

        /**
         * Switch read only mode status.
         *
         * @param enabled Whether read mode should be enabled or disabled
         */
        void switchReadOnlyMode(boolean enable);
    }
}
