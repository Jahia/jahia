package org.jahia.test.services.readonly;

import org.jahia.settings.readonlymode.ReadOnlyModeCapable;

/**
 * This service, is a test service that implement ReadOnlyModeCapable to test different case of switching readonly mode ON or OFF
 *
 * Created by Kevan
 */
public class FullReadOnlyModeTestService implements ReadOnlyModeCapable {

    private TestCallback testCallback;

    @Override
    public void onReadOnlyModeChanged(boolean readOnlyModeIsOn, long timeout) {
        if (testCallback != null) {
            testCallback.doExecute();
        }
    }

    @Override
    public int getReadOnlyModePriority() {
        return 0;
    }

    public TestCallback getTestCallback() {
        return testCallback;
    }

    void setTestCallback(TestCallback testCallback) {
        this.testCallback = testCallback;
    }

    interface TestCallback {
        void doExecute();
    }
}
