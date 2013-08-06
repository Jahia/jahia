package org.jahia.modules.rolesmanager;

import java.io.Serializable;
import java.util.Map;

public class RoleBean implements Serializable {

    private String uuid;

    private String name;

    private RoleType roleType;

    private int depth;

    private Map<String, Map<String,PermissionBean>> permissions;

//    private Map<String, List<PermissionBean>> externalPermissions;



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType scope) {
        this.roleType = scope;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Map<String, Map<String,PermissionBean>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Map<String,PermissionBean>> permissions) {
        this.permissions = permissions;
    }

//    public Map<String, List<PermissionBean>> getExternalPermissions() {
//        return externalPermissions;
//    }
//
//    public void setExternalPermissions(Map<String, List<PermissionBean>> externalPermissions) {
//        this.externalPermissions = externalPermissions;
//    }
}
