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
import org.apache.logging.log4j.core.config.Property;
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

    public static Log4jEventCollector createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("LevelRangeFilter") final LevelRangeFilter filter, final Layout<? extends Serializable> layout) {
        if (name == null) {
            LOGGER.error("A name for the Appender must be specified");
            return null;
        }
        return new Log4jEventCollector(name, layout, filter);
    }

    private Log4jEventCollector(final String name, final Layout<? extends Serializable> layout, final LevelRangeFilter filter) {
        super(name, filter, layout, false, Property.EMPTY_ARRAY);
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
