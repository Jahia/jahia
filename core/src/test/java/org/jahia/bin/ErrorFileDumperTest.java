/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.settings.SettingsBean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test unit for error file dumper sub system.
 */
public class ErrorFileDumperTest {

    private final long LOOP_COUNT = 1000L;
    private final long THREAD_COUNT = 20;
    private Set<Thread> threadSet = new HashSet<Thread>();
    private static File todaysDirectory;

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorFileDumperTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        Date now = new Date();
        todaysDirectory = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-errors"), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.deleteDirectory(todaysDirectory);
    }

    @Test
    public void testDumperInSequence() throws InterruptedException {
        StopWatch stopWatch = new StopWatch("testDumperInSequence");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        generateExceptions();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        ErrorFileDumper.shutdown(10000L);

        Assert.assertTrue("Error dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Error dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);
    }

    @Test
    public void testDumperInParallel() throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    generateExceptions();
                }
            }, "ErrorFileDumperTestThread" + i);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ErrorFileDumper.shutdown(10000L);

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Error dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Error dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);
    }

    @Test
    public void testOutputSystemInfoAllInParallel() throws InterruptedException {

        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    // this is the call made in errors.jsp file.
                    ErrorFileDumper.outputSystemInfoAll(new PrintWriter(System.out));
                }
            }, "ErrorFileDumperTestThread" + i);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ErrorFileDumper.shutdown(10000L);

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Error dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Error dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

    }

    private void generateExceptions() {
        for (int i=0; i < LOOP_COUNT; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/cms");
            request.setQueryString("name=value");
            request.addHeader("headerName", "headerValue");
            try {
                ErrorFileDumper.dumpToFile(new Throwable("mock error " + i), (HttpServletRequest) request);
            } catch (IOException e) {
                logger.error("Error while dumping error", e);
            }
        }
    }
}
