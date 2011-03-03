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

package org.jahia.bin.errors;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jul 16, 2010
 * Time: 4:56:24 PM
 * IMPORTANT NOTE : As this code gets called by log4j appenders, please do not use Log4J loggers in this code !
 */
public class ErrorFileDumper {

    private static Throwable lastFileDumpedException = null;
    private static int lastFileDumpedExceptionOccurences = 0;
    private static long exceptionCount = 0L;

    private static Map<String, Set<Throwable>> exceptions = new HashMap<String, Set<Throwable>>();
    private static ExecutorService executorService;

    /**
     * A low priority thread factory
     */
    private static class LowPriorityThreadFactory implements ThreadFactory {

        public Thread newThread(Runnable runnable) {
            Thread lowPriorityThread = new Thread(runnable);
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
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    static {
        executorService = Executors.newSingleThreadExecutor(new LowPriorityThreadFactory());

        // add shutdown hook to properly dispose of executor service.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down error file dumper executor service...");
                if (executorService != null) {
                    executorService.shutdown();
                    try {
                        if (!executorService.awaitTermination(100L, TimeUnit.MILLISECONDS)) {
                            List<Runnable> droppedTasks = executorService.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                }
                }
            }
        });
    }

    public static void dumpToFile(Throwable t, HttpServletRequest request) throws IOException {

        HttpRequestData requestData = null;
        if (request != null) {
           requestData = new HttpRequestData(request);
        }
        Future<?> dumperFuture = executorService.submit(new FileDumperRunnable(t, requestData));
    }

    private static void performDumpToFile(Throwable t, HttpRequestData httpRequestData) throws IOException {
        long dumpStartTime = System.currentTimeMillis();
        if (lastFileDumpedException != null && t != null && t.toString().equals(lastFileDumpedException.toString())) {
            lastFileDumpedExceptionOccurences++;
            if (lastFileDumpedExceptionOccurences <
                    SettingsBean.getInstance().getFileDumpMaxRegroupingOfPreviousException()) {
                return;
            }
        }

        exceptionCount++;

        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

        File errorDir = new File(sysTempDir, "jahia-errors");
        if (!errorDir.exists()) {
            errorDir.mkdir();
        }
        Date now = new Date();
        SimpleDateFormat directoryDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        File todaysDirectory = new File(errorDir, directoryDateFormat.format(now));
        if (!todaysDirectory.exists()) {
            todaysDirectory.mkdir();
        }
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_SSS");
        File errorFile = new File(todaysDirectory,
                "error-" + fileDateFormat.format(now) + "-" + Long.toString(exceptionCount) + ".txt");
        StringWriter errorWriter =
                generateErrorReport(httpRequestData, t, lastFileDumpedExceptionOccurences, lastFileDumpedException);
        FileWriter fileWriter = new FileWriter(errorFile);
        fileWriter.write(errorWriter.toString());
        fileWriter.close();
        lastFileDumpedException = t;
        lastFileDumpedExceptionOccurences = 1;
        long dumpTotalTime = System.currentTimeMillis() - dumpStartTime;
        System.err.println("Error dumped to file " + errorFile.getAbsolutePath() + " in " + dumpTotalTime + "ms");
    }

    public static StringWriter generateErrorReport(HttpRequestData requestData, Throwable t, int lastExceptionOccurences,
                                                   Throwable lastException) {
        StringWriter msgBodyWriter = new StringWriter();
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

        outputSystemInfoAll(strOut);

        strOut.println("");
        strOut.println(
                "Depending on the severity of this error, server may still be operational or not. Please check your");
        strOut.println("installation as soon as possible.");
        strOut.println("");
        strOut.println("Yours Faithfully, ");
        strOut.println("    Server Notification Service");
        return msgBodyWriter;
    }

    public static void outputSystemInfoAll(PrintWriter strOut) {
        outputSystemInfo(strOut, true, true, true, true, true, true);
    }
    
    public static void outputSystemInfo(PrintWriter strOut, boolean systemProperties, boolean jahiaSettings, boolean memory, boolean caches, boolean threads, boolean deadlocks) {
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
                strOut.println("Server configuration:");
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
                    if (curPropertyName.toLowerCase().indexOf("password") == -1) {
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
            if (ServicesRegistry.getInstance().getCacheService() != null) {
                strOut.println("Cache status:");
                strOut.println("--------------");
    
                SortedSet sortedCacheNames = new TreeSet(ServicesRegistry.getInstance().getCacheService().getNames());
                Iterator cacheNameIte = sortedCacheNames.iterator();
                DecimalFormat percentFormat = new DecimalFormat("###.##");
                while (cacheNameIte.hasNext()) {
                    String curCacheName = (String) cacheNameIte.next();
                    Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache(curCacheName);
                    if (objectCache instanceof Cache) {
                        Cache curCache = (Cache) objectCache;
                        String efficiencyStr = "0";
                        if (!Double.isNaN(curCache.getCacheEfficiency())) {
                            efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                        }
                        strOut.println("name=" + curCacheName + " size=" + curCache.size() + " successful hits=" + curCache.getSuccessHits() +
                                " total hits=" + curCache.getTotalHits() + " efficiency=" + efficiencyStr + "%");
                    }
                }
    
            }
        }
        
        ThreadMonitor threadMonitor = null;
        if (threads) {
            strOut.println();
            strOut.println("Thread status:");
            strOut.println("--------------");
            threadMonitor = new ThreadMonitor();
            threadMonitor.generateThreadInfo(strOut);
        }
        
        if (deadlocks) {
            strOut.println();
            strOut.println("Deadlock status:");
            threadMonitor = threadMonitor != null ? threadMonitor : new ThreadMonitor();
            strOut.print(threadMonitor.findDeadlock());
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
