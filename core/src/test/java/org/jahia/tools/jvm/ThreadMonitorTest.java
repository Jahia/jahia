package org.jahia.tools.jvm;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.errors.ErrorFileDumper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit test for ThreadMonitor class
 */
public class ThreadMonitorTest {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ThreadMonitorTest.class);

    private final long LOOP_COUNT = 1000L;
    private final long THREAD_COUNT = 200;

    private static File todaysDirectory;
    private Set<Thread> threadSet = new HashSet<Thread>();

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        Date now = new Date();
        todaysDirectory = new File(new File(System.getProperty("java.io.tmpdir"),
                "jahia-threads"), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        FileUtils.deleteDirectory(todaysDirectory);
    }

    @Test
    public void testDumpThreadInfo() throws InterruptedException {
        StopWatch stopWatch = new StopWatch("testDumpThreadInfo");
        stopWatch.start(Thread.currentThread().getName() + " dumping thread info");

        threadSet.clear();
        ThreadMonitor.getInstance().setLoggingConcurrentCalls(true);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(100);

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    ThreadMonitor.getInstance().dumpThreadInfo(false, true);
                }
            }, "DumpThreadInfoThread" + i);
            threadSet.add(newThread);
            Thread.sleep(50);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ThreadMonitor.shutdownInstance();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per dump = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Thread dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Thread dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

    }

    @Test
    public void testDumpThreadInfoWithInterval() throws InterruptedException {

        StopWatch stopWatch = new StopWatch("testDumpThreadInfoWithInterval");
        stopWatch.start(Thread.currentThread().getName() + " dumping thread info with interval");

        ThreadMonitor.getInstance().setLoggingConcurrentCalls(true);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(100);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    ThreadMonitor.getInstance().dumpThreadInfoWithInterval(false, true, 5, 1);
                }
            }, "DumpThreadInfoThread" + i);
            threadSet.add(newThread);
            Thread.sleep(10);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        Thread.sleep(10000);

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ThreadMonitor.shutdownInstance();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per dump = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Thread dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Thread dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

    }

    @Test
    public void testFindDeadLock() throws InterruptedException {
        StopWatch stopWatch = new StopWatch("testFindDeadLock");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ThreadMonitor.getInstance().setLoggingConcurrentCalls(true);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(100);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    ThreadMonitor.getInstance().findDeadlock();
                }
            }, "DumpThreadInfoThread" + i);
            threadSet.add(newThread);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ThreadMonitor.shutdownInstance();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per dump = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Thread dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Thread dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

    }

    @Test
    public void testGenerateThreadInfo() throws InterruptedException {
        StopWatch stopWatch = new StopWatch("testGenerateThreadInfo");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ThreadMonitor.getInstance().setLoggingConcurrentCalls(true);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(100);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    StringWriter stringWriter = new StringWriter();
                    ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
                    stringWriter = null;
                }
            }, "DumpThreadInfoThread" + i);
            threadSet.add(newThread);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        for (Thread curThread : threadSet) {
            curThread.join();
        }

        ThreadMonitor.shutdownInstance();

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per dump = " + averageTime);
        logger.info(stopWatch.prettyPrint());

        Assert.assertTrue("Thread dump directory does not exist !", todaysDirectory.exists());
        Assert.assertTrue("Thread dump directory should have error files in it !", todaysDirectory.listFiles().length > 0);

    }

}
