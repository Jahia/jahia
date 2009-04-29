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

import org.jahia.params.ParamBean;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.applications.EntryPointInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 29 juil. 2008
 * Time: 14:48:33
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContextRequest extends HttpServletRequestWrapper {

    private ParamBean paramBean;
    private String pathInfo = null;
    private String queryString = null;
    private String servletPath = null;
    private EntryPointInstance entryPointInstance;

    public JahiaContextRequest(ParamBean paramBean, HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
        this.paramBean = paramBean;
    }

    public String getServletPath() {
        if (servletPath == null) {
            return super.getServletPath();
        } else {
            return servletPath;
        }
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public String getPathInfo() {
        if (pathInfo == null) {
            return super.getPathInfo();
        } else {
            return pathInfo;
        }
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getQueryString() {
        if (queryString == null) {
            return super.getQueryString();
        } else {
            return queryString;
        }
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestURI() {
        return getContextPath() + getServletPath() + getPathInfo();
    }

    public String getRemoteUser() {
        return paramBean.getUser().getUserKey();
    }

    public Principal getUserPrincipal() {
        return paramBean.getUser();
    }

    public void setEntryPointInstance(EntryPointInstance entryPointInstance) {
        this.entryPointInstance = entryPointInstance;
    }

    public boolean isUserInRole(String role) {
        // This method maps servlet roles on Jahia's groups
        if (entryPointInstance == null) {
            return false;
        }
        return entryPointInstance.isUserInRole(paramBean.getUser(), role);
    }
}
