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
package org.jahia.services.applications.pluto;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.driver.core.PortalServletRequest;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.applications.EntryPointInstance;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 30 juil. 2008
 * Time: 12:27:46
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortalServletRequest extends PortalServletRequest {
    private JahiaUser jahiaUser;
    private String id;
    private EntryPointInstance entryPointInstance;

    public JahiaPortalServletRequest(EntryPointInstance entryPointInstance,JahiaUser jahiaUser, HttpServletRequest request, PortletWindow window) {
        super(request, window);
        this.jahiaUser = jahiaUser;
        id = window.getId().getStringId();
        this.entryPointInstance = entryPointInstance;
    }

    public String getRemoteUser() {
        return jahiaUser.getUserKey();
    }

    public Principal getUserPrincipal() {
        return jahiaUser;
    }

    public String getId() {
        return id;
    }

    public boolean isUserInRole(String role) {
        // This method maps servlet roles on Jahia's groups
        if (entryPointInstance == null) {
            return false;
        }
        return entryPointInstance.isUserInRole(jahiaUser, role);
    }
}
