/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.tools.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Warning : generating thread dumps is an operation that locks the JVM and therefore should not be done while
 * high load is running on the system.
 */
public class ThreadMonitor {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ThreadMonitor.class);

    public static final String THREAD_MONITOR_DEACTIVATED = "ThreadMonitor deactivated.";

    private class ThreadDumpTask extends TimerTask {

        private int executionCount;
        private int numberOfExecutions;
        private File targetFile;

        private boolean toSystemOut;

        ThreadDumpTask(int numberOfExecutions, boolean toSystemOut, File targetFile) {
            super();
            this.numberOfExecutions = numberOfExecutions;
            this.targetFile = targetFile;
            this.toSystemOut = toSystemOut;
            out("Starting thread dump task for " + numberOfExecutions + " executions into a file " + targetFile);
        }

        @Override
        public void run() {
            executionCount++;
            if (executionCount > numberOfExecutions) {
                return;
            }
            out("Executing thread dump " + executionCount + " of " + numberOfExecutions);
            OutputStream out = null;
            long startTime = System.currentTimeMillis();
            try {
                String dump = ThreadMonitor.getInstance().getFullThreadInfo();
                if (toSystemOut) {
                    System.out.println(dump);
                }
                if (targetFile != null) {
                    out = new FileOutputStream(targetFile, true);
                    out.write(dump.getBytes("UTF-8"));
                    out.flush();
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (targetFile != null) {
                    IOUtils.closeQuietly(out);
                    long dumpTime = System.currentTimeMillis() - startTime;
                    debug("Appended thread dump to file " + targetFile.getAbsolutePath() + " in " + dumpTime + " ms");
                }
                if (executionCount >= numberOfExecutions) {
                    cancelTimer();
                    out("Stopping thread dump task after " + executionCount + " executions into a file " + targetFile);
                    ThreadMonitor.getInstance().releaseAlreadyDumping();
                }
            }
        }
    }

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private static final String DUMP_END ="\n<EndOfDump>\n\n";

    private static String INDENT = "    ";
    private static File getNextThreadDumpFile(String postfix) {
        Date now = new Date();
        File todaysDirectory = new File(SettingsBean.getThreadDir(), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
        todaysDirectory.mkdirs();
        return new File(todaysDirectory, "thread-dump-"
                + ErrorFileDumper.DATE_FORMAT_FILE.format(now) + (postfix != null ? postfix : "")
                + ".out");
    }

    private void debug(String msg) {
        if (debugLogging) {
            System.out.println(msg);
        }
    }

    private static void out(String msg) {
        System.out.println(msg);
    }

    private String dumpPrefix = "\nFull thread dump ";

    private ThreadMXBean tmbean;

    private volatile static ThreadMonitor instance;

    private Timer timer;

    private boolean debugLogging = false;

    private AtomicBoolean alreadyDumping = new AtomicBoolean(false);

    private boolean activated = true;

    private long minimalIntervalBetweenDumps = 500;

    private long lastDumpTime = -1;

    private long dumpsGenerated = 0;

    /**
     * Constructs a ThreadMonitor object to get thread information in the local JVM.
     */
    private ThreadMonitor() {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    /**
     * Constructs a ThreadMonitor object to get thread information in a remote JVM.
     */
    private ThreadMonitor(MBeanServerConnection server) {
        setMBeanServerConnection(server);
        try {
            parseMBeanInfo();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the singleton instance, creating it if it doesn't exist yet. Be sure to call the shutdownInstance()
     * method once you no longer need it.
     * @return
     */
    public static ThreadMonitor getInstance() {
        if (instance == null) {
            synchronized (ThreadMonitor.class) {
                if (instance == null) {
                    instance = new ThreadMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * Shuts down the singleton instance, calls this when undeploying your application, or call it from the
     * dependency injection system upon destruction of the application context.
     */
    public static void shutdownInstance() {
        if (instance == null) {
            return;
        }
        instance.shutdown();
        instance = null;
    }

    public boolean isDebugLogging() {
        return debugLogging;
    }

    /**
     * Activate this to output concurrent call logging to System.out
     * @param debugLogging
     */
    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    public boolean isActivated() {
        return activated;
    }

    /**
     * With this method you can activate/deactivate thread dumping methods. If deactivated, the methods will simply
     * do nothing.
     * @param activated
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public long getMinimalIntervalBetweenDumps() {
        return minimalIntervalBetweenDumps;
    }

    /**
     * Sets the minimal interval allowed between thread dumps. This makes it easy to avoid loading the CPU with
     * thread dumps under heavy load. The default value is 20ms between two thread dumps. If a thread dump is requested
     * before the interval has elapsed it will simply be ignored (there is no queuing). Some methods that return a
     * String will indicate this behavior.
     * @param minimalIntervalBetweenDumps specified in milliseconds (default value is 20ms).
     */
    public void setMinimalIntervalBetweenDumps(long minimalIntervalBetweenDumps) {
        this.minimalIntervalBetweenDumps = minimalIntervalBetweenDumps;
    }

    public boolean isDumping() {
        return alreadyDumping.get();
    }

    private void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private boolean acquireAlreadyDumping() {
        boolean dumping = alreadyDumping.get();
        if (dumping) {
            debug("Thread dump already in progress, ignoring...");
            return true;
        } else {
            // let's check the interval since that last dump.
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastDumpTime) <= minimalIntervalBetweenDumps) {
                debug("Cannot dump threads as minimal interval ("+minimalIntervalBetweenDumps+"ms) between dumps has not elapsed (=" + (currentTime - lastDumpTime) + "ms)");
                return true;
            } else {
                debug("More than minimal interval (" + minimalIntervalBetweenDumps + "ms) has elapsed (=" + (currentTime - lastDumpTime) + "ms), letting dump go through...");
                alreadyDumping.set(true);
                return false;
            }
        }
    }

    private void releaseAlreadyDumping() {
        lastDumpTime = System.currentTimeMillis();
        dumpsGenerated++;
        alreadyDumping.set(false);
        debug("Released dumping lock, alreadyDumping=" + alreadyDumping.get());
    }

    private void cancelTimer() {
        timer.cancel();
        timer = null;
    }

    /**
     * Prints the thread dump information to System.out or/and to a file.
     * @param toSysOut print the generated thread dump to a System.out
     * @param toFile print the generated thread dump to a file
     * @return the file where the thread dumps will be performed into, if the dumps are done into a file
     */
    public File dumpThreadInfo(boolean toSysOut, boolean toFile) {

        if (!activated) {
            return null;
        }

        if (!(toSysOut || toFile)) {
            return null;
        }

        if (acquireAlreadyDumping()) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        String threadInfo = getFullThreadInfo();
        if (toSysOut) {
            System.out.print(threadInfo);
        }

        File dumpFile = null;
        if (toFile) {
            dumpFile = getNextThreadDumpFile(null);
            try {
                FileUtils.writeStringToFile(dumpFile, threadInfo, "UTF-8");
                long dumpTime = System.currentTimeMillis() - startTime;
                out("Wrote thread dump to file " + dumpFile.getAbsolutePath() + " in " + dumpTime + " ms");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        releaseAlreadyDumping();

        return dumpFile;
    }

    private void dumpThreadInfo(StringBuilder dump) {
        dump.append(getDumpDate());
        dump.append(dumpPrefix);
        dump.append("\n");
        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (int i = 0; i < tinfos.length; i++) {
            ThreadInfo ti = tinfos[i];
            if (ti != null) {
                printThreadInfo(ti, dump);
            }
        }
        dump.append(DUMP_END);
    }

    private void dumpThreadInfoUsingJstack(StringBuilder dump) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("jstack " + JahiaContextLoaderListener.getPid());
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                dump.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(br);
        }
    }

    /**
     * Starts a background thread to do series of thread dumps with the specified interval.
     *
     * @param toSysOut
     *            print the generated thread dump to a System.out
     * @param toFile
     *            print the generated thread dump to a file
     * @param threadDumpCount
     *            the number of thread dumps to do
     * @param intervalSeconds
     *            the interval between thread dumps in seconds
     * @return the file where the thread dumps will be performed into, if the dumps are done into a file
     */
    public File dumpThreadInfoWithInterval(boolean toSysOut, boolean toFile, int threadDumpCount,
            int intervalSeconds) {
        if (!activated) {
            return null;
        }

        if (threadDumpCount < 1 || intervalSeconds < 1 || !(toSysOut || toFile)) {
            return null;
        }

        if (acquireAlreadyDumping()) {
            return null;
        }

        if (timer == null) {
            timer = new Timer("DumpThreadInfoWithInterval", true);
        }
        File file = toFile ? getNextThreadDumpFile("-" + threadDumpCount + "-executions") : null;
        timer.schedule(new ThreadDumpTask(threadDumpCount, toSysOut, file), 0,
                intervalSeconds * 1000L);

        return file;
        // releaseAlreadyDumping is done in ThreadDumpTask class.
    }

    /**
     * Checks if any threads are deadlocked. If any, print the thread dump information.
     */
    public String findDeadlock() {

        if (!activated) {
            return THREAD_MONITOR_DEACTIVATED;
        }

        if (acquireAlreadyDumping()) {
            return "Dead lock detection already in progress in another thread, will not report";
        }

        StringBuilder dump = new StringBuilder();
        long[] tids = tmbean.findMonitorDeadlockedThreads();
        if (tids == null) {
            releaseAlreadyDumping();
            return null;
        }
        dump.append("\n\nFound one Java-level deadlock:\n");
        dump.append("==============================\n");
        ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (int i = 1; i < infos.length; i++) {
            ThreadInfo ti = infos[i];
            // print thread information
            printThreadInfo(ti, dump);
        }

        releaseAlreadyDumping();

        return (dump.toString());
    }

    /**
     * Generates a string with the full thread dump information.
     *
     * @param writer the output writer
     */
    public void generateThreadInfo(Writer writer) {
        if (!activated) {
            try {
                writer.write(THREAD_MONITOR_DEACTIVATED);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
            }
            return;
        }

        if (acquireAlreadyDumping()) {
            try {
                writer.write("Thread info generation already in progress in another thread.");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
            }
            return;
        }
        try {
            writer.write(getFullThreadInfo());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            releaseAlreadyDumping();
        }
    }

    /**
     * create dump date similar to format used by 1.6 VMs
     *
     * @return dump date (e.g. 2007-10-25 08:00:00)
     */
    private String getDumpDate() {
        return (DATE_FORMAT.format(new Date()));
    }

    /**
     * Generates a string with the full thread dump information.
     *
     * @return the thread dump content as string
     */
    private String getFullThreadInfo() {
        StringBuilder dump = new StringBuilder(65536);

        if ((SettingsBean.getInstance() != null) && SettingsBean.getInstance().isUseJstackForThreadDumps()) {
            dumpThreadInfoUsingJstack(dump);
        } else {
            dumpThreadInfo(dump);
        }

        return dump.toString();
    }

    private void parseMBeanInfo() throws IOException {
        setDumpPrefix();
    }

    private void printThread(ThreadInfo ti, StringBuilder dump) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\""
                + " nid=" + ti.getThreadId() + " state=" + ti.getThreadState());
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        sb.append(" []\n"
                + Patterns.DOLLAR.matcher(ti.getThreadState().getClass().getName()).replaceAll(".")
                + ": " + ti.getThreadState());
        if (ti.getLockName() != null
                && ti.getThreadState() != Thread.State.BLOCKED) {
            String[] lockInfo = Patterns.AT.split(ti.getLockName());
            sb.append("\n" + INDENT + "- waiting on <0x" + lockInfo[1]
                    + "> (a " + lockInfo[0] + ")");
            sb.append("\n" + INDENT + "- locked <0x" + lockInfo[1] + "> (a "
                    + lockInfo[0] + ")");
        } else if (ti.getLockName() != null
                && ti.getThreadState() == Thread.State.BLOCKED) {
            String[] lockInfo = Patterns.AT.split(ti.getLockName());
            sb.append("\n" + INDENT + "- waiting to lock <0x" + lockInfo[1]
                    + "> (a " + lockInfo[0] + ")");
        }
        dump.append(sb.toString());
        dump.append("\n");
        if (ti.getLockOwnerName() != null) {
            dump.append(INDENT + " owned by " + ti.getLockOwnerName() + " id="
                    + ti.getLockOwnerId());
            dump.append("\n");
        }
    }

    private void printThreadInfo(ThreadInfo ti, StringBuilder dump) {
        // print thread information
        printThread(ti, dump);

        // print stack trace with locks
        StackTraceElement[] stacktrace = ti.getStackTrace();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            dump.append(INDENT + "at " + ste.toString());
            dump.append("\n");
        }
        dump.append("\n");
    }

    private void setDumpPrefix() {
        RuntimeMXBean rmbean = ManagementFactory.getRuntimeMXBean();
        dumpPrefix += rmbean.getVmName() + " (" + rmbean.getVmVersion()
                + ")\n";
    }

    /**
     * reset mbean server connection
     *
     * @param mbs
     */
    void setMBeanServerConnection(MBeanServerConnection mbs) {
        this.tmbean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    }

    public long getDumpsGenerated() {
        return dumpsGenerated;
    }

    public long getLastDumpTime() {
        return lastDumpTime;
    }
}
