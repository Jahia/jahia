/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
