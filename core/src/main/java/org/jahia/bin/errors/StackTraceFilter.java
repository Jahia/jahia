/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.errors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.util.Strings;
import org.jahia.utils.Log4jEventCollector;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class that provides a printStackTrace method that supports package filtering as well as line separator
 * cleanup to avoid security log injection problems. This method should be used mostly in user facing errors. The list
 * of filtered packages can be configured through the jahia.properties file with the following properties:
 * - org.jahia.exceptions.filteredPackages : the list of filtered packages, if not defined uses the default in
 *   StackTraceFilter.DEFAULT_FILTERED_PACKAGES),
 * - org.jahia.exception.maxNbLines : the maximum number of lines an exception should output. Be careful, if set too
 *   low it will hide the Caused by exception which might be a problem. If not set it defaults to
 *   StackTraceFilter.DEFAULT_MAX_NUMBER_OF_LINES = 100
 * - org.jahia.exception.log4jAppenders : The Log4J appenders to modify with the above settings. Normally there should
 *   be no need to modify this unless you have added some new appenders you want to support this. If not set defaults to
 *   StackTraceFilter.DEFAULT_APPENDERS_TO_MODIFY = "Console,RollingJahiaLog"
 */
public class StackTraceFilter {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StackTraceFilter.class);

    private static final String STACKTRACE_LINE_START = "\n\tat";
    private static final String NESTED_STACKTRACE_LINE_START = "\n\t\tat";
    public static final String DEFAULT_FILTERED_PACKAGES = "jdk.internal.,org.apache.felix.,org.eclipse.gemini.,org.springframework.,com.oracle.,org.apache.tomcat.,org.apache.catalina.,org.tuckey.web.filters.,org.graalvm.,org.apache.logging.,org.apache.jsp.,org.apache.jasper.,org.ops4j.,javax.servlet.,org.apache.shiro.,org.glassfish.jersey";
    public static final int DEFAULT_MAX_NUMBER_OF_LINES = 100;
    public static final String DEFAULT_APPENDERS_TO_MODIFY = "Console,RollingJahiaLog";
    public static final int SXTHROWABLE_LENGTH = "%sxThrowable".length();

    private static List<String> filteredPackages;
    private static int maxNumberOfLines;
    private static List<String> log4jAppendersToModify;

    public static void init(String filteredPackages, int maxNumberOfLines, String log4jAppendersToModify) {
        logger.info("Configuring StackTraceFilter with filteredPackages: {}, maxNumberOfLines: {}, log4jAppendersToModify: {}", filteredPackages, maxNumberOfLines, log4jAppendersToModify);
        StackTraceFilter.maxNumberOfLines = Math.max(maxNumberOfLines, 0);
        StackTraceFilter.filteredPackages = splitAndStrip(filteredPackages);
        StackTraceFilter.log4jAppendersToModify = splitAndStrip(log4jAppendersToModify);
        if (!StackTraceFilter.log4jAppendersToModify.isEmpty()) {
            updateLog4JConfiguration();
        }
    }

    /**
     * Prints the stack trace of the given throwable to a string, filtering out the packages defined in the
     * throwableFilterPackages system property or the default list of packages
     * @param throwable the throwable to print
     * @return a String containing the full stack trace of the throwable, including any nested exceptions
     */
    public static String printStackTrace(Throwable throwable) {
        StringBuilder toAppendTo = new StringBuilder();
        final String suffix = Strings.EMPTY;
        final String extStackTrace = new ThrowableProxy(throwable).getExtendedStackTraceAsString(filteredPackages, PlainTextRenderer.getInstance(), suffix);
        final int len = toAppendTo.length();
        if (len > 0 && !Character.isWhitespace(toAppendTo.charAt(len - 1))) {
            toAppendTo.append(' ');
        }
        appendExtendedStackTrace(toAppendTo, extStackTrace, maxNumberOfLines == Integer.MAX_VALUE, Strings.LINE_SEPARATOR, maxNumberOfLines);
        return toAppendTo.toString();
    }


    /**
     * This method is used internally by the SafeExtendedThrowablePatternConverter to print the stack trace of the
     * given throwable to a string, to be displayed in Log4J2 logs. It does not do the filtering of packages since that
     * is done by Log4J itself. It does however do the line separator cleanup to avoid security log injection problems.
     * @param toAppendTo the StringBuilder to append the stack trace to
     * @param extStackTrace the stack trace to use as input
     * @param allLines whether to print all lines or just to the maxLines limit
     * @param separator the line separator
     * @param maxLines the maximum number of lines to include
     */
    public static void appendExtendedStackTrace(final StringBuilder toAppendTo, final String extStackTrace, boolean allLines, String separator, int maxLines) {
        if (!allLines || !Strings.LINE_SEPARATOR.equals(separator)) {
            toAppendTo.append(replaceLineSeparator(extStackTrace, separator, maxLines));
        } else {
            int firstMessageWithCRLFIndex = indexOfMessageWithCRLF(extStackTrace);
            if (firstMessageWithCRLFIndex != -1) {
                toAppendTo.append(replaceLineSeparatorInMessages(extStackTrace, firstMessageWithCRLFIndex));
            } else {
                toAppendTo.append(extStackTrace);
            }
        }
    }

    private static String replaceLineSeparator(String extStackTrace, String separator, int maxLines) {
        String[] array = extStackTrace.split(Strings.LINE_SEPARATOR);
        return StringUtils.join(array, separator, 0, Math.min(maxLines, array.length)) + separator; // we want a separator at the end
    }

    private static int indexOfMessageWithCRLF(String extStackTrace) {
        int messageStartIndex = 0;
        int stackTraceStartIndex;
        do {
            stackTraceStartIndex = getNextIndex(extStackTrace.indexOf(STACKTRACE_LINE_START, messageStartIndex),
                    extStackTrace.indexOf(NESTED_STACKTRACE_LINE_START, messageStartIndex));
            if (stackTraceStartIndex != -1) {
                stackTraceStartIndex--;
                if (StringUtils.lastIndexOf(extStackTrace, '\r', stackTraceStartIndex) >= messageStartIndex
                        || StringUtils.lastIndexOf(extStackTrace, '\n', stackTraceStartIndex) >= messageStartIndex) {
                    return messageStartIndex;
                } else if (stackTraceStartIndex != -1) {
                    messageStartIndex = getNextNestedMessageIndex(extStackTrace, stackTraceStartIndex);
                }
            }
        } while (messageStartIndex != -1 && stackTraceStartIndex != -1);
        return -1;
    }

    private static int getNextNestedMessageIndex(String extStackTrace, int stackTraceStartIndex) {
        return getNextIndex(extStackTrace.indexOf("Caused by:", stackTraceStartIndex),
                extStackTrace.indexOf("Suppressed:", stackTraceStartIndex));
    }

    private static int getNextNestedMessageIndex(StringBuilder extStackTrace, int stackTraceStartIndex) {
        return getNextIndex(extStackTrace.indexOf("Caused by:", stackTraceStartIndex),
                extStackTrace.indexOf("Suppressed:", stackTraceStartIndex));
    }

    private static int getNextIndex(int firstIndex, int secondIndex) {
        if (secondIndex == -1) {
            return firstIndex;
        } else if (firstIndex == -1) {
            return secondIndex;
        } else {
            return Math.min(firstIndex, secondIndex);
        }
    }

    private static String replaceLineSeparatorInMessages(String extStackTrace, int messageWithCRLFIndex) {
        final StringBuilder sb = new StringBuilder(extStackTrace);
        do {
            int beginningOfStackTrace = getNextIndex(sb.indexOf(STACKTRACE_LINE_START, messageWithCRLFIndex),
                    sb.indexOf(NESTED_STACKTRACE_LINE_START, messageWithCRLFIndex));
            replaceCRLF(sb, messageWithCRLFIndex, beginningOfStackTrace);
            messageWithCRLFIndex = indexOfNestedExceptionMessageWithCRLF(sb, beginningOfStackTrace);
        } while (messageWithCRLFIndex != -1);
        return sb.toString();
    }

    private static int indexOfNestedExceptionMessageWithCRLF(StringBuilder extStackTrace, int startIndex) {
        int stackTraceStartIndex;
        do {
            startIndex = getNextNestedMessageIndex(extStackTrace, startIndex);
            if (startIndex == -1) {
                return startIndex;
            }
            stackTraceStartIndex = getNextIndex(extStackTrace.indexOf(STACKTRACE_LINE_START, startIndex),
                    extStackTrace.indexOf(NESTED_STACKTRACE_LINE_START, startIndex));
            if (stackTraceStartIndex != -1) {
                stackTraceStartIndex--;
                if (StringUtils.lastIndexOf(extStackTrace, '\r', stackTraceStartIndex) >= startIndex
                        || StringUtils.lastIndexOf(extStackTrace, '\n', stackTraceStartIndex) >= startIndex) {
                    return startIndex;
                }
            }
            startIndex = stackTraceStartIndex;
        } while (startIndex != -1 && stackTraceStartIndex != -1);
        return -1;
    }

    private static StringBuilder replaceCRLF(StringBuilder toAppendTo, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            final char c = toAppendTo.charAt(i);
            switch (c) {
                case '\r':
                    toAppendTo.setCharAt(i, '\\');
                    toAppendTo.insert(i + 1, 'r');
                    break;
                case '\n':
                    toAppendTo.setCharAt(i, '\\');
                    toAppendTo.insert(i + 1, 'n');
                    break;
                default:
                    break;
            }
        }
        return toAppendTo;
    }

private static void updateLog4JConfiguration() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

    for (String appenderName : log4jAppendersToModify) {
        Appender oldAppender = loggerConfig.getAppenders().get(appenderName);
        if (oldAppender != null) {
            Layout<?> layout = oldAppender.getLayout();

            if (layout instanceof PatternLayout) {
                PatternLayout newLayout = updatePatternLayout((PatternLayout) layout, config);

                Appender newAppender = createNewAppender(oldAppender, newLayout, config);
                if (newAppender != null) {
                    newAppender.start();
                    loggerConfig.removeAppender(appenderName);
                    loggerConfig.addAppender(newAppender, null, null);
                }
            }
        }
    }

    // Update the logger context with the new configuration
    ctx.updateLoggers();
}

private static PatternLayout updatePatternLayout(PatternLayout oldLayout, Configuration config) {
    String oldPattern = oldLayout.getConversionPattern();
    String newPattern = oldPattern;
    if (oldPattern.contains("sxThrowable")) {
        int sxThrowablePos = oldPattern.indexOf("%sxThrowable{");
        if (sxThrowablePos > 0) {
            oldPattern = oldPattern.substring(0, sxThrowablePos + SXTHROWABLE_LENGTH);
        }
        newPattern = oldPattern + "{" + maxNumberOfLines + "}{filters(" + StringUtils.join(filteredPackages, ",") + ")}";
    }

    // Create a new PatternLayout with the new pattern
    return PatternLayout.newBuilder()
            .withPattern(newPattern)
            .withConfiguration(config)
            .build();
}

private static Appender createNewAppender(Appender oldAppender, Layout<?> newLayout, Configuration config) {
    if (oldAppender instanceof ConsoleAppender) {
        ConsoleAppender oldConsoleAppender = (ConsoleAppender) oldAppender;
        return ConsoleAppender.newBuilder()
                .setName(oldConsoleAppender.getName())
                .setLayout(newLayout)
                .setConfiguration(config)
                .setFilter(oldConsoleAppender.getFilter())
                .setIgnoreExceptions(oldConsoleAppender.ignoreExceptions())
                .setTarget(oldConsoleAppender.getTarget())
                .build();
    } else if (oldAppender instanceof FileAppender) {
        FileAppender oldFileAppender = (FileAppender) oldAppender;
        return FileAppender.newBuilder()
                .setName(oldFileAppender.getName())
                .setLayout(newLayout)
                .withFileName(oldFileAppender.getFileName())
                .setConfiguration(config)
                .setFilter(oldFileAppender.getFilter())
                .setIgnoreExceptions(oldFileAppender.ignoreExceptions())
                .build();
    } else if (oldAppender instanceof RollingFileAppender) {
        RollingFileAppender oldRollingFileAppender = (RollingFileAppender) oldAppender;
        return RollingFileAppender.newBuilder()
                .setName(oldRollingFileAppender.getName())
                .setLayout(newLayout)
                .withFileName(oldRollingFileAppender.getFileName())
                .withFilePattern(oldRollingFileAppender.getFilePattern())
                .setConfiguration(config)
                .setFilter(oldRollingFileAppender.getFilter())
                .setIgnoreExceptions(oldRollingFileAppender.ignoreExceptions())
                .withPolicy(oldRollingFileAppender.getTriggeringPolicy())
                .build();
    } else if (oldAppender instanceof RollingRandomAccessFileAppender) {
        RollingRandomAccessFileAppender oldRollingRandomAccessFileAppender = (RollingRandomAccessFileAppender) oldAppender;
        return RollingRandomAccessFileAppender.newBuilder()
                .setName(oldRollingRandomAccessFileAppender.getName())
                .setLayout(newLayout)
                .withFileName(oldRollingRandomAccessFileAppender.getFileName())
                .withFilePattern(oldRollingRandomAccessFileAppender.getFilePattern())
                .setConfiguration(config)
                .setFilter(oldRollingRandomAccessFileAppender.getFilter())
                .setIgnoreExceptions(oldRollingRandomAccessFileAppender.ignoreExceptions())
                .build();
    } else if (oldAppender instanceof Log4jEventCollector) {
        Log4jEventCollector oldLog4jEventCollector = (Log4jEventCollector) oldAppender;
        return Log4jEventCollector.createAppender(oldLog4jEventCollector.getName(),
                (LevelRangeFilter) oldLog4jEventCollector.getFilter(),
                newLayout);
    }
    return null;
}

    private static List<String> splitAndStrip(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = input.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = StringUtils.strip(parts[i]);
        }
        return Arrays.asList(parts);
    }

}
