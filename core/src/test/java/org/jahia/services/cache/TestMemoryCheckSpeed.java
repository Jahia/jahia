/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
