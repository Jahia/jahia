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
package org.jahia.services.toolbar.bean;

import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:19:13
 */
public class Visibility {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Visibility.class);

    //visibility parameter
    private String mode;
    private String pageACL;
    private String siteActionPermission;
    private String serverActionPermission;
    private String needAuthentication;
    private String value;
    private String classResolver;
    private String inputResolver;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPageACL() {
        return pageACL;
    }

    public void setPageACL(String pageACL) {
        this.pageACL = pageACL;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getClassResolver() {
        return classResolver;
    }

    public void setClassResolver(String classResolver) {
        this.classResolver = classResolver;
    }

    public String getInputResolver() {
        return inputResolver;
    }

    public void setInputResolver(String inputResolver) {
        this.inputResolver = inputResolver;
    }

    public boolean getRealValue(org.jahia.data.JahiaData jData) {
        if (value != null) {
            if (logger.isDebugEnabled()) logger.debug("Value: " + value);
            return Boolean.getBoolean(value);
        } else {
            try {
                if(jData == null){
                    logger.error("ParamBean is not set. The item will not be displayed ");
                    return false;
                }
                // resolver is more "important" than attributes
                if (classResolver != null) {
                    VisibilityResolver resolver = (VisibilityResolver) Class.forName(classResolver).newInstance();
                    boolean isVisible = resolver.isVisible(jData, inputResolver);
                    logger.debug("ClassResolver [" + classResolver + "]" + isVisible);
                    return isVisible;
                }
                // check attributes
                else {
                    // check logging
                    JahiaUser jahiaUser = jData.getProcessingContext().getUser();
                    boolean isLogging = isLogging(jahiaUser);
                    if (!isLogging) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Logging: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Logging: true");
                    }

                    // check mode
                    boolean isAllowedMode = isAllowedMode(jData);
                    if (!isAllowedMode) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Mode: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("allowed mode: true");
                    }

                    // check server permission
                    if (!isAllowedServerPermission(jData)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ServerAction: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("ServerPermission:: true");
                    }

                    // check site permission
                    if (!isAllowedSitePermission(jData)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("SitePermission:: false");
                        }
                        return false;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("SitePermission: true");
                    }

                    // check permission
                    boolean isAllowedPageACL = true;
                    if (pageACL != null) {
                        int acl = JahiaBaseACL.ALL_RIGHTS;
                        if (pageACL.equalsIgnoreCase("admin") || pageACL.indexOf('a') == 2) {
                            acl = JahiaBaseACL.ADMIN_RIGHTS;
                        } else if (pageACL.equalsIgnoreCase("write") || pageACL.indexOf('w') == 1) {
                            acl = JahiaBaseACL.WRITE_RIGHTS;
                        } else if (pageACL.equalsIgnoreCase("read") || pageACL.indexOf('r') == 0) {
                            acl = JahiaBaseACL.READ_RIGHTS;
                        }
                        if (acl != JahiaBaseACL.ALL_RIGHTS) {
                            JahiaBaseACL jahiaBaseACL = jData.getProcessingContext().getPage().getACL();
                            isAllowedPageACL = jahiaBaseACL.getPermission(jahiaUser, acl);
                        }
                    }
                    if (!isAllowedPageACL) {
                        return false;
                    }
                    if (logger.isDebugEnabled()) logger.debug("Permisions: true");

                    return true;
                }
            } catch (final Exception e) {
                logger.error("Error in getRealValue", e);
                return true;
            }
        }
    }

    /**
     * Get serverPermission
     *
     * @param jData
     * @return
     */
    private boolean isAllowedServerPermission(org.jahia.data.JahiaData jData) {
        if (serverActionPermission != null) {
            ProcessingContext processingContext = jData.getProcessingContext();
            JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            Object o = processingContext.getAttribute(serverActionPermission);
            if (o != null) {
                return ((Boolean) o).booleanValue();
            } else {
                boolean isAllowedServerPermission = aclService.getServerActionPermission(serverActionPermission, processingContext.getUser(), JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) > 0;
                processingContext.setAttribute(serverActionPermission, new Boolean(isAllowedServerPermission));
                return isAllowedServerPermission;
            }
        } else {
            return true;
        }
    }

    /**
     * Get sitePermission
     *
     * @param jahiaData
     * @return
     */
    private boolean isAllowedSitePermission(org.jahia.data.JahiaData jahiaData) {
        if (siteActionPermission != null) {
            ProcessingContext processingContext = jahiaData.getProcessingContext();
            JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            Object o = processingContext.getAttribute(siteActionPermission);
            logger.debug("Site action permission value: " + siteActionPermission);
            if (o != null) {
                logger.debug("Site permission value(from request): " + ((Boolean) o).booleanValue());
                return ((Boolean) o).booleanValue();
            } else {
                boolean isAllowedSitePermission = aclService.getSiteActionPermission(siteActionPermission, 
                        processingContext.getUser(), JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) > 0;
                logger.debug("Site permission value: " + isAllowedSitePermission);
                processingContext.setAttribute(siteActionPermission, new Boolean(isAllowedSitePermission));
                return isAllowedSitePermission;
            }
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
    private boolean isLogging(JahiaUser jahiaUser) {
        if (needAuthentication != null) {
            if (Boolean.parseBoolean(needAuthentication)) {
                return jahiaUser != null && (AdvPreviewSettings.isInUserAliasingMode() ||
                        !jahiaUser.getUsername().equalsIgnoreCase(JahiaUserManagerProvider.GUEST_USERNAME));
            }
        }
        return true;
    }

    /**
     * True id the mode is allowed
     *
     * @param jData
     * @return
     */
    private boolean isAllowedMode(org.jahia.data.JahiaData jData) {
        return mode == null ||
                mode.indexOf(jData.getProcessingContext().getOperationMode()) > -1;
    }
}
