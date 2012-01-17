/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.utils.RequestLoadAverage;
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
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Test unit for error file dumper sub system.
 */
public class ErrorFileDumperTest {

    private final long LOOP_COUNT = 1000L;
    private final long THREAD_COUNT = 200;
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
        RequestLoadAverage.getInstance().stop();
    }

    @Test
    public void testDumperActivation() throws InterruptedException {

        logger.info("Starting testDumperInSequence test...");

        ErrorFileDumper.start();

        logger.info("Activating Error file dumping...");
        ErrorFileDumper.setFileDumpActivated(true);

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeTest = (files == null ? 0 : files.length);

        generateExceptions();

        Thread.sleep(5000);

        files = todaysDirectory.listFiles();
        int fileCountAfterTest = (files == null ? 0 : files.length);
        Assert.assertTrue("File count after test should be higher but it is not", (fileCountAfterTest > fileCountBeforeTest));

        logger.info("De-activating Error file dumping...");

        ErrorFileDumper.setFileDumpActivated(false);

        files = todaysDirectory.listFiles();
        fileCountBeforeTest = (files == null ? 0 : files.length);

        generateExceptions();

        Thread.sleep(5000);

        files = todaysDirectory.listFiles();
        fileCountAfterTest = (files == null ? 0 : files.length);

        Assert.assertEquals("File count after test should be the same as before", fileCountBeforeTest, fileCountAfterTest);

        ErrorFileDumper.shutdown(10000L);

    }

    @Test
    public void testHighLoadDeactivation() throws InterruptedException {

        logger.info("Starting testHighLoadDeactivation test...");

        RequestLoadAverage.RequestCountProvider requestCountProvider = new RequestLoadAverage.RequestCountProvider() {
            public long getRequestCount() {
                return 100;
            }
        };

        RequestLoadAverage requestLoadAverage = new RequestLoadAverage("requestLoadAverage", requestCountProvider);
        requestLoadAverage.start();
        logger.info("Waiting for load average to reach 10...");
        while (requestLoadAverage.getOneMinuteLoad() < 10.0) {
            Thread.sleep(500);
        }

        StopWatch stopWatch = new StopWatch("testHighLoadDeactivation");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        int fileCountBeforeTest = 0;
        if (todaysDirectory.exists()) {
            File[] files = todaysDirectory.listFiles();
            fileCountBeforeTest = (files == null ? 0 : files.length);
        }

        ErrorFileDumper.setHighLoadBoundary(10.0);
        ErrorFileDumper.start();

        generateExceptions();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        ErrorFileDumper.shutdown(10000L);

        RequestLoadAverage.getInstance().stop();

        int fileCountAfterTest = 0;
        if (todaysDirectory.exists()) {
            File[] files = todaysDirectory.listFiles();
            fileCountAfterTest = (files == null ? 0 : files.length);
        }

        Assert.assertEquals("File count should stay the same because high load deactivates file dumping !", fileCountBeforeTest, fileCountAfterTest);
    }

    @Test
    public void testDumperInSequence() throws InterruptedException {

        logger.info("Starting testDumperInSequence test...");

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

        logger.info("Starting testDumperInParallel test...");

        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    generateExceptions();
                }
            }, "ErrorFileDumperTestThread" + i);
            threadSet.add(newThread);
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

        logger.info("Starting testOutputSystemInfoAllInParallel test...");

        StopWatch stopWatch = new StopWatch("testDumperInParallel");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ErrorFileDumper.start();

        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    // this is the call made in errors.jsp file.
                    StringWriter stringWriter = new StringWriter();
                    ErrorFileDumper.outputSystemInfo(new PrintWriter(stringWriter));
                    stringWriter.toString();
                    stringWriter = null;
                }
            }, "ErrorFileDumperTestThread" + i);
            threadSet.add(newThread);
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
