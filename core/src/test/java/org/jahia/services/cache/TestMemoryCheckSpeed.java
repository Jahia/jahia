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

import junit.framework.TestCase;

/**
 * The goal of this test case is to check if we can use memory checks to
 * evict cache based on memory consumption.
 *
 * Example result :
 *
 * Time for 1000000 memory checks : 877ms
 * maxMemory   = 66650112 bytes 63MB
 * totalMemory = 66650112 bytes 63MB
 * freeMemory  = 4903408 bytes 4MB
 *
 * User: Serge Huber
 * Date: 4 mai 2006
 * Time: 18:20:44
 * Copyright (C) Jahia Inc.
 */
public class TestMemoryCheckSpeed  extends TestCase {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (TestMemoryCheckSpeed.class);

    private static final int MAX_ITERATIONS = 1000000;
    String[] junkArray = new String[MAX_ITERATIONS];

    public void testMemoryCheckSpeed() {

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = 0;
        long freeMemory = 0;
        long totalMemory = 0;

        long startTime = 0;
        long endTime = 0;
        long totalTime = 0;
        String junk = null;
        for (int i=0; i < MAX_ITERATIONS; i++ ) {
            junkArray[i] = new String("j" + i);
            startTime = System.currentTimeMillis();
            maxMemory = runtime.maxMemory();
            freeMemory = runtime.freeMemory();
            totalMemory = runtime.totalMemory();
            endTime = System.currentTimeMillis();
            totalTime += endTime - startTime;
            junk = junkArray[i].substring(1);
        }

        logger.info("Time for " + MAX_ITERATIONS + " memory checks : " + totalTime + "ms");
        logger.info("maxMemory   = " + maxMemory + " bytes " + maxMemory / (1024*1024) + "MB");
        logger.info("totalMemory = " + totalMemory + " bytes " + totalMemory / (1024*1024) + "MB");
        logger.info("freeMemory  = " + freeMemory + " bytes " + freeMemory / (1024*1024) + "MB");

    }
}
