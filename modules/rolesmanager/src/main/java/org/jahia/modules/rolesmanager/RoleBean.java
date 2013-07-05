package org.jahia.modules.rolesmanager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RoleBean implements Serializable {

    private String uuid;

    private String name;

    private RolesAndPermissionsHandler.Scope scope;

    private boolean isPrivileged;

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

    public RolesAndPermissionsHandler.Scope  getScope() {
        return scope;
    }

    public void setScope(RolesAndPermissionsHandler.Scope scope) {
        this.scope = scope;
    }

    public boolean isPrivileged() {
        return isPrivileged;
    }

    public void setPrivileged(boolean privileged) {
        isPrivileged = privileged;
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
