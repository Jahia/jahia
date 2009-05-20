/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin.listeners;

import org.apache.log4j.Logger;
import org.apache.pluto.driver.PortalStartupListener;

import javax.servlet.ServletContextEvent;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 22 juil. 2008
 * Time: 17:03:45
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortalStartupListener extends PortalStartupListener {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaPortalStartupListener.class);
    
    public void contextInitialized(ServletContextEvent event) {
        try {
            if (event.getServletContext().getResource("/WEB-INF/etc/config/jahia.properties") != null) {
                super.contextInitialized(event);    //To change body of overridden methods use File | Settings | File Templates.
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (event.getServletContext().getResource("/WEB-INF/etc/config/jahia.properties") != null) {
                super.contextDestroyed(event);    //To change body of overridden methods use File | Settings | File Templates.
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
