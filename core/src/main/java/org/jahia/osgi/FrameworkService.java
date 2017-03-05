/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.servlet.ServletContext;

import java.util.*;

/**
 * OSGi framework service
 *
 * @author Serge Huber
 */
public class FrameworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final FrameworkService INSTANCE = new FrameworkService(JahiaContextLoaderListener.getServletContext());

        private Holder() {
        }
    }

    public static FrameworkService getInstance() {
        return Holder.INSTANCE;
    }

    private final ServletContext context;
    private Felix felix;
    private ProvisionActivator provisionActivator = null;
    
    private boolean started;

    private FrameworkService(ServletContext context) {
        this.context = context;
    }

    public void start() throws BundleException {
        Felix tmp = new Felix(createConfig());
        tmp.start();
        this.felix = tmp;
    }

    public void stop() throws BundleException {
        provisionActivator = null;
        if (this.felix != null) {
            FrameworkEvent stopEvent;
            this.felix.stop();
            logger.info("Waiting for OSGi framework shutdown...");
            try {
                stopEvent = this.felix.waitForStop(30000);
                logger.info("Framework stopped with event {}", stopEvent.getType());
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info("OSGi framework stopped");
    }

    public ProvisionActivator getProvisionActivator() {
        return provisionActivator;
    }

    private Map<String, Object> createConfig() {

        @SuppressWarnings("unchecked")
        Map<String,String> unreplaced = (Map<String,String>) SpringContextSingleton.getBean("felixProperties");
        
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");
        Properties systemProps = System.getProperties();
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry : unreplaced.entrySet()) {
            map.put(entry.getKey(), placeholderHelper.replacePlaceholders(entry.getValue(), systemProps));
        }
        String value = (String) map.get("gosh.args");
        if (value != null && value.contains("--port=-1")) {
            map.put("gosh.args", "--nointeractive");
        }

        StringBuilder extra = new StringBuilder((String) map.get("org.osgi.framework.system.packages.extra"));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().startsWith("org.osgi.framework.system.packages.extra.")) {
                extra.append(',').append(entry.getValue());
            }
        }
        map.put("org.osgi.framework.system.packages.extra",extra.toString());

        map.put("org.jahia.servlet.context", context);

        provisionActivator = new ProvisionActivator(this.context);
        map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Collections.singletonList(provisionActivator));
        return map;
    }

    public boolean isStarted() {
        return started;
    }
    
    public static BundleContext getBundleContext() {
        final FrameworkService instance = getInstance();
        if (instance != null && instance.felix != null) {
            return instance.felix.getBundleContext();
        } else {
            return null;
        }
    }
    
    /**
     * Notify this service that the framework has actually started.
     */
    public static void notifyStarted() {
        logger.info("Got started event");
        final FrameworkService instance = getInstance();
        synchronized (instance) {
            logger.info("Started event arrived");
            instance.started = true;
            instance.notifyAll();
            logger.info("Notified all about framework started event");
        }
    }
}
