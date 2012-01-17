/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.uicomponents.bean;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:19:13
 */
public class Visibility {
    private static final Logger logger = LoggerFactory.getLogger(Visibility.class);

    //visibility parameter
    private String permission;
    private String needAuthentication;
    private String userAgent;
    private String value;
    private String contextNodePath;
    private String inNodePath;
    private String operatingMode;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getNeedAuthentication() {
        return needAuthentication;
    }

    public void setNeedAuthentication(String needAuthentication) {
        this.needAuthentication = needAuthentication;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContextNodePath() {
        return contextNodePath;
    }

    public void setContextNodePath(String contextNodePath) {
        this.contextNodePath = contextNodePath;
    }

    public String getInNodePath() {
        return inNodePath;
    }

    public void setInNodePath(String inNodePath) {
        this.inNodePath = inNodePath;
    }

    public String getOperatingMode() {
        return operatingMode;
    }

    public void setOperatingMode(String operatingMode) {
        this.operatingMode = operatingMode;
    }

    public boolean getRealValue(JCRNodeWrapper contextNode, JahiaUser jahiaUser, Locale locale, HttpServletRequest request) {
        if (value != null) {
            if (logger.isDebugEnabled()) logger.debug("Value: " + value);
            return Boolean.getBoolean(value);
        } else {
            try {
                // check attributes
                // check logging
                boolean isLogged = isLogged(jahiaUser);
                if (!isLogged) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Logging: false");
                    }
                    return false;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Logging: true");
                }
                if (inNodePath != null && !contextNode.getPath().startsWith(inNodePath)) {
                    return false;
                }
                try {
                    if (contextNodePath != null) {
                        contextNode = contextNode.getSession().getNode(contextNodePath);
                    }
                } catch (PathNotFoundException e) {
                    return false;
                }

                if (!isAllowed(contextNode)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SitePermission:: false");
                    }
                    return false;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("SitePermission: true");
                }

                if (!isAllowedUserAgent(request)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("UserAgent: false");
                    }
                    return false;
                }

                if (!isAllowedOperatingMode()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("OperatingMode: false");
                    }
                    return false;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("UserAgent: true");
                }


                if (logger.isDebugEnabled()) logger.debug("Permissions: true");

                return true;
            } catch (final Exception e) {
                logger.error("Error in getRealValue", e);
                return true;
            }
        }
    }

    /**
     * Get sitePermission
     *
     * @return
     */
    private boolean isAllowed(JCRNodeWrapper node) {
        if (permission != null && !node.getUser().isRoot()) {
            try {
                return node.hasPermission(permission);
            } catch (Exception e) {
                logger.error("Cannot check permission " + permission, e);
            }
        }
        return true;
    }

    /**
     * True if user is not guest
     *
     * @param jahiaUser
     * @return
     */
    private boolean isLogged(JahiaUser jahiaUser) {
        if (needAuthentication != null) {
            if (Boolean.parseBoolean(needAuthentication)) {
                return jahiaUser != null && (!jahiaUser.getUsername().equalsIgnoreCase(JahiaUserManagerService.GUEST_USERNAME));
            }
        }
        return true;
    }


    private boolean isAllowedUserAgent(HttpServletRequest request) {
        if (userAgent != null) {
            boolean matches = false;
            String thisUserAgent = resolveUserAgent(request);
            if (thisUserAgent != null) {
                if ("ie".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("MSIE") != -1;
                } else if ("ie6".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("MSIE 6") != -1;
                } else if ("ie7".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("MSIE 7") != -1;
                } else if ("ns".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("Mozilla") != -1 && userAgent.indexOf("MSIE") == -1;
                } else if ("ns4".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("Mozilla/4") != -1 && userAgent.indexOf("MSIE") == -1;
                } else if ("ns6".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("Mozilla/5") != -1;
                } else if ("opera".equals(userAgent)) {
                    matches = thisUserAgent.indexOf("opera") != -1;
                }
            } else {
                return false;
            }
            return matches;
        }
        return true;
    }

    private boolean isAllowedOperatingMode() {
        return (operatingMode == null) ||
                (operatingMode.contains("development") && SettingsBean.getInstance().isDevelopmentMode()) ||
                (operatingMode.contains("production") && SettingsBean.getInstance().isProductionMode()) ||
                (operatingMode.contains("distantPublicationServer") && SettingsBean.getInstance().isDistantPublicationServerMode());
    }

    private String resolveUserAgent(HttpServletRequest request) {
        String reqUserAgent = "";
        Enumeration<?> userAgentValues = request.getHeaders("user-agent");
        if (userAgentValues.hasMoreElements()) {
            // we only use the first value.
            reqUserAgent = (String) userAgentValues.nextElement();
        }
        return reqUserAgent;
    }


}
