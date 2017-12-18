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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Log4j appender that accumulates logging events and provides access to accumulated items.
 */
public class Log4jEventCollector extends AppenderSkeleton {

    private int minLevel;
    private int maxLevel;
    private boolean closed;
    private List<LoggingEvent> events = new LinkedList<LoggingEvent>();

    /**
     * Create a collector.
     * @param minLevel Minimum logging level of events to collect
     * @param maxLevel Maximum logging level of events to collect
     */
    public Log4jEventCollector(int minLevel, int maxLevel) {
        if (minLevel > maxLevel) {
            throw new IllegalArgumentException("Min level must be lower than or equal to max level");
        }
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    protected synchronized void append(LoggingEvent event) {
        if (closed) {
            throw new IllegalStateException("Event collector is closed");
        }
        int level = event.getLevel().toInt();
        if (level < minLevel || level > maxLevel) {
            return;
        }
        events.add(event);
    }

    @Override
    public synchronized void close() {
        closed = true;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    /**
     * @return Logging events that have been collected
     */
    public synchronized List<LoggingEvent> getCollectedEvents() {
        return Collections.unmodifiableList(events);
    }
}