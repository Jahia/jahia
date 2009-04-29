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
package org.jahia.admin;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.JahiaAdministration;
import org.jahia.params.ParamBean;
import org.jahia.exceptions.JahiaException;

import javax.servlet.ServletContext;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 2, 2009
 * Time: 10:04:21 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAdministrationModule implements AdministrationModule {

    private String icon;
    private String label;
    private String link;
    private String name;
    private String tooltip;
    private String urlKey;
    private String permissionName;
    private String urlType;
    private String urlAction;
    private String urlParams;
    private boolean serverModule;

    private boolean hasServerPermission(String permissionName, JahiaUser user, int siteID) {
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        return aclService.getServerActionPermission(permissionName,
                user,
                JahiaBaseACL.READ_RIGHTS,
                siteID) > 0;
    }

    private boolean hasSitePermission(String permissionName, JahiaUser user, int siteID) {
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        return aclService.getSiteActionPermission(permissionName,
                user,
                JahiaBaseACL.READ_RIGHTS,
                siteID) > 0;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isServerModule() {
        return serverModule;
    }

    public void setServerModule(boolean serverModule) {
        this.serverModule = serverModule;
    }

    public boolean isEnabled(JahiaUser user, int siteID) {
        if (StringUtils.isEmpty(permissionName)) {
            // no permission check required
            return true;
        }
        if (isServerModule()) {
            return hasServerPermission(permissionName, user, siteID);
        } else {
            return hasSitePermission(permissionName, user, siteID);
        }
    }
    
    public boolean isSelected(ParamBean ctx) {
        boolean selected = false;
        if ("module".equals(urlType)) {
            selected = StringUtils.equals(getUrlAction(), ctx.getParameter("do"));
        } else if ("struts".equals(urlType)) {
            selected = StringUtils.equals(getUrlKey(), ctx.getParameter("group"));
        }
        
        return selected;
    }

    public String getActionURL(ParamBean paramBean) throws JahiaException {
        if ("module".equals(urlType)) {
            return JahiaAdministration.composeActionURL(paramBean.getRequest(),paramBean.getResponse(),urlAction,urlParams);
        } else if ("struts".equals(urlType)) {
            return paramBean.composeStrutsUrl(urlAction,urlParams);
        } else {
            return urlAction;
        }
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getUrlType() {
        return urlType;
    }

    public void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    public String getUrlAction() {
        return urlAction;
    }

    public void setUrlAction(String urlAction) {
        this.urlAction = urlAction;
    }

    public String getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(String urlParams) {
        this.urlParams = urlParams;
    }

    public void init(ServletContext servletContext) {
    }
}
