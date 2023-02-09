/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.data.templates;

/**
 * Information required to perform module release operation, including target module Private App Store.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleReleaseInfo {

    private String forgeModulePageUrl;

    private String password;

    private String forgeUrl;

    private String username;

    private String nextVersion;

    private boolean publishToForge;

    private boolean publishToMaven;

    private String repositoryId;

    private String repositoryUrl;

    private String artifactUrl;

    public String getForgeModulePageUrl() {
        return forgeModulePageUrl;
    }

    public String getPassword() {
        return password;
    }

    public String getForgeUrl() {
        return forgeUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getNextVersion() {
        return nextVersion;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public boolean isPublishToForge() {
        return publishToForge;
    }

    public boolean isPublishToMaven() {
        return publishToMaven;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setForgeModulePageUrl(String forgeModulePageUrl) {
        this.forgeModulePageUrl = forgeModulePageUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setForgeUrl(String forgeUrl) {
        this.forgeUrl = forgeUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNextVersion(String nextVersion) {
        this.nextVersion = nextVersion;
    }

    public void setPublishToForge(boolean publishToForge) {
        this.publishToForge = publishToForge;
    }

    public void setPublishToMaven(boolean publishToMaven) {
        this.publishToMaven = publishToMaven;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }
}
