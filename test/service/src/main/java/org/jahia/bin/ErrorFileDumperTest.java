/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import org.jahia.bin.errors.ErrorFileDumper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Test unit for error file dumper sub system.
 */
public class ErrorFileDumperTest {

    private final long LOOP_COUNT = 1000L;

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorFileDumperTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
    }

    @Test
    public void testDumper() throws IOException {
        StopWatch stopWatch = new StopWatch("testErrorFileDumper");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");
        for (int i=0; i < LOOP_COUNT; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/cms");
            request.setQueryString("name=value");
            request.addHeader("headerName", "headerValue");
            ErrorFileDumper.dumpToFile(new Throwable("mock error " + i), (HttpServletRequest) request);
        }
        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());
    }
}
