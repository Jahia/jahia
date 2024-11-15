/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.pax.logging.bridging.appender;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jahia.osgi.LogBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * An appender for the Log4J using inside the pax-logging-log4j2 implementation that will bridge the log messages
 * to the Log4J implementation used by Jahia's core. This appender also support SLF4j's MDC by bridging it into the core's
 * SLF4j MDC. This is currently using reflection API to inject the MDC adapter into the MDC class, so this might easily
 * break if upgrading SLF4j.
 */
@Plugin(name = "LogBridgeAppender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class LogBridgeAppender extends AbstractAppender {
    private static final Logger logger = LoggerFactory.getLogger(LogBridgeAppender.class);

    MDCAdapter logBridgeAdapter = null;

    @PluginFactory
    public static LogBridgeAppender createAppender(
            @PluginAttribute("name") final String name) {
        return new LogBridgeAppender(name, null, null);
    }

    private LogBridgeAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter) {
        super(name, filter, layout, false, Property.EMPTY_ARRAY);
        init();
    }

    @Override
    public void append(LogEvent event) {
        LogBridge.log(event.getLoggerName(), event.getLevel().intLevel(), event.getMessage(), event.getThrown());
    }

    private void init() {
        logBridgeAdapter = new LogBridgeAdapter(MDC.getMDCAdapter());
        try {
            Field mdcAdapterField = MDC.class.getDeclaredField("mdcAdapter");
            mdcAdapterField.setAccessible(true);
            mdcAdapterField.set(null, logBridgeAdapter);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warn("Error initializing log bridge adapter", e);
        }
    }

    /**
     * This is the MDC Adapter class that will wrap an existing MDC adapter and will be injected into the MDC static
     * field.
     */
    public class LogBridgeAdapter implements MDCAdapter {

        MDCAdapter wrappedMDCAdapter = null;

        public LogBridgeAdapter(MDCAdapter wrappedMDCAdapter) {
            this.wrappedMDCAdapter = wrappedMDCAdapter;
        }

        @Override
        public void put(String key, String val) {
            wrappedMDCAdapter.put(key, val);
            LogBridge.putMDC(key, val);
        }

        @Override
        public String get(String key) {
            wrappedMDCAdapter.get(key);
            return LogBridge.getMDC(key);
        }

        @Override
        public void remove(String key) {
            wrappedMDCAdapter.remove(key);
            LogBridge.removeMDC(key);
        }

        @Override
        public void clear() {
            wrappedMDCAdapter.clear();
            LogBridge.clearMDC();
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return LogBridge.getCopyOfContextMap();
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            wrappedMDCAdapter.setContextMap(contextMap);
            LogBridge.setContextMap(contextMap);
        }
    }
}
