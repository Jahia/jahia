/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
