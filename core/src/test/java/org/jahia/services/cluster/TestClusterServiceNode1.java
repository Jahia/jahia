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
 package org.jahia.services.cluster;

/**
 * User: Serge Huber
 * Date: Jul 25, 2005
 * Time: 7:54:19 PM
 * Copyright (C) Jahia Inc.
 */
public class TestClusterServiceNode1 extends AbstractClusterServiceTest {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (TestClusterServiceNode1.class);

    public void testClusterServiceNode1() throws InterruptedException {
        logger.debug("Sending first value...");
        sendMessage("value1");
        logger.debug("Waiting for value...");
        logger.debug("Received value: " + waitForValue(100000, 50));
    }
}
