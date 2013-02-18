/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.osgi;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * OSGi framework service
 *
 * @author loom
 */
public class FrameworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);
    
    private static FrameworkService instance;
    private final ServletContext context;
    private Felix felix;
    private ProvisionActivator provisionActivator = null;

    public FrameworkService(ServletContext context) {
        this.context = context;
        instance = this;
    }

    public void start() throws BundleException {
        Felix tmp = new Felix(createConfig());
        tmp.start();
        this.felix = tmp;

        logger.info("OSGi framework started");
    }

    public void stop() throws BundleException {
        provisionActivator = null;
        if (this.felix != null) {
            this.felix.stop();
            logger.info("Waiting for OSGi framework shutdown...");
            try {
                this.felix.waitForStop(10000);
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
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : unreplaced.entrySet()) {
            map.put(entry.getKey(), placeholderHelper.replacePlaceholders(entry.getValue(), systemProps));
        }

        map.put("org.jahia.servlet.context", context);

        provisionActivator = new ProvisionActivator(this.context);
        map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(provisionActivator));
        return map;
    }

    public static BundleContext getBundleContext() {
        if (instance != null && instance.felix != null) {
            return instance.felix.getBundleContext();
        } else {
            return null;
        }
    }
}
