/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.bin.errors;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.statistics.StatisticsGateway;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.ehcache.EhCacheImpl;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.RequestLoadAverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * System information utility.
 * User: loom
 * Date: Jul 16, 2010
 * Time: 4:56:24 PM
 * IMPORTANT NOTE : As this code gets called by log4j appenders, please do not use Log4J loggers in this code !
 */
public class ErrorFileDumper {

    public static final FastDateFormat DATE_FORMAT_DIRECTORY = FastDateFormat.getInstance("yyyy_MM_dd");
    public static final FastDateFormat DATE_FORMAT_FILE = FastDateFormat.getInstance("yyyy_MM_dd-HH_mm_ss_SSS");

    private static final long MIN_INTERVAL_BETWEEN_QUEUE_WARNING = 1000L;
    private static final long MIN_INTERVAL_BETWEEN_HIGHLOAD_WARNING = 1000L;
    private static final Logger logger = LoggerFactory.getLogger(ErrorFileDumper.class);

    private static Throwable previousException = null;
    private static int previousExceptionOccurrences = 0;
    private static long totalExceptionCount = 0L;

    private static volatile ExecutorService executorService;
    private static volatile int maximumTasksAllowed = 100;
    private static volatile double highLoadBoundary = 10.0;
    private static volatile int tasksSubmitted = 0;
    private static volatile long lastCallToDump = 0;
    private static volatile long lastHighLoadMessageTime = 0;

    /**
     * A low priority thread factory
     */
    private static class LowPriorityThreadFactory implements ThreadFactory {

        public Thread newThread(Runnable runnable) {
            Thread lowPriorityThread = new Thread(runnable);
            lowPriorityThread.setName("LowPriorityThread");
            lowPriorityThread.setPriority(Thread.MIN_PRIORITY);
            lowPriorityThread.setName("ErrorFileDumperThread");
            return lowPriorityThread;
        }
    }

    /**
     * Utility class to copy useful information from the HTTP request object, as we won't have access to it in
     * asynchronous threads.
     */
    public static class HttpRequestData {

        private String requestURL;
        private String queryString;
        private String method;
        private Map<String, String> headers = new HashMap<String, String>();
        private String remoteHost;
        private String remoteAddr;

        public HttpRequestData(HttpServletRequest request) {

            // When dealing with a forwarded request, we are still interested in the original URL and query string
            // which are stored as a dedicated request attributes
            String originalRequestUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
            if (originalRequestUri == null) {
                this.requestURL = request.getRequestURI();
            } else {
                this.requestURL = originalRequestUri;
            }
            String originalQueryString = (String) request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
            if (originalQueryString == null) {
                this.queryString = request.getQueryString();
            } else {
                this.queryString = originalQueryString;
            }

            this.method = request.getMethod();
            this.remoteHost = request.getRemoteHost();
            this.remoteAddr = request.getRemoteAddr();
            Iterator<?> headerNames = new EnumerationIterator(request.getHeaderNames());
            while (headerNames.hasNext()) {
                String headerName = (String) headerNames.next();
                String headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);
            }
        }

        public String getRequestURL() {
            return requestURL;
        }

        public String getQueryString() {
            return queryString;
        }

        public String getMethod() {
            return method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }
    }

    private static class FileDumperRunnable implements Runnable {

        private Throwable t;
        private HttpRequestData requestData;

        public FileDumperRunnable(Throwable t, HttpRequestData requestData) {
            this.t = t;
            this.requestData = requestData;
        }

        public void run() {
            try {
                performDumpToFile(t, requestData);
                tasksSubmitted--;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void start() {
        if (isShutdown()) {
            executorService = Executors.newSingleThreadExecutor(new LowPriorityThreadFactory());
            System.out.println("Started error file dumper executor service");
        }
    }

    public static void shutdown() {
        shutdown(100L);
    }

    public static void shutdown(long millisecondsToWait) {
        if (!isShutdown()) {
            System.out.println("Shutting down error file dumper executor service...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(millisecondsToWait, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            executorService = null;
            System.out.println("...done shutting down error file dumper executor service.");
        }
    }

    public static boolean isShutdown() {
        return (executorService == null);
    }

    public static void setFileDumpActivated(boolean fileDumpActivated) {
        if (fileDumpActivated && isShutdown()) {
            start();
        } else if (!fileDumpActivated && isFileDumpActivated()) {
            shutdown();
        }
    }

    public static boolean isFileDumpActivated() {
        return !isShutdown();
    }

    public static void dumpToFile(Throwable t, HttpServletRequest request) throws IOException {

        if (isShutdown()) {
            return;
        }

        if (isHighLoad()) {
            return;
        }

        int maxTasks = maximumTasksAllowed;
        if (tasksSubmitted < maxTasks) {
            HttpRequestData requestData = null;
            if (request != null) {
                requestData = new HttpRequestData(request);
            }
            executorService.submit(new FileDumperRunnable(t, requestData));
            tasksSubmitted++;
            lastCallToDump = System.currentTimeMillis();
        } else {
            long now = System.currentTimeMillis();
            if ((now - lastCallToDump) > MIN_INTERVAL_BETWEEN_QUEUE_WARNING) {
                System.out.println(maxTasks + " error dumps already submitted, not allowing any more.");
                lastCallToDump = now;
            }
        }
    }

    public static int getMaximumTasksAllowed() {
        return maximumTasksAllowed;
    }

    /**
     * Sets the maximum number of parallel dumping tasks allowed to be queued. Default value is 100.
     */
    public static void setMaximumTasksAllowed(int maximumTasksAllowed) {
        ErrorFileDumper.maximumTasksAllowed = maximumTasksAllowed;
    }

    private static void performDumpToFile(Throwable exception, HttpRequestData httpRequestData) throws IOException {

        long dumpStartTime = System.currentTimeMillis();

        Throwable previousExceptionToDump;
        int previousExceptionOccurencesToDump;
        long momentaryTotalExceptionCount;

        synchronized (ErrorFileDumper.class) {

            if (previousException != null
                    && exception != null
                    && exception.getClass().equals(previousException.getClass()) // For performance reasons, to avoid unnecessary toString() invocations.
                    && exception.toString().equals(previousException.toString())) {

                previousExceptionOccurrences++;
                SettingsBean settings = SettingsBean.getInstance();
                if (settings != null && previousExceptionOccurrences < settings.getFileDumpMaxRegroupingOfPreviousException()) {
                    return;
                }
            }

            previousExceptionToDump = previousException;
            previousExceptionOccurencesToDump = previousExceptionOccurrences;
            previousException = exception;
            previousExceptionOccurrences = 1;

            totalExceptionCount++;
            momentaryTotalExceptionCount = totalExceptionCount;
        }

        StringWriter errorWriter = generateErrorReport(httpRequestData, exception, previousExceptionOccurencesToDump, previousExceptionToDump);
        File errorFile = getNextErrorFile(momentaryTotalExceptionCount);
        FileUtils.writeStringToFile(errorFile, errorWriter.toString(), "UTF-8");

        long dumpTotalTime = System.currentTimeMillis() - dumpStartTime;
        System.err.println("Error dumped to file " + errorFile.getAbsolutePath() + " in " + dumpTotalTime + "ms");
    }

    private static File getNextErrorFile(long exceptionCount) {
        Date now = new Date();
        File todaysDirectory = new File(SettingsBean.getErrorDir(), DATE_FORMAT_DIRECTORY.format(now));
        todaysDirectory.mkdirs();
        return new File(todaysDirectory, "error-" + DATE_FORMAT_FILE.format(now) + "-" + Long.toString(exceptionCount) + ".txt");
    }

    private static File getNextHeapDumpFile() {
        Date now = new Date();
        File todaysDirectory = new File(SettingsBean.getHeapDir(), ErrorFileDumper.DATE_FORMAT_DIRECTORY.format(now));
        todaysDirectory.mkdirs();
        return new File(todaysDirectory, "heap-" + ErrorFileDumper.DATE_FORMAT_FILE.format(now) + ".hprof");
    }

    public static StringWriter generateErrorReport(HttpRequestData requestData, Throwable exception, int previousExceptionOccurences, Throwable previousException) {

        StringWriter msgBodyWriter = new StringWriter();
        if (isHighLoad()) {
            return msgBodyWriter;
        }
        PrintWriter strOut = new PrintWriter(msgBodyWriter);
        if (previousExceptionOccurences > 1) {
            strOut.println("");
            strOut.println("The previous error: " + previousException.getMessage() + " occured " +
                    Integer.toString(previousExceptionOccurences) + " times.");
            strOut.println("");
        }
        strOut.println("");
        strOut.println("Your Server has generated an error. Please review the details below for additional information: ");
        strOut.println("");
        if (exception instanceof JahiaException) {
            JahiaException nje = (JahiaException) exception;
            String severityMsg = "Undefined";
            switch (nje.getSeverity()) {
                case JahiaException.WARNING_SEVERITY:
                    severityMsg = "WARNING";
                    break;
                case JahiaException.ERROR_SEVERITY:
                    severityMsg = "ERROR";
                    break;
                case JahiaException.CRITICAL_SEVERITY:
                    severityMsg = "CRITICAL";
                    break;
                case JahiaException.FATAL_SEVERITY:
                    severityMsg = "FATAL";
                    break;
            }
            strOut.println("Severity: " + severityMsg);
        }
        strOut.println("");
        if (exception != null) {
            strOut.println("Error: " + exception.getMessage());
        }
        strOut.println("");
        if (requestData != null) {
            strOut.println("URL: " + requestData.getRequestURL());
            if (requestData.getQueryString() != null) {
                strOut.println("?" + requestData.getQueryString());
            }
            strOut.println("");
            strOut.println("Method: " + requestData.getMethod());
            strOut.println("");
            strOut.println(
                    "Remote host: " + requestData.getRemoteHost() + "     Remote Address: " + requestData.getRemoteAddr());
            strOut.println("");
            strOut.println("Request headers:");
            strOut.println("-----------------");
            Iterator<Map.Entry<String, String>> headerEntryIterator = requestData.getHeaders().entrySet().iterator();
            while (headerEntryIterator.hasNext()) {
                Map.Entry<String, String> headerEntry = headerEntryIterator.next();
                strOut.println("   " + headerEntry.getKey() + " : " + headerEntry.getValue());
            }
        }

        strOut.println("");
        strOut.println("Stack trace:");
        strOut.println("-------------");
        String stackTraceStr = stackTraceToString(exception);
        strOut.println(stackTraceStr);

        outputSystemInfo(strOut);

        strOut.println("");
        strOut.println("Depending on the severity of this error, server may still be operational or not. Please check your");
        strOut.println("installation as soon as possible.");
        strOut.println("");
        strOut.println("Yours Faithfully, ");
        strOut.println("    Server Notification Service");
        return msgBodyWriter;
    }

    public static void outputSystemInfo(PrintWriter strOut) {
        outputSystemInfoConsiderLoad(strOut);
    }

    public static double getHighLoadBoundary() {
        return highLoadBoundary;
    }

    /**
     * Sets the boundary load value above which all reporting will automatically deactivate. The default value is 10.0
     *
     * @param highLoadBoundary
     */
    public static void setHighLoadBoundary(double highLoadBoundary) {
        ErrorFileDumper.highLoadBoundary = highLoadBoundary;
    }

    private static void outputSystemInfoConsiderLoad(PrintWriter strOut) {
        boolean highLoad = isHighLoad();
        outputSystemInfo(strOut, !highLoad, !highLoad, !highLoad, !highLoad, !highLoad, !highLoad, !highLoad, true);
    }

    private static boolean isHighLoad() {
        boolean highLoad = false;
        RequestLoadAverage loadAverage = RequestLoadAverage.getInstance();
        highLoad = loadAverage != null && loadAverage.getOneMinuteLoad() > highLoadBoundary;
        if (highLoad) {
            long now = System.currentTimeMillis();
            if ((now - lastHighLoadMessageTime) > MIN_INTERVAL_BETWEEN_HIGHLOAD_WARNING) {
                System.out.println("High load (" + loadAverage.getOneMinuteLoad() + ") detected, will deactivate reporting...");
            }
            lastHighLoadMessageTime = System.currentTimeMillis();
        }
        return highLoad;
    }

    public static void outputSystemInfo(PrintWriter strOut, boolean systemProperties, boolean jahiaSettings, boolean memory, boolean caches, boolean threads, boolean deadlocks, boolean loadAverage) {
        outputSystemInfo(strOut, systemProperties, false, jahiaSettings, memory, caches, threads, deadlocks, loadAverage);
    }

    public static void outputSystemInfo(PrintWriter strOut, boolean systemProperties, boolean environmentVariables, boolean jahiaSettings, boolean memory, boolean caches, boolean threads, boolean deadlocks, boolean loadAverage) {

        if (systemProperties) {
            // now let's output the system properties.
            strOut.println();
            strOut.println("System properties:");
            strOut.println("-------------------");
            Map<Object, Object> orderedProperties = new TreeMap<Object, Object>(System.getProperties());
            Iterator<Map.Entry<Object, Object>> entrySetIter = orderedProperties.entrySet().iterator();
            while (entrySetIter.hasNext()) {
                Map.Entry<Object, Object> curEntry = entrySetIter.next();
                String curPropertyName = (String) curEntry.getKey();
                String curPropertyValue = (String) curEntry.getValue();
                strOut.println("   " + curPropertyName + " : " + curPropertyValue);
            }
        }

        if (environmentVariables) {
            // now let's output the environment variables.
            strOut.println();
            strOut.println("Environment variables:");
            strOut.println("-------------------");
            Map<String, String> orderedProperties = new TreeMap<String, String>(System.getenv());
            Iterator<Map.Entry<String, String>> entrySetIter = orderedProperties.entrySet().iterator();
            while (entrySetIter.hasNext()) {
                Map.Entry<String, String> curEntry = entrySetIter.next();
                String curPropertyName = curEntry.getKey();
                String curPropertyValue = curEntry.getValue();
                strOut.println("   " + curPropertyName + " : " + curPropertyValue);
            }
        }

        if (jahiaSettings) {
            strOut.println();

            if (SettingsBean.getInstance() != null) {
                strOut.append("Server configuration (").append(Jahia.getFullProductVersion()).append(" - ").append(Jahia.getBuildDate()).append("):");
                strOut.println();
                strOut.println("---------------------");
                SettingsBean settings = SettingsBean.getInstance();
                Map<Object, Object> jahiaOrderedProperties = new TreeMap<Object, Object>(settings.getPropertiesFile());
                Iterator<Map.Entry<Object, Object>> jahiaEntrySetIter = jahiaOrderedProperties.entrySet().iterator();
                while (jahiaEntrySetIter.hasNext()) {
                    Map.Entry<Object, Object> curEntry = jahiaEntrySetIter.next();
                    String curPropertyName = (String) curEntry.getKey();
                    String curPropertyValue = null;
                    if (curEntry.getValue() == null) {
                        curPropertyValue = null;
                    } else if (curEntry.getValue() instanceof String) {
                        curPropertyValue = (String) curEntry.getValue();
                    } else {
                        curPropertyValue = curEntry.getValue().toString();
                    }
                    if (curPropertyName.toLowerCase().indexOf("password") == -1
                            && (!"mail_server".equals(curPropertyName)
                            || !StringUtils.contains(curPropertyValue, "&password=") && !StringUtils
                            .contains(curPropertyValue, "?password="))) {
                        strOut.println("   " + curPropertyName + " = " + curPropertyValue);
                    }
                }
                if (!settings.getStartupOptions().getOptions().isEmpty()) {
                    strOut.println();
                    strOut.append("Server startup options:");
                    strOut.println();
                    strOut.println("---------------------");
                    strOut.println(StringUtils.join(settings.getStartupOptions().getOptions(), ", "));
                }
            }
        }

        if (memory) {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
            printMemoryUsage(MemoryType.HEAP.toString(), memoryUsage, strOut);
            memoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            printMemoryUsage(MemoryType.NON_HEAP.toString(), memoryUsage, strOut);

            strOut.println("--------------");
            strOut.println("Memory pool details");
            strOut.println("--------------");
            List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean bean : memoryPoolMXBeans) {
                printMemoryUsage("Memory Pool \"" + bean.getName() + "\" (" + bean.getType().toString() + ")",
                        bean.getUsage(), strOut);
            }
        }

        if (caches) {
            strOut.println();
            DecimalFormat percentFormat = new DecimalFormat("###.##");
            if (SpringContextSingleton.getInstance().isInitialized() && (ServicesRegistry.getInstance().getCacheService() != null)) {
                strOut.println("Cache status:");
                strOut.println("--------------");

                // non Ehcaches
                SortedSet<String> sortedCacheNames = new TreeSet<>(ServicesRegistry.getInstance().getCacheService().getNames());
                for (String sortedCacheName : sortedCacheNames) {
                    final Cache<Object, Object> objectCache = ServicesRegistry.getInstance().getCacheService().getCache(sortedCacheName);
                    if (objectCache != null && !(((Cache<?, ?>) objectCache).getCacheImplementation() instanceof EhCacheImpl)) {
                        String efficiencyStr = "0";
                        if (!Double.isNaN(objectCache.getCacheEfficiency())) {
                            efficiencyStr = percentFormat.format(objectCache.getCacheEfficiency());
                        }
                        strOut.println(sortedCacheName + ": size=" + objectCache.size() + ", successful hits=" + objectCache.getSuccessHits() +
                                ", total hits=" + objectCache.getTotalHits() + ", efficiency=" + efficiencyStr + "%");
                    }
                }
            }

            // Ehcaches
            List<CacheManager> cacheManagers = CacheManager.ALL_CACHE_MANAGERS;
            for (CacheManager ehcacheManager : cacheManagers) {
                String[] ehcacheNames = ehcacheManager.getCacheNames();
                java.util.Arrays.sort(ehcacheNames);
                for (String ehcacheName : ehcacheNames) {
                    Ehcache ehcache = ehcacheManager.getEhcache(ehcacheName);
                    strOut.append(ehcacheName).append(": ");
                    if (ehcache != null) {
                        StatisticsGateway ehcacheStats = ehcache.getStatistics();
                        String efficiencyStr = "0";
                        if (ehcacheStats.cacheHitCount() + ehcacheStats.cacheMissCount() > 0) {
                            efficiencyStr = percentFormat.format(ehcacheStats.cacheHitCount() * 100f / (ehcacheStats.cacheHitCount() + ehcacheStats.cacheMissCount()));
                        }
                        strOut.append("size=" + ehcacheStats.getSize()
                                + ", successful hits=" + ehcacheStats.cacheHitCount()
                                + ", total hits="
                                + (ehcacheStats.cacheHitCount() + ehcacheStats.cacheMissCount())
                                + ", efficiency=" + efficiencyStr + "%");
                        strOut.println();
                    }
                }
            }
        }

        ThreadMonitor threadMonitor = null;
        if (threads) {
            strOut.println();
            strOut.println("Thread status:");
            strOut.println("--------------");
            threadMonitor = ThreadMonitor.getInstance();
            threadMonitor.generateThreadInfo(strOut);
        }

        if (deadlocks) {
            strOut.println();
            strOut.println("Deadlock status:");
            threadMonitor = threadMonitor != null ? threadMonitor : ThreadMonitor.getInstance();
            ;
            String deadlock = threadMonitor.findDeadlock();
            strOut.println(deadlock != null ? deadlock : "none");
        }

        if (loadAverage) {
            strOut.println();
            strOut.println("Request load average:");
            strOut.println("---------------------");
            RequestLoadAverage info = RequestLoadAverage.getInstance();
            if (info != null) {
                strOut.println("Over one minute=" + info.getOneMinuteLoad() + " Over five minute="
                        + info.getFiveMinuteLoad() + " Over fifteen minute="
                        + info.getFifteenMinuteLoad());
            } else {
                strOut.println("not available");
            }
            strOut.println();
        }

        strOut.flush();
    }

    private static void printMemoryUsage(String type, MemoryUsage usage, PrintWriter strOut) {
        strOut.println();
        strOut.print(type);
        strOut.print(" : ");
        strOut.print(Math.round((float) usage.getUsed() / (float) usage.getMax() * 100f));
        strOut.println("% used");
        strOut.println("---------------");
        strOut.print("Used      : ");
        strOut.println(org.jahia.utils.FileUtils.humanReadableByteCount(usage.getUsed(), true));
        strOut.print("Committed : ");
        strOut.println(org.jahia.utils.FileUtils.humanReadableByteCount(usage.getCommitted(), true));
        strOut.print("Max       : ");
        strOut.println(org.jahia.utils.FileUtils.humanReadableByteCount(usage.getMax(), true));
    }

    /**
     * Converts an exception stack trace to a string, going doing into all
     * the embedded exceptions too to detail as much as possible the real
     * causes of the error.
     *
     * @param t the exception (eventually that contains other exceptions) for
     *          which we want to convert the stack trace into a string.
     * @return a string containing all the stack traces of all the exceptions
     *         contained inside this exception, or an empty string if passed an
     *         empty string.
     */
    protected static String stackTraceToString(Throwable t) {
        int nestingDepth = getNestedExceptionDepth(t, 0);
        return recursiveStackTraceToString(t, nestingDepth);
    }

    protected static String recursiveStackTraceToString(Throwable t, int curDepth) {
        if (t == null) {
            return "";
        }
        StringWriter msgBodyWriter = new StringWriter();
        PrintWriter strOut = new PrintWriter(msgBodyWriter);
        Throwable innerThrowable = t.getCause();
        if (innerThrowable != null) {
            String innerExceptionTrace = recursiveStackTraceToString(innerThrowable, curDepth - 1);
            msgBodyWriter.write(innerExceptionTrace);
        }
        if (curDepth == 0) {
            strOut.println("Cause level : " + curDepth + " (level 0 is the most precise exception)");

        } else {
            strOut.println("Cause level : " + curDepth);
        }
        t.printStackTrace(strOut);
        return msgBodyWriter.toString();
    }

    protected static int getNestedExceptionDepth(Throwable t, int curDepth) {
        if (t == null) {
            return curDepth;
        }
        int newDepth = curDepth;
        Throwable innerThrowable = t.getCause();
        if (innerThrowable != null) {
            newDepth = getNestedExceptionDepth(innerThrowable, curDepth + 1);
        }
        return newDepth;
    }

    public static boolean isHeapDumpSupported() throws MalformedObjectNameException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName hotSpotDiagnostic = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        return mBeanServer.isRegistered(hotSpotDiagnostic);
    }

    public static File performHeapDump() throws MalformedObjectNameException, InstanceNotFoundException,
            ReflectionException, MBeanException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName hotSpotDiagnostic = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        if (!mBeanServer.isRegistered(hotSpotDiagnostic)) {
            throw new UnsupportedOperationException("Unable to find the "
                    + "'com.sun.management:type=HotSpotDiagnostic'" + " managed bean to perform heap dump");
        }
        File hprofFilePath = getNextHeapDumpFile();
        mBeanServer.invoke(hotSpotDiagnostic, "dumpHeap", new Object[] { hprofFilePath.getPath(), Boolean.TRUE },
                new String[] { String.class.getName(), boolean.class.getName() });
        return hprofFilePath;
    }
}
