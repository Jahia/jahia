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
package org.jahia.bin.errors;

import static org.junit.Assert.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jahia.utils.Log4jEventCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Unit tests for the StackTraceFilter
 */
public class StackTraceFilterTest {

    public static final String ERROR_MESSAGE = "Ignore this error it is a test";
    public static final String EVENT_COLLECTOR_APPENDER_NAME = "eventCollector";
    public static final String NPE_EXCEPTION_PREFIX = "java.lang.NullPointerException: Please Ignore";

    @Before
    public void setUp() {
        addLog4jAppender();
    }

    @After
    public void tearDown() {
        removeLog4jAppender();
    }

    @Test
    public void testStackTracePackageFiltering() {
        StackTraceFilter.init("java.lang.", 300, null);
        String result = StackTraceFilter.printStackTrace(new NullPointerException("Please Ignore"));
        result = result.substring(NPE_EXCEPTION_PREFIX.length()); // We strip the first line of the stack trace
        assertFalse("Filtered stack trace should not contain any classes from the java.lang package", result.contains("java.lang."));
    }

    @Test
    public void testStackTraceMaxNbLines() {
        StackTraceFilter.init(null, 3, null);
        String result = StackTraceFilter.printStackTrace(new NullPointerException("Please Ignore"));
        String[] lines = result.split("\n");
        assertEquals("Expected number of lines is incorrect", 3, lines.length);
    }

    @Test
    public void testLog4JConfiguration() {
        // Initialize the StackTraceFilter with the packages to filter, the maximum number of lines, and the appenders to modify
        initializeStackTraceFilter();

        // Create a log event for testing
        createLogEvent();

        // Check that the log event was correctly filtered
        checkFilteredLogEvent();
    }

    private static void addLog4jAppender() {
        // First we must create a pattern layout and the appender for the event collector appender
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withConfiguration(config).withPattern("%-5p - %encode{%.-500m}{CRLF} - %sxThrowable").build();
        Log4jEventCollector logEventCollector = Log4jEventCollector.createAppender(EVENT_COLLECTOR_APPENDER_NAME,
                LevelRangeFilter.createFilter(Level.getLevel(Level.ERROR.name()), Level.getLevel(Level.ERROR.name()), Filter.Result.ACCEPT, Filter.Result.DENY), layout);
        // Appenders must be started before we can use them
        logEventCollector.start();
        // Finally we add the appender to the root logger and update the existing loggers from the updated configuration
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(logEventCollector, null, null);
        ctx.updateLoggers();
    }

    private static void removeLog4jAppender() {
        LoggerConfig loggerConfig = getRootLoggerConfig();
        loggerConfig.getAppenders().get(EVENT_COLLECTOR_APPENDER_NAME).stop();
        loggerConfig.removeAppender(EVENT_COLLECTOR_APPENDER_NAME);
    }

    private static LoggerConfig getRootLoggerConfig() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        return config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    }


    private void initializeStackTraceFilter() {
        String[] filteredPackages = new String[]{"java.lang.", "org.junit.", "jdk.internal."};
        StackTraceFilter.init(StringUtils.join(filteredPackages, ","), 3, "Console,eventCollector");
    }

    private void createLogEvent() {
        final Logger logger = LogManager.getLogger(StackTraceFilterTest.class);
        logger.error(ERROR_MESSAGE, new NullPointerException("Please Ignore"));
    }

    private void checkFilteredLogEvent() {
        // First we have to retrieve the appender since it was modified by the StackTraceFilter.init method, we cannot
        // keep a reference to the appender in the test class because of this.
        LoggerConfig rootLoggerConfig = getRootLoggerConfig();
        Log4jEventCollector log4jEventCollector = (Log4jEventCollector) rootLoggerConfig.getAppenders().get(EVENT_COLLECTOR_APPENDER_NAME);
        List<LogEvent> logEvents = log4jEventCollector.getCollectedEvents();

        assertEquals("There should only be one log event", 1, logEvents.size());

        LogEvent logEvent = logEvents.get(0);

        assertEquals("Log event message is incorrect", ERROR_MESSAGE, logEvent.getMessage().getFormattedMessage());

        // The log events are not formatted, we must use the layout to format the log event, which will in turn trigger
        // the StackTrace filter to format the stack trace
        Layout<? extends Serializable> layout = log4jEventCollector.getLayout();
        String serializedLogEvent = (String) layout.toSerializable(logEvent);

        // Assert that the stack trace does not contain any classes from the filtered packages
        checkFilteredPackages(serializedLogEvent);

        // Assert that the number of lines in the stack trace is correct
        checkNumberOfLines(serializedLogEvent);
    }

    private void checkFilteredPackages(String serializedLogEvent) {
        String[] filteredPackages = new String[]{"java.lang.", "org.junit.", "jdk.internal."};
        for (String filteredPackage : filteredPackages) {
            assertFalse("Filtered stack trace should not contain any classes from the " + filteredPackage + " package", serializedLogEvent.substring(NPE_EXCEPTION_PREFIX.length()).contains(filteredPackage));
        }
    }

    private void checkNumberOfLines(String serializedLogEvent) {
        String[] lines = serializedLogEvent.split("\n");
        assertEquals("Expected number of lines is incorrect", 3, lines.length);
    }

}
