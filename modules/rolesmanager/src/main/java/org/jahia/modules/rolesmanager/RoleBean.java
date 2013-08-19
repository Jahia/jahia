package org.jahia.modules.rolesmanager;

import java.io.Serializable;
import java.util.Map;

public class RoleBean implements Serializable {

    private String uuid;

    private String name;

    private String path;

    private String title = "";

    private String description = "";

    private boolean hidden = false;

    private RoleType roleType;

    private int depth;

    private boolean isDirty = false;

    private Map<String, Map<String, Map<String,PermissionBean>>> permissions;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
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

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public  Map<String, Map<String, Map<String,PermissionBean>>> getPermissions() {
        return permissions;
    }

    public void setPermissions( Map<String, Map<String, Map<String,PermissionBean>>> permissions) {
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
