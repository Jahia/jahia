/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.RpcMap;

/**
 * Represents release and distribution information for a module.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTModuleReleaseInfo extends RpcMap {

    private static final long serialVersionUID = -815808036831126232L;

    public String getForgeModulePageUrl() {
        return (String) get("forgeModulePageUrl");
    }

    public String getArtifactUrl() {
        return (String) get("artifactUrl");
    }

    public String getPassword() {
        return (String) get("password");
    }

    public String getForgeUrl() {
        return (String) get("forgeUrl");
    }

    public String getUsername() {
        return (String) get("username");
    }

    public String getNextVersion() {
        return (String) get("nextVersion");
    }

    public String getRepositoryId() {
        return (String) get("repositoryId");
    }

    public String getRepositoryUrl() {
        return (String) get("repositoryUrl");
    }

    public boolean isPublishToForge() {
        return containsKey("publishToForge") && (Boolean) get("publishToForge");
    }

    public boolean isPublishToMaven() {
        return containsKey("publishToMaven") && (Boolean) get("publishToMaven");
    }

    public void setForgeModulePageUrl(String pageUrl) {
        put("forgeModulePageUrl", pageUrl);
    }

    public void setArtifactUrl(String artifactUrl) {
        put("artifactUrl", artifactUrl);
    }

    public void setPassword(String password) {
        put("password", password);
    }

    public void setForgeUrl(String forgeUrl) {
        put("forgeUrl", forgeUrl);
    }

    public void setUsername(String username) {
        put("username", username);
    }

    public void setNextVersion(String nextVersion) {
        put("nextVersion", nextVersion);
    }

    public void setPublishToForge(boolean publishToForge) {
        put("publishToForge", Boolean.valueOf(publishToForge));
    }

    public void setPublishToMaven(boolean publishToMaven) {
        put("publishToMaven", Boolean.valueOf(publishToMaven));
    }

    public void setRepositoryId(String repositoryId) {
        put("repositoryId", repositoryId);
    }

    public void setRepositoryUrl(String repositoryUrl) {
        put("repositoryUrl", repositoryUrl);
    }

}
