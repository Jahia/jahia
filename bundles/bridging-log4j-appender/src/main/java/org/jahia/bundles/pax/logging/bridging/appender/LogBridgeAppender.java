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
package org.jahia.bundles.pax.logging.bridging.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jahia.osgi.LogBridge;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An appender for the Log4J using inside the pax-logging-service implementation that will bridge the log messages
 * to the Log4J implementation used by DX's core. This appender also support SLF4j's MDC by bridging it into the core's
 * SLF4j MDC. This is currently using reflection API to inject the MDC adapter into the MDC class, so this might easily
 * break if upgrading SLF4j.
 */
public class LogBridgeAppender extends AppenderSkeleton {

    MDCAdapter logBridgeAdapter = null;

    public LogBridgeAppender() {
        super();
        init();
    }

    public LogBridgeAppender(boolean isActive) {
        super(isActive);
        init();
    }

    @Override
    protected void append(LoggingEvent event) {
        ThrowableInformation throwableInfo = event.getThrowableInformation();
        LogBridge.log(event.getLogger().getName(), event.getLevel().toInt(), event.getMessage(), (throwableInfo != null ? throwableInfo.getThrowable() : null));
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    private void init() {
        logBridgeAdapter = new LogBridgeAdapter(MDC.getMDCAdapter());
        try {
            Field mdcAdapterField = MDC.class.getDeclaredField("mdcAdapter");
            mdcAdapterField.setAccessible(true);
            mdcAdapterField.set(null, logBridgeAdapter);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
        public Map getCopyOfContextMap() {
            return LogBridge.getCopyOfContextMap();
        }

        @Override
        public void setContextMap(Map contextMap) {
            wrappedMDCAdapter.setContextMap(contextMap);
            LogBridge.setContextMap(contextMap);
        }
    }
}