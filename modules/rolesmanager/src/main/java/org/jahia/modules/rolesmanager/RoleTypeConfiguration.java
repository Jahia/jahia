package org.jahia.modules.rolesmanager;

import java.util.*;

public class RoleTypeConfiguration {
    private LinkedHashMap<String, RoleType> roleTypes = new LinkedHashMap<String, RoleType>();

    private Map<String,List<String>> permissionsGroups;

    public RoleTypeConfiguration() {
    }

    public void setRoleTypes(List<RoleType> roleTypesSet) {
        for (RoleType roleType : roleTypesSet) {
            roleTypes.put(roleType.getName(), roleType);
        }
    }

    public RoleType get(String name) {
        return roleTypes.get(name);
    }

    public Collection<RoleType> getValues() {
        return roleTypes.values();
    }


    public Map<String, List<String>> getPermissionsGroups() {
        return permissionsGroups;
    }

    public void setPermissionsGroups(Map<String, List<String>> permissionsGroups) {
        this.permissionsGroups = permissionsGroups;
    }
}
