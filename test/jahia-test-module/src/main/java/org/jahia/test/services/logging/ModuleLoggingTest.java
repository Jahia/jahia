/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.logging.log4j.Level;
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

        if (level >= Level.TRACE.intLevel()) {
            assertTrue(logger.isTraceEnabled());
        }
        if (level >= Level.DEBUG.intLevel()) {
            assertTrue(logger.isDebugEnabled());
        }
        if (level >= Level.INFO.intLevel()) {
            assertTrue(logger.isInfoEnabled());
        }
        if (level >= Level.ERROR.intLevel()) {
            assertTrue(logger.isErrorEnabled());
        }

        if (level < Level.ERROR.intLevel()) {
            assertFalse(logger.isErrorEnabled());
        }
        if (level < Level.WARN.intLevel()) {
            assertFalse(logger.isWarnEnabled());
        }
        if (level < Level.INFO.intLevel()) {
            assertFalse(logger.isInfoEnabled());
        }
        if (level < Level.DEBUG.intLevel()) {
            assertFalse(logger.isDebugEnabled());
        }
        if (level < Level.TRACE.intLevel()) {
            assertFalse(logger.isTraceEnabled());
        }
    }

    @Before
    public void defaultLoggingLevelIsInfo() {
        assertLevel(loggerA, Level.INFO.intLevel());
        assertLevel(loggerB, Level.INFO.intLevel());
        assertLevel(loggerC, Level.INFO.intLevel());
    }

    @Test
    public void loggerLevelIsError() throws RepositoryException, IOException {
        LoggingConfigListener.setLoggerLevel(loggerB.getName(), Level.ERROR.toString());
        TestHelper.sleep(2000);
        try {
            assertLevel(loggerA, Level.INFO.intLevel());
            assertLevel(loggerB, Level.ERROR.intLevel());
            assertLevel(loggerC, Level.INFO.intLevel());
        } finally {
            LoggingConfigListener.setLoggerLevel(loggerB.getName(), Level.INFO.toString());
            TestHelper.sleep(2000);
        }
    }
}
