/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
 */

package org.jahia.modules.rolesmanager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoleType implements Serializable {
    private String name;
    private List<String> defaultNodeTypes;
    private List<String> availableNodeTypes;
    private boolean isPrivileged;

    private Set<String> scopes;

    private Map<String,List<String>> permissionsGroups;



    public RoleType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivileged() {
        return isPrivileged;
    }

    public void setPrivileged(boolean privileged) {
        isPrivileged = privileged;
    }

    public List<String> getDefaultNodeTypes() {
        return defaultNodeTypes;
    }

    public void setDefaultNodeTypes(List<String> defaultNodeTypes) {
        this.defaultNodeTypes = defaultNodeTypes;
    }

    public List<String> getAvailableNodeTypes() {
        return availableNodeTypes;
    }

    public void setAvailableNodeTypes(List<String> availableNodeTypes) {
        this.availableNodeTypes = availableNodeTypes;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Map<String, List<String>> getPermissionsGroups() {
        return permissionsGroups;
    }

    public void setPermissionsGroups(Map<String, List<String>> permissionsGroups) {
        this.permissionsGroups = permissionsGroups;
    }

}
