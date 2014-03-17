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

import java.io.Serializable;
import java.util.Map;

public class PermissionBean implements Serializable, Comparable<PermissionBean> {
    private String uuid;
    private String parentPath;
    private String name;
    private String title;
    private String description;
    private String module;
    private String path;
    private String targetPath;
    private Map<String, PermissionBean> mappedPermissions;
    private boolean mappedPermissionsExpanded;
    private boolean partialSet;
    private boolean set;
    private boolean superSet;
    private boolean hasChildren;
    private int depth;
    private String scope;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public Map<String, PermissionBean> getMappedPermissions() {
        return mappedPermissions;
    }

    public void setMappedPermissions(Map<String, PermissionBean> mappedPermissions) {
        this.mappedPermissions = mappedPermissions;
    }

    public boolean isMappedPermissionsExpanded() {
        return mappedPermissionsExpanded;
    }

    public void setMappedPermissionsExpanded(boolean mappedPermissionsExpanded) {
        this.mappedPermissionsExpanded = mappedPermissionsExpanded;
    }

    public boolean isPartialSet() {
        return partialSet;
    }

    public void setPartialSet(boolean partialSet) {
        this.partialSet = partialSet;
    }

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public boolean isSuperSet() {
        return superSet;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public void setSuperSet(boolean superSet) {
        this.set = false;
        this.partialSet = false;
        this.superSet = superSet;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String  scope) {
        this.scope = scope;
    }

    @Override
    public int compareTo(PermissionBean o) {
        if (path.compareTo(o.getPath()) != 0) {
            return path.compareTo(o.getPath());
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionBean that = (PermissionBean) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return result;
    }
}
