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
package org.jahia.services.uicomponents.bean;

import org.jahia.services.content.JCRContentUtils;
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
import java.util.List;
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
    private boolean needsMavenExecutable;

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

    public void setNeedsMavenExecutable(boolean needsMavenExecutable) {
        this.needsMavenExecutable = needsMavenExecutable;
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

                if (!isAllowed(contextNode)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SitePermission: false");
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

                if (!isAllowedMavenExecutable()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("MavenExecutable: false");
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
        try {
            if (permission != null && !node.getUser().isRoot()) {
                if ("$currentsite".equals(contextNodePath)) {
                    node = node.getResolveSite();
                } else if ("$anysite".equals(contextNodePath)) {
                    // Read sites list for edit mode access permission instead of modules list
                    JCRNodeWrapper sitesPath = node.getSession().getNode("/sites");
                    List<JCRNodeWrapper> l = JCRContentUtils.getChildrenOfType(sitesPath, "jnt:virtualsite");
                    for (JCRNodeWrapper nodeWrapper : l) {
                        if (nodeWrapper.hasPermission(permission)) {
                            return true;
                        }
                    }
                    return false;
                } else if (contextNodePath != null && !contextNodePath.startsWith("/")) {
                    node = node.getNode(contextNodePath);
                } else if (contextNodePath != null) {
                    node = node.getSession().getNode(contextNodePath);
                }
                return node.hasPermission(permission);
            }
        } catch (PathNotFoundException e) {
            return false;
        } catch (RepositoryException e) {
            logger.error("Cannot check permission " + permission, e);
            return false;
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
                (operatingMode.contains("production") && (SettingsBean.getInstance().isProductionMode() && !SettingsBean.getInstance().isDistantPublicationServerMode())) ||
                (operatingMode.contains("distantPublicationServer") && SettingsBean.getInstance().isDistantPublicationServerMode());
    }

    private boolean isAllowedMavenExecutable() {
        return !needsMavenExecutable || SettingsBean.getInstance().isMavenExecutableSet();
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
