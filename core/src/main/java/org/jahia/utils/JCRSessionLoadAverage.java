package org.jahia.utils;

import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;

/**
 * A tracker for JCR sessions load.
 */
public class JCRSessionLoadAverage extends LoadAverage {

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JCRSessionLoadAverage.class);
    private static transient JCRSessionLoadAverage instance = null;

    public JCRSessionLoadAverage(String threadName) {
        super(threadName);
        instance = this;
    }

    public static JCRSessionLoadAverage getInstance() {
        return instance;
    }

    @Override
    public double getCount() {
        return (double) JCRSessionWrapper.getActiveSessions().get();
    }

    @Override
    public void tickCallback() {
        if (oneMinuteLoad > 15.0) {
            logger.info("Jahia JCR Session Load = " + oneMinuteLoad + " " + fiveMinuteLoad + " " + fifteenMinuteLoad);
        }
    }
}
