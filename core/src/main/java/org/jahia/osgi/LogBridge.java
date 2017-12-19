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
package org.jahia.osgi;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * This LogBridge is used by a custom pax-logging-service appender to bring back up the log messages inside the OSGi runtime back into DX's
 * core. It also supports SLF4j's MDC bridging through the *MDC methods. These are deliberately not using the MDCAdapter interface to avoid
 * class loader issues (because this class is being exposed by the DX core).
 */
public class LogBridge {

    /**
     * Performs logging of the provided message and exception.
     *
     * @param loggerName the name of the logger
     * @param level the logging level
     * @param message the message to be logged
     * @param t the exception to be logged
     */
    public static void log(String loggerName, int level, Object message, Throwable t) {

        if (level == Level.OFF_INT) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(loggerName);
        String msg = (message != null ? message.toString() : null);

        if (level >= Level.ERROR_INT) {
            logger.error(msg, t);
        } else if (level >= Level.WARN_INT) {
            logger.warn(msg, t);
        } else if (level >= Level.INFO_INT) {
            logger.info(msg, t);
        } else if (level >= Level.DEBUG_INT) {
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
    public static Map getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Set the whole DX core SLF4j MDC hashtable to a new map. Be VERY careful with this method, not only will it
     * overwrite data, but you must also make sure you provide a map that only contains keys and values that are String
     * objects.
     * @param contextMap a map that only contains keys and values that are String objects
     */
    public static void setContextMap(Map contextMap) {
        MDC.setContextMap(contextMap);
    }
}
