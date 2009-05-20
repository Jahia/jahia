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
