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
package org.jahia.test.services.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.log4j.Level;
import org.jahia.bin.listeners.LoggingConfigListener;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for logging in modules.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleLoggingTest extends JahiaTestCase {

    private static final Logger loggerA = LoggerFactory.getLogger(ModuleLoggingTest.class.getName() + ".loggerA");

    private static final Logger loggerB = LoggerFactory.getLogger(ModuleLoggingTest.class.getName() + ".loggerB");

    private static final Logger loggerC = LoggerFactory.getLogger(ModuleLoggingTest.class.getName() + ".loggerC");

    private static void assertLevel(Logger logger, int level) {
        logger.trace("Trace logging of logger {}", logger.getName());
        logger.debug("Debug logging of logger {}", logger.getName());
        logger.info("Info logging of logger {}", logger.getName());
        logger.warn("Warn logging of logger {}", logger.getName());
        logger.error("Error logging of logger {}", logger.getName());
        
        logger.error("Debug enabled for logger {}: {}", logger.getName(), logger.isDebugEnabled());

        if (level <= Level.TRACE_INT) {
            assertTrue(logger.isTraceEnabled());
        }
        if (level <= Level.DEBUG_INT) {
            assertTrue(logger.isDebugEnabled());
        }
        if (level <= Level.INFO_INT) {
            assertTrue(logger.isInfoEnabled());
        }
        if (level <= Level.ERROR_INT) {
            assertTrue(logger.isErrorEnabled());
        }

        if (level > Level.ERROR_INT) {
            assertFalse(logger.isErrorEnabled());
        }
        if (level > Level.WARN_INT) {
            assertFalse(logger.isWarnEnabled());
        }
        if (level > Level.INFO_INT) {
            assertFalse(logger.isInfoEnabled());
        }
        if (level > Level.DEBUG_INT) {
            assertFalse(logger.isDebugEnabled());
        }
        if (level > Level.TRACE_INT) {
            assertFalse(logger.isTraceEnabled());
        }
    }

    @Before
    public void defaultLoggingLevelIsInfo() throws RepositoryException, IOException {
        assertLevel(loggerA, Level.INFO_INT);
        assertLevel(loggerB, Level.INFO_INT);
        assertLevel(loggerC, Level.INFO_INT);
    }

    @Test
    public void loggerLevelIsError() throws RepositoryException, IOException {
        LoggingConfigListener.setLoggerLevel(loggerB.getName(), Level.ERROR.toString());
        TestHelper.sleep(2000);
        try {
            assertLevel(loggerA, Level.INFO_INT);
            assertLevel(loggerB, Level.ERROR_INT);
            assertLevel(loggerC, Level.INFO_INT);
        } finally {
            LoggingConfigListener.setLoggerLevel(loggerB.getName(), Level.INFO.toString());
            TestHelper.sleep(2000);
        }
    }
}
