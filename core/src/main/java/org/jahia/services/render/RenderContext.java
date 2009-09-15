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
package org.jahia.services.render;

import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Template rendering context with the information about current request/response pair and optional template parameters.
 *
 * @author toto
 */
public class RenderContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Resource mainResource;
    private JahiaUser user;

    private boolean includeSubModules = true;
    private boolean isEditMode = false;

    private String templateWrapper;

    private Set<String> displayedModules = new HashSet<String>();
    private Map<String,Set<String>> externalLinks = new HashMap<String,Set<String>>();
    
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public RenderContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public JahiaUser getUser() {
        return user;
    }

    public Set<String> getDisplayedModules() {
        return displayedModules;
    }

    public boolean isIncludeSubModules() {
        return includeSubModules;
    }

    public void setIncludeSubModules(boolean includeSubModules) {
        this.includeSubModules = includeSubModules;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public String getTemplateWrapper() {
        return templateWrapper;
    }

    public void setTemplateWrapper(String pageWrapper) {
        this.templateWrapper = pageWrapper;
    }

    public void addExternalLink(String externalLinkType,String externalLink) {
        Set<String> externalLinkList = getExternalLinks(externalLinkType);
        if (externalLinkList == null) {
            externalLinkList = new HashSet<String>();
        }
        externalLinkList.add(externalLink);
        externalLinks.put(externalLinkType,externalLinkList);
    }

    public Set<String> getExternalLinks(String externalLinkType) {
        return externalLinks.get(externalLinkType);
    }

    public Map<String, Set<String>> getExternalLinks() {
        return externalLinks;
    }

    public void setMainResource(Resource mainResource) {
        this.mainResource = mainResource;
    }

    public Resource getMainResource() {
        return mainResource;
    }

	public Map<String, Object> getModuleParams() {
    	return parameters;
    }

}
