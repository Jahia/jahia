package org.jahia.modules.rolesmanager;

import java.util.*;

public class RoleTypeConfiguration {
    private LinkedHashMap<String, RoleType> roleTypes = new LinkedHashMap<String, RoleType>();

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
}
