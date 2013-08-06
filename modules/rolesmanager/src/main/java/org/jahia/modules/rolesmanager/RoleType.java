package org.jahia.modules.rolesmanager;

import java.io.Serializable;
import java.util.Set;

public class RoleType implements Serializable {
    private String name;
    private String nodeType;
    private boolean isPrivileged;

    private Set<String> scopes;

    public RoleType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isPrivileged() {
        return isPrivileged;
    }

    public void setPrivileged(boolean privileged) {
        isPrivileged = privileged;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
