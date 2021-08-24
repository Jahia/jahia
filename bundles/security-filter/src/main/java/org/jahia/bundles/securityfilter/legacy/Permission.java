/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.securityfilter.legacy;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Permission configuration entry for API access
 *
 * Will check the requiredPermission if all conditions api / workspace / pathPattern / scopes / nodeType matches
 */
public class Permission {
    public static enum AccessType {
        denied, restricted
    }

    private AccessType access;
    private String requiredPermission;
    private String permission;
    private Set<String> apis = Collections.emptySet();
    private Set<String> workspaces = Collections.emptySet();
    private Set<Pattern> pathPatterns = Collections.emptySet();
    private Set<String> nodeTypes = Collections.emptySet();
    private Set<String> scopes = Collections.emptySet();
    private Set<String> requiredScopes = Collections.emptySet();
    private int priority = 0;

    public AccessType getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access =  (access != null) ? AccessType.valueOf(access.toLowerCase()) : null;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Set<String> getApis() {
        return apis;
    }

    public void setApis(Set<String> apis) {
        this.apis = (apis != null && !apis.isEmpty()) ? apis : Collections.<String>emptySet();
    }

    public Set<String> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(Set<String> workspaces) {
        this.workspaces = (workspaces != null && !workspaces.isEmpty()) ? workspaces : Collections.<String>emptySet();
    }

    public Set<Pattern> getPathPatterns() {
        return pathPatterns;
    }

    public void setPathPatterns(Set<Pattern> pathPatterns) {
        this.pathPatterns = (pathPatterns != null && !pathPatterns.isEmpty()) ? pathPatterns : Collections.<Pattern>emptySet();
    }

    public Set<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(Set<String> nodeTypes) {
        this.nodeTypes = (nodeTypes != null && !nodeTypes.isEmpty()) ? nodeTypes : Collections.<String>emptySet();
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Set<String> getRequiredScopes() {
        return requiredScopes;
    }

    public void setRequiredScopes(Set<String> requiredScopes) {
        this.requiredScopes = requiredScopes;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "access=" + access +
                ", requiredPermission='" + requiredPermission + '\'' +
                ", permission='" + permission + '\'' +
                ", apis=" + apis +
                ", workspaces=" + workspaces +
                ", pathPatterns=" + pathPatterns +
                ", nodeTypes=" + nodeTypes +
                ", scopes=" + scopes +
                ", requiredScopes=" + requiredScopes +
                ", priority=" + priority +
                '}';
    }
}
