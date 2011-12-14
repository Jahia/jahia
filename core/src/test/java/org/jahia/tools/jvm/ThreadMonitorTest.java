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

    private boolean enabledDebugLogging = false;
    private long minimalIntervalBetweenDumps = 10;

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
    public void testMonitorActivation() throws InterruptedException {
        logger.info("Starting testMonitorActivation test...");

        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);

        File[] files = todaysDirectory.listFiles();
        int fileCountBeforeActiveMonitor = (files == null ? 0 : files.length);

        ThreadMonitor.getInstance().setActivated(true);
        ThreadMonitor.getInstance().dumpThreadInfo(false, true);
        Thread.sleep(minimalIntervalBetweenDumps*2);
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval(false, true, 2, 1);
        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }
        String deadLocks = ThreadMonitor.getInstance().findDeadlock();
        Thread.sleep(minimalIntervalBetweenDumps*2);
        StringWriter stringWriter = new StringWriter();
        ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
        Thread.sleep(minimalIntervalBetweenDumps*2);

        files = todaysDirectory.listFiles();
        int fileCountAfterActiveMonitor = (files == null ? 0 : files.length);
        Assert.assertEquals("File difference is not as expected with active monitor", 2, fileCountAfterActiveMonitor - fileCountBeforeActiveMonitor);
        Assert.assertNotSame("Value for dead lock is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, deadLocks);
        Assert.assertNotSame("Value for generated thread info is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, stringWriter.toString());

        ThreadMonitor.getInstance().setActivated(false);
        Thread.sleep(minimalIntervalBetweenDumps*2);
        ThreadMonitor.getInstance().dumpThreadInfo(true, true);
        Thread.sleep(minimalIntervalBetweenDumps*2);
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval(true, true, 2, 1);
        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }
        deadLocks = ThreadMonitor.getInstance().findDeadlock();
        Thread.sleep(minimalIntervalBetweenDumps*2);
        stringWriter = new StringWriter();
        ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
        Thread.sleep(minimalIntervalBetweenDumps*2);

        files = todaysDirectory.listFiles();
        int fileCountAfterInactiveMonitor = (files == null ? 0 : files.length);
        Assert.assertEquals("File difference is not as expected with inactive monitor", 0, fileCountAfterInactiveMonitor - fileCountAfterActiveMonitor);
        Assert.assertEquals("Value for dead lock is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, deadLocks);
        Assert.assertEquals("Value for generated thread info is not as expected", ThreadMonitor.THREAD_MONITOR_DEACTIVATED, stringWriter.toString());

    }

    @Test
    public void testDumpThreadInfo() throws InterruptedException {
        logger.info("Starting testDumpThreadInfo test...");

        StopWatch stopWatch = new StopWatch("testDumpThreadInfo");
        stopWatch.start(Thread.currentThread().getName() + " dumping thread info");

        threadSet.clear();
        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);

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
        logger.info("Starting testDumpThreadInfoWithInterval test...");

        StopWatch stopWatch = new StopWatch("testDumpThreadInfoWithInterval");
        stopWatch.start(Thread.currentThread().getName() + " dumping thread info with interval");

        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    ThreadMonitor.getInstance().dumpThreadInfoWithInterval(false, true, 5, 1);
                }
            }, "DumpThreadInfoWithIntervalThread" + i);
            threadSet.add(newThread);
            Thread.sleep(20);
            newThread.start();
        }

        logger.info("Waiting for dumps to be processed...");

        while (ThreadMonitor.getInstance().isDumping()) {
            Thread.sleep(100);
        }

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
        logger.info("Starting testFindDeadLock test...");

        StopWatch stopWatch = new StopWatch("testFindDeadLock");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    ThreadMonitor.getInstance().findDeadlock();
                }
            }, "FindDeadLockThread" + i);
            threadSet.add(newThread);
            Thread.sleep(10);
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
        logger.info("Starting testGenerateThreadInfo test...");

        StopWatch stopWatch = new StopWatch("testGenerateThreadInfo");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");

        ThreadMonitor.getInstance().setDebugLogging(enabledDebugLogging);
        ThreadMonitor.getInstance().setMinimalIntervalBetweenDumps(minimalIntervalBetweenDumps);
        threadSet.clear();

        for (int i=0; i < THREAD_COUNT; i++) {
            Thread newThread = new Thread(new Runnable() {

                public void run() {
                    StringWriter stringWriter = new StringWriter();
                    ThreadMonitor.getInstance().generateThreadInfo(stringWriter);
                    stringWriter = null;
                }
            }, "GenerateThreadInfoThread" + i);
            threadSet.add(newThread);
            Thread.sleep(10);
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
