/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.uicomponents.bean;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:19:13
 */
public class Visibility {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Visibility.class);

    //visibility parameter
    private String siteActionPermission;
    private String serverActionPermission;
    private String needAuthentication;
    private String userAgent;
    private String value;

    public String getSiteActionPermission() {
        return siteActionPermission;
    }

    public void setSiteActionPermission(String siteActionPermission) {
        this.siteActionPermission = siteActionPermission;
    }

    public String getServerActionPermission() {
        return serverActionPermission;
    }

    public void setServerActionPermission(String serverActionPermission) {
        this.serverActionPermission = serverActionPermission;
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

    public boolean getRealValue(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, HttpServletRequest request) {
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

                    // check server permission
                    if (!isAllowedServerPermission(jahiaUser)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ServerAction: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("ServerPermission:: true");
                    }

                    // check site permission
                    if (!isAllowedSitePermission(jahiaUser, site)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("SitePermission:: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("SitePermission: true");
                    }

                    // check site permission
                    if (!isAllowedUserAgent(request)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("UserAgent: false");
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
     * Get serverPermission
     *
     * @param user
     * @return
     */
    private boolean isAllowedServerPermission(JahiaUser user) {
        if (serverActionPermission != null) {
//            ProcessingContext processingContext = jData.getProcessingContext();
//            Object o = processingContext.getAttribute(serverActionPermission);
//            if (o != null) {
//                return ((Boolean) o).booleanValue();
//            } else {
                boolean isAllowedServerPermission = user.isPermitted(new PermissionIdentity(serverActionPermission));
//                processingContext.setAttribute(serverActionPermission, new Boolean(isAllowedServerPermission));
                return isAllowedServerPermission;
//            }
        } else {
            return true;
        }
    }

    /**
     * Get sitePermission
     *
     * @return
     */
    private boolean isAllowedSitePermission(JahiaUser user,JCRSiteNode site) {
        if (siteActionPermission != null && !user.isRoot()) {
//            ProcessingContext processingContext = jahiaData.getProcessingContext();
//            Object o = processingContext.getAttribute(siteActionPermission);
//            logger.debug("Site action permission value: " + siteActionPermission);
//            if (o != null) {
//                logger.debug("Site permission value(from request): " + ((Boolean) o).booleanValue());
//                return ((Boolean) o).booleanValue();
//            } else {
                boolean isAllowedSitePermission = user.isPermitted(new PermissionIdentity(siteActionPermission,
                        site.getSiteKey()));
//                logger.debug("Site permission value: " + isAllowedSitePermission);
//                processingContext.setAttribute(siteActionPermission, new Boolean(isAllowedSitePermission));
                return isAllowedSitePermission;
//            }
        } else {
            return true;
        }
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
                    matches = userAgent.indexOf("MSIE") != -1;
                } else if ("ie6".equals(userAgent)) {
                    matches = userAgent.indexOf("MSIE 6") != -1;
                } else if ("ie7".equals(userAgent)) {
                    matches = userAgent.indexOf("MSIE 7") != -1;
                } else if ("ns".equals(userAgent)) {
                    matches = userAgent.indexOf("Mozilla") != -1 && userAgent.indexOf("MSIE") == -1;
                } else if ("ns4".equals(userAgent)) {
                    matches = userAgent.indexOf("Mozilla/4") != -1 && userAgent.indexOf("MSIE") == -1;
                } else if ("ns6".equals(userAgent)) {
                    matches = userAgent.indexOf("Mozilla/5") != -1;
                } else if ("opera".equals(userAgent)) {
                    matches = userAgent.indexOf("opera") != -1;
                }
            } else {
                return false;
            }
            return matches;
        }
        return true;
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
