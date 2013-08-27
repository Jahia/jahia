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
