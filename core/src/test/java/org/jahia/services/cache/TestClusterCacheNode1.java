/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.cache;

/**
 * First node for testing
 * User: Serge Huber
 * Date: Jul 18, 2005
 * Time: 9:45:23 AM
 * Copyright (C) Jahia Inc.
 */
public class TestClusterCacheNode1 extends AbstractClusterCacheTest {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (TestClusterCacheNode1.class);

    public synchronized void testClusterCacheNode1() throws InterruptedException {
        logger.info("Sending first value...");
        cache.put("1", "1");
        cacheService.syncClusterNow();
        logger.info("Waiting for response value...");
        waitForValue("2", 100000, 100);
    }
}
