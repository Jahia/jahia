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
package org.jahia.osgi;

import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * This LogBridge is used by a custom pax-logging-log4j2 appender to bring back up the log messages inside the OSGi runtime back into DX's
 * core. It also supports SLF4j's MDC bridging through the *MDC methods. These are deliberately not using the MDCAdapter interface to avoid
 * class loader issues (because this class is being exposed by the Jahia core).
 */
public class LogBridge {

    private LogBridge() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * Performs logging of the provided message and exception.
     *
     * @param loggerName the name of the logger
     * @param level the logging level
     * @param message the message to be logged
     * @param t the exception to be logged
     */
    public static void log(String loggerName, int level, Object message, Throwable t) {

        if (level == Level.OFF.intLevel()) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(loggerName);
        String msg = (message != null ? message.toString() : null);

        if (level <= Level.ERROR.intLevel()) {
            logger.error(msg, t);
        } else if (level <= Level.WARN.intLevel()) {
            logger.warn(msg, t);
        } else if (level <= Level.INFO.intLevel()) {
            logger.info(msg, t);
        } else if (level <= Level.DEBUG.intLevel()) {
            logger.debug(msg, t);
        } else {
            logger.trace(msg, t);
        }
    }

    /**
     * Put an entry in the DX core SLF4j MDC hashtable
     * @param key the key name for the entry
     * @param value the value as a String for the MDC entry
     */
    public static void putMDC(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Retrieve an MDC entry from the DX core SLF4j MDC hashtable
     * @param key the key name for the entry
     * @return the value for the entry (might be null if none exists)
     */
    public static String getMDC(String key) {
        return MDC.get(key);
    }

    /**
     * Remove an entry from the DX core SLF4j MDC hashtable
     * @param key the key name to be removed. If the key doesn't exist this method doesn't do anything.
     */
    public static void removeMDC(String key) {
        MDC.remove(key);
    }

    /**
     * Clears the DX core SLF4j MDC hashtable. Be careful with this method, it is better to remove entries by key name
     * because this method might clear data set by other sub-systems or modules
     */
    public static void clearMDC() {
        MDC.clear();
    }

    /**
     * Retrieve a copy of the whole DX core SLF4j MDC hashtable
     * @return despite the fact that this is a generic Map (for compatibility reasons), it should normally only contain
     * entries with String objects as key and String objects as values
     */
    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Set the whole DX core SLF4j MDC hashtable to a new map. Be VERY careful with this method, not only will it
     * overwrite data, but you must also make sure you provide a map that only contains keys and values that are String
     * objects.
     * @param contextMap a map that only contains keys and values that are String objects
     */
    public static void setContextMap(Map<String, String> contextMap) {
        MDC.setContextMap(contextMap);
    }
}
