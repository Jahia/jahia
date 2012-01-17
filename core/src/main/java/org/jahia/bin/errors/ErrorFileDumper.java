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

package org.jahia.bin.errors;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.RequestLoadAverage;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    private static Throwable lastFileDumpedException = null;
    private static int lastFileDumpedExceptionOccurences = 0;
    private static long exceptionCount = 0L;

    private static ExecutorService executorService;
    private static int maximumTasksAllowed = 100;
    private static AtomicInteger tasksSubmitted = new AtomicInteger(0);
    private static long lastCallToDump = 0;
    private static final long MIN_INTERVAL_BETWEEN_QUEUE_WARNING = 1000L;
    private static double highLoadBoundary = 10.0;
    private static long lastHighLoadMessageTime = 0;
    private static final long MIN_INTERVAL_BETWEEN_HIGHLOAD_WARNING = 1000L;

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

        String requestURL;
        String queryString;
        String method;
        Map<String, String> headers = new HashMap<String, String>();
        String remoteHost;
        String remoteAddr;

        public HttpRequestData(HttpServletRequest request) {
            this.requestURL = request.getRequestURI();
            this.queryString = request.getQueryString();
            this.method = request.getMethod();
            this.remoteHost = request.getRemoteHost();
            this.remoteAddr = request.getRemoteAddr();
            Iterator headerNames = new EnumerationIterator(request.getHeaderNames());
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
                tasksSubmitted.decrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        if (isShutdown()) {
            return;
        }
        if (executorService != null) {
            System.out.println("Shutting down error file dumper executor service...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(millisecondsToWait, TimeUnit.MILLISECONDS)) {
                    List<Runnable> droppedTasks = executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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

        if (tasksSubmitted.get() < maximumTasksAllowed) {
            HttpRequestData requestData = null;
            if (request != null) {
               requestData = new HttpRequestData(request);
            }
            Future<?> dumperFuture = executorService.submit(new FileDumperRunnable(t, requestData));
            tasksSubmitted.incrementAndGet();
            lastCallToDump = System.currentTimeMillis();
        } else {
            long now = System.currentTimeMillis();
            if ((now - lastCallToDump) > MIN_INTERVAL_BETWEEN_QUEUE_WARNING) {
                System.out.println(maximumTasksAllowed + " error dumps already submitted, not allowing any more.");
                lastCallToDump = now;
            }
        }
    }

    public static int getMaximumTasksAllowed() {
        return maximumTasksAllowed;
    }

    /**
     * Sets the maximum number of parallel dumping tasks allowed to be queued. Default value is 10.
     * @param maximumTasksAllowed
     */
    public static void setMaximumTasksAllowed(int maximumTasksAllowed) {
        ErrorFileDumper.maximumTasksAllowed = maximumTasksAllowed;
    }

    /**
     * Retrieves the number of queued tasks for generating error dumps.
     * @return
     */
    public static int getTasksSubmitted() {
        return tasksSubmitted.get();
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private static void performDumpToFile(Throwable t, HttpRequestData httpRequestData) throws IOException {
        long dumpStartTime = System.currentTimeMillis();
        if (lastFileDumpedException != null && t != null && t.toString().equals(lastFileDumpedException.toString())) {
            lastFileDumpedExceptionOccurences++;
            if ((SettingsBean.getInstance() != null) && (lastFileDumpedExceptionOccurences <
                    SettingsBean.getInstance().getFileDumpMaxRegroupingOfPreviousException())) {
                return;
            }
        }

        exceptionCount++;

        StringWriter errorWriter =
                generateErrorReport(httpRequestData, t, lastFileDumpedExceptionOccurences, lastFileDumpedException);
        File errorFile = getNextErrorFile();
        FileUtils.writeStringToFile(errorFile, errorWriter.toString(), "UTF-8");
        errorWriter = null;
        lastFileDumpedException = t;
        lastFileDumpedExceptionOccurences = 1;
        long dumpTotalTime = System.currentTimeMillis() - dumpStartTime;
        System.err.println("Error dumped to file " + errorFile.getAbsolutePath() + " in " + dumpTotalTime + "ms");
    }

    private static File getNextErrorFile() {
        Date now = new Date();
        File todaysDirectory = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-errors"), DATE_FORMAT_DIRECTORY.format(now));
        todaysDirectory.mkdirs();
        return new File(todaysDirectory,
                "error-" + DATE_FORMAT_FILE.format(now) + "-" + Long.toString(exceptionCount) + ".txt");
    }

    public static StringWriter generateErrorReport(HttpRequestData requestData, Throwable t, int lastExceptionOccurences,
                                                   Throwable lastException) {

        StringWriter msgBodyWriter = new StringWriter();
        if (isHighLoad()) {
            return msgBodyWriter;
        }
        PrintWriter strOut = new PrintWriter(msgBodyWriter);
        if (lastExceptionOccurences > 1) {
            strOut.println("");
            strOut.println("The previous error: " + lastException.getMessage() + " occured " +
                    Integer.toString(lastExceptionOccurences) + " times.");
            strOut.println("");
        }
        strOut.println("");
        strOut.println(
                "Your Server has generated an error. Please review the details below for additional information: ");
        strOut.println("");
        if (t instanceof JahiaException) {
            JahiaException nje = (JahiaException) t;
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
        if (t != null) {
            strOut.println("Error: " + t.getMessage());
        }
        strOut.println("");
        if (requestData != null) {
            strOut.println("URL: " + requestData.getRequestURL());
            if (requestData.getQueryString() != null) {
                strOut.println("?" + requestData.getQueryString());
            }
            strOut.println("   Method: " + requestData.getMethod());
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
        String stackTraceStr = stackTraceToString(t);
        strOut.println(stackTraceStr);

        outputSystemInfo(strOut);

        strOut.println("");
        strOut.println(
                "Depending on the severity of this error, server may still be operational or not. Please check your");
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
     * @param highLoadBoundary
     */
    public static void setHighLoadBoundary(double highLoadBoundary) {
        ErrorFileDumper.highLoadBoundary = highLoadBoundary;
    }

    private static void outputSystemInfoConsiderLoad(PrintWriter strOut) {
        boolean highLoad = isHighLoad();
        outputSystemInfo(strOut, !highLoad, !highLoad, !highLoad, !highLoad, !highLoad, !highLoad, true);
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
        if (systemProperties) {
            // now let's output the system properties.
            strOut.println();
            strOut.println("System properties:");
            strOut.println("-------------------");
            Map orderedProperties = new TreeMap(System.getProperties());
            Iterator entrySetIter = orderedProperties.entrySet().iterator();
            while (entrySetIter.hasNext()) {
                Map.Entry curEntry = (Map.Entry) entrySetIter.next();
                String curPropertyName = (String) curEntry.getKey();
                String curPropertyValue = (String) curEntry.getValue();
                strOut.println("   " + curPropertyName + " : " + curPropertyValue);
            }
        }
        
        if (jahiaSettings) {
            strOut.println();
    
            if (SettingsBean.getInstance() != null) {
                strOut.append("Server configuration (").append(Jahia.getFullProductVersion()).append("):");
                strOut.println();
                strOut.println("---------------------");
                SettingsBean settings = SettingsBean.getInstance();
                Map jahiaOrderedProperties = new TreeMap(settings.getPropertiesFile());
                Iterator jahiaEntrySetIter = jahiaOrderedProperties.entrySet().iterator();
                while (jahiaEntrySetIter.hasNext()) {
                    Map.Entry curEntry = (Map.Entry) jahiaEntrySetIter.next();
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
            }
        }
        
        if (memory) {
            strOut.println();
            strOut.println("Memory status:");
            strOut.println("---------------");
            strOut.println("Max memory   : " + Runtime.getRuntime().maxMemory() + " bytes");
            strOut.println("Free memory  : " + Runtime.getRuntime().freeMemory() + " bytes");
            strOut.println("Total memory : " + Runtime.getRuntime().totalMemory() + " bytes");
        }

        if (caches) {
            strOut.println();
            DecimalFormat percentFormat = new DecimalFormat("###.##");
            if (SpringContextSingleton.getInstance().isInitialized() && (ServicesRegistry.getInstance().getCacheService() != null)) {
                strOut.println("Cache status:");
                strOut.println("--------------");
    
                // non Ehcaches
                SortedSet sortedCacheNames = new TreeSet(ServicesRegistry.getInstance().getCacheService().getNames());
                Iterator cacheNameIte = sortedCacheNames.iterator();
                while (cacheNameIte.hasNext()) {
                    String curCacheName = (String) cacheNameIte.next();
                    Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache(curCacheName);
                    if (objectCache instanceof Cache && !(((Cache) objectCache).getCacheImplementation() instanceof org.jahia.services.cache.ehcache.EhCacheImpl)) {
                        Cache curCache = (Cache) objectCache;
                        String efficiencyStr = "0";
                        if (!Double.isNaN(curCache.getCacheEfficiency())) {
                            efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                        }
                        strOut.println(curCacheName + ": size=" + curCache.size() + ", successful hits=" + curCache.getSuccessHits() +
                                ", total hits=" + curCache.getTotalHits() + ", efficiency=" + efficiencyStr + "%");
                    }
                }
            }
    
            // Ehcaches
            List<CacheManager> cacheManagers = CacheManager.ALL_CACHE_MANAGERS;
            for (CacheManager ehcacheManager : cacheManagers) {
                String[] ehcacheNames = ehcacheManager.getCacheNames();
                java.util.Arrays.sort(ehcacheNames);
                for (String ehcacheName : ehcacheNames) {
                    net.sf.ehcache.Cache ehcache = ehcacheManager.getCache(ehcacheName);
                    strOut.append(ehcacheName).append(": ");
                    if (ehcache.isStatisticsEnabled()) {
                        Statistics ehcacheStats = ehcache.getStatistics();
                        String efficiencyStr = "0";
                        if (ehcacheStats.getCacheHits() + ehcacheStats.getCacheMisses() > 0) {
                            efficiencyStr = percentFormat.format(ehcacheStats.getCacheHits() * 100f / (ehcacheStats.getCacheHits() + ehcacheStats.getCacheMisses()));
                        }
                        strOut.append("size=" + ehcacheStats.getObjectCount()
                                + ", successful hits=" + ehcacheStats.getCacheHits()
                                + ", total hits="
                                + (ehcacheStats.getCacheHits() + ehcacheStats.getCacheMisses())
                                + ", efficiency=" + efficiencyStr + "%");
                    } else {
                        strOut.append("statistics disabled");
                    }
                    strOut.println();
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
            threadMonitor = threadMonitor != null ? threadMonitor : ThreadMonitor.getInstance();;
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

}
