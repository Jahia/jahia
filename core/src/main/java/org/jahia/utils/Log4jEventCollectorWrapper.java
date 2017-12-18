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
package org.jahia.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Wraps the Log4jEventCollector providing for methods similar to Log4jEventCollector, which are decoupled from Log4J however.
 * <p>
 * A specific need for this wrapper is to mediate usage of some Log4J classes (like Appender) which are not considered a part of the pax logging API
 * whenever we still need to use them in modules, in order to avoid class loading issues.
 */
public class Log4jEventCollectorWrapper implements AutoCloseable {

    private Log4jEventCollector target;

    /**
     * Wraps the Log4J LoggingEvent.
     */
    public static class LoggingEventWrapper {

        private LoggingEvent target;

        /**
         * Create a logging event.
         * @param target Underlying Log4J logging event
         */
        public LoggingEventWrapper(LoggingEvent target) {
            this.target = target;
        }

        /**
         * @return Event level
         */
        public int getLevel() {
            return target.getLevel().toInt();
        }

        /**
         * @return Event timestamp
         */
        public long getTimestamp() {
            return target.getTimeStamp();
        }

        /**
         * @return Event message
         */
        public String getMessage() {
            return target.getRenderedMessage();
        }

        /**
         * @return Event exception info if any, null otherwise
         */
        public String[] getThrowableInfo() {
            return target.getThrowableStrRep();
        }
    }

    /**
     * Create a wrapper and start collecting events.
     * @param level Logging level of events to collect
     */
    public Log4jEventCollectorWrapper(int level) {
        this(level, level);
    }

    /**
     * Create a wrapper and start collecting events.
     * @param minLevel Minimum logging level of events to collect
     * @param maxLevel Maximum logging level of events to collect
     */
    public Log4jEventCollectorWrapper(int minLevel, int maxLevel) {
        target = new Log4jEventCollector(minLevel, maxLevel);
        Logger.getRootLogger().addAppender(target);
    }

    @Override
    public void close() {
        Logger.getRootLogger().removeAppender(target);
        target.close();
    }

    /**
     * @return Collected logging events.
     */
    public List<LoggingEventWrapper> getCollectedEvents() {
        return convert(target.getCollectedEvents());
    }

    private static List<LoggingEventWrapper> convert(List<LoggingEvent> events) {
        ArrayList<LoggingEventWrapper> result = new ArrayList<LoggingEventWrapper>(events.size());
        for (LoggingEvent event : events) {
            result.add(new LoggingEventWrapper(event));
        }
        return result;
    }
}
