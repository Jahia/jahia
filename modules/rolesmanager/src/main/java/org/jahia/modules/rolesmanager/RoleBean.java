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

import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RoleBean implements Serializable {

    private String uuid;

    private String name;

    private String path;

    private Map<String, I18nRoleProperties> i18nProperties;

    private boolean hidden = false;

    private RoleType roleType;

    private Collection<NodeType> nodeTypes;

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

    public Map<String, I18nRoleProperties> getI18nProperties() {
        return i18nProperties;
    }

    public void setI18nProperties(Map<String, I18nRoleProperties> i18nProperties) {
        this.i18nProperties = i18nProperties;
    }

    public String getTitle() {
        String language = LocaleContextHolder.getLocale().getLanguage();
        if (i18nProperties.containsKey(language) && i18nProperties.get(language) != null) {
            return i18nProperties.get(language).getTitle();
        } else {
            return "";
        }
    }

    public String getDescription() {
        String language = LocaleContextHolder.getLocale().getLanguage();
        if (i18nProperties.containsKey(language) && i18nProperties.get(language) != null) {
            return i18nProperties.get(language).getDescription();
        } else {
            return "";
        }
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

    public Collection<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(Collection<NodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
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

}
