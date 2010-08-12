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

    public static File dumpToFile(Throwable t, HttpServletRequest request) throws IOException {

        if (lastFileDumpedException != null && t != null && t.toString().equals(lastFileDumpedException.toString())) {
            lastFileDumpedExceptionOccurences++;
            if (lastFileDumpedExceptionOccurences <
                    SettingsBean.getInstance().getFileDumpMaxRegroupingOfPreviousException()) {
                return null;
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
                generateErrorReport(request, t, lastFileDumpedExceptionOccurences, lastFileDumpedException);
        FileWriter fileWriter = new FileWriter(errorFile);
        fileWriter.write(errorWriter.toString());
        fileWriter.close();
        lastFileDumpedException = t;
        lastFileDumpedExceptionOccurences = 1;
        return errorFile;

    }

    public static StringWriter generateErrorReport(HttpServletRequest request, Throwable t, int lastExceptionOccurences,
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
        if (request != null) {
            strOut.println("URL: " + request.getRequestURL());
            if (request.getQueryString() != null) {
                strOut.println("?" + request.getQueryString());
            }
            strOut.println("   Method: " + request.getMethod());
            strOut.println("");
            strOut.println(
                    "Remote host: " + request.getRemoteHost() + "     Remote Address: " + request.getRemoteAddr());
            strOut.println("");
            strOut.println("Request headers:");
            strOut.println("-----------------");
            Iterator headerNames = new EnumerationIterator(request.getHeaderNames());
            while (headerNames.hasNext()) {
                String headerName = (String) headerNames.next();
                String headerValue = request.getHeader(headerName);
                strOut.println("   " + headerName + " : " + headerValue);
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
                        long cacheLimit = curCache.getCacheLimit();
                        String efficiencyStr = "0";
                        if (!Double.isNaN(curCache.getCacheEfficiency())) {
                            efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                        }
                        strOut.println("name=" + curCacheName + " size=" + curCache.size() + " limit=" +
                                cacheLimit / (1024 * 1024) + "MB" + " successful hits=" + curCache.getSuccessHits() +
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
            threadMonitor.findDeadlock(strOut);
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
