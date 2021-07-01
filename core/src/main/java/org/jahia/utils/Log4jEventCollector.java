/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;

/**
 * A Log4j appender that accumulates logging events and provides access to accumulated items.
 */
@Plugin(name = "Log4jEventCollector", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class Log4jEventCollector extends AbstractAppender {

    private boolean closed;
    private List<LogEvent> events = new LinkedList<>();
    
    @PluginFactory
    public static Log4jEventCollector createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("LevelRangeFilter") final LevelRangeFilter filter) {
        if (name == null) {
            LOGGER.error("A name for the Appender must be specified");
            return null;
        }
        return new Log4jEventCollector(name, null, filter);
    }

    private Log4jEventCollector(final String name, final Layout<? extends Serializable> layout, final LevelRangeFilter filter) {
        super(name, filter, layout, false);
    }

    @Override
    public synchronized void append(LogEvent event) {
        if (closed) {
            throw new IllegalStateException("Event collector is closed");
        }
        events.add(event);
    }

    /**
     * @return Logging events that have been collected
     */
    public synchronized List<LogEvent> getCollectedEvents() {
        return Collections.unmodifiableList(events);
    }
}