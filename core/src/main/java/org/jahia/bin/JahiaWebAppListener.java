/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.bin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sitemap.JahiaSiteMapService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * This class is used to implements listeners to the servlet containers events.
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 * @author Serge Huber
 * @version 3.0
 */

public class JahiaWebAppListener implements HttpSessionListener, ServletContextListener {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaWebAppListener.class);

    public JahiaWebAppListener () {
    }

    /**
     * Notification that a session was created.
     * @param se the notification event
     */
    public void sessionCreated (HttpSessionEvent se) {
        logger.debug("Session " + se.getSession().getId() + " was just created...");
    }

    /**
     * Notification that a session was destroyed.
     * 
     * @param se
     *            the notification event
     */
    public void sessionDestroyed(HttpSessionEvent se) {
        if (org.jahia.settings.SettingsBean.getInstance() == null) {
            // Jahia was not yet installed, let's exit now.
            return;
        }
        String sessionId = se.getSession().getId();
        if (logger.isDebugEnabled()) {
            logger.debug("Session " + sessionId + " was just destroyed."
                    + " Purging session related data...");
        }
        ServicesRegistry servReg = ServicesRegistry.getInstance();
        if (servReg != null) {
            try {
                JahiaSiteMapService siteMapService = servReg
                        .getJahiaSiteMapService();
                if (siteMapService != null) {
                    siteMapService.removeSessionSiteMap(sessionId);
                }
            } catch (Exception e) {
                logger.error("Error removing site map session cache for session ID '"
                        + sessionId + "'", e);
            }
            JahiaUser u = null;
            try {
                u = (JahiaUser) se.getSession().getAttribute(
                        ParamBean.SESSION_USER);
            } catch (IllegalStateException ise) {
                logger.info("Skip getting user for session ID '" + sessionId
                        + "' as the session is already invalidated");
            } catch (Exception e) {
                logger.error("Error getting user for session ID '" + sessionId
                        + "'", e);
            }
            if (u != null) {
                try {
                    servReg.getLockService()
                            .purgeLockForContext(u.getUserKey());
                } catch (Exception e) {
                    logger.error("Error purging locks for user '"
                            + u.getUserKey() + "' and session ID '" + sessionId
                            + "'", e);
                }                
            }
        }
    }

    public void contextInitialized(ServletContextEvent event) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
