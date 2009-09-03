/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.node;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:38
 */
public class GWTJahiaNode extends BaseTreeModel implements Serializable, Comparable<GWTJahiaNode>, ListLoadConfig {

    private boolean displayable = false;
    private String url;
    private boolean hasChildren = false;
    private boolean hasFolderChildren = false;
    private boolean portlet = false;
    private String normalizedName = null;
    private boolean versioned = false;
    private int width = 0 ;
    private int height = 0 ;
    private SortInfo sortInfo = new SortInfo("name", Style.SortDir.ASC);
    private List<GWTJahiaNodeVersion> versions;
    private String selectedVersion;
    private GWTJahiaNode referencedNode;

    public GWTJahiaNode() {
        super();
        setFile(Boolean.FALSE);
    }

    public GWTJahiaNode(String uuid, String name, String description, String path, String url, Date date, List<String> nodetypes, List<String> inheritedTypes, String aclContext, String providerKey, boolean writeable, boolean deleteable, boolean lockable, boolean locked, String lockOwner, boolean versioned) {
        super();
        setUUID(uuid);
        setName(name);
        setDisplayName(name);
        setDescription(description);
        setPath(path);
        setUrl(url);
        setDate(date);
        setNodeTypes(nodetypes);
        setInheritedNodeTypes(inheritedTypes);
        setAclContext(aclContext);
        setProviderKey(providerKey);
        setFile(Boolean.FALSE);
        setWriteable(writeable);
        setDeleteable(deleteable);
        setLockable(lockable);
        setExt("icon-dir");
        setLocked(locked);
        setLockOwner(lockOwner);
        setThumbnailsMap(new HashMap<String, String>());
        setVersioned(versioned);
    }

    public GWTJahiaNode(String uuid, String name, String description, String path, String url, Date date, List<String> nodetypes, List<String> inheritedTypes, String aclContext, String providerKey, Long size, boolean writeable, boolean deleteable, boolean lockable, boolean locked, String lockOwner, boolean versioned) {
        this(uuid, name,description, path, url, date, nodetypes, inheritedTypes, aclContext, providerKey, writeable, deleteable, lockable, locked, lockOwner,versioned);
        setSize(size);
        setFile(Boolean.TRUE);
        //setPreview("../images/types/gwt/large/icon-file.png");
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setHasFolderChildren(boolean hasChildren) {
        this.hasFolderChildren = hasChildren;
    }

    public boolean hasFolderChildren() {
        return hasFolderChildren;
    }

    public void setDisplayable(boolean disp) {
        displayable = disp;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setLockable(Boolean lockable) {
        set("lockable", lockable);
    }

    public Boolean isLockable() {
        return get("lockable");
    }

    public void setLocked(Boolean locked) {
        set("locked", locked);
    }

    public Boolean isLocked() {
        return get("locked");
    }

    public void setWriteable(Boolean writeable) {
        set("writeable", writeable);
    }

    public Boolean isWriteable() {
        return get("writeable");
    }

    public void setDeleteable(Boolean deleteable) {
        set("deleteable", deleteable);
    }

    public Boolean isDeleteable() {
        return get("deleteable");
    }

    public void setFileProperties(Map<String, String> map) {
        set("properties", map);
    }

    public Map<String, String> getFileProperties() {
        return get("properties");
    }

    public String getName() {
        return get("name");
    }

    public String getUUID() {
        return get("uuid");
    }

    public String getPath() {
        return get("path");
    }

    public void setName(String name) {
        set("name", name);
    }

    public void setDisplayName(String name) {
        set("displayName", name);
    }

    public String getDisplayName() {
        return get("displayName") ;
    }

    public void setUUID(String uuid) {
        set("uuid", uuid);
    }

    public void setPath(String path) {
        set("path", path);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public void setFile(Boolean file) {
        set("file", file);
    }

    public Boolean isFile() {
        return get("file");
    }

    public void setSize(Long size) {
        set("size", size);
    }

    public Long getSize() {
        return get("size");
    }

    public void setNodeTypes(List<String> nodeTypes) {
        set("nodeTypes", nodeTypes);
    }

    public List<String> getNodeTypes() {
        return get("nodeTypes");
    }

    public void setInheritedNodeTypes(List<String> nodeTypes) {
        set("inheritedNodeTypes", nodeTypes);
    }

    public String getAclContext() {
        return get("aclContext");
    }

    public void setAclContext(String aclContext) {
        set("aclContext", aclContext);
    }

    public List<String> getInheritedNodeTypes() {
        return get("inheritedNodeTypes");
    }

    public String getExt() {
        return get("ext");
    }

    public void setExt(String ext) {
        set("ext", ext);
    }

    public String getProviderKey() {
        return get("providerKey");
    }

    public void setProviderKey(String providerName) {
        set("providerKey", providerName);
    }

    public String getPreview() {
        return get("preview");
    }

    public void setPreview(String preview) {
        set("preview", preview);
    }

    public Map<String, String> getThumbnailsMap() {
        return get("thumbnails");
    }

    public void setThumbnailsMap(Map<String, String> preview) {
        set("thumbnails", preview);
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }


    public int getHeight() {
        return height ;
    }

    public void setHeight(int h) {
        height = h ;
    }

    public int getWidth() {
        return width ;
    }

    public void setWidth(int w) {
        width = w ;
    }

    public boolean isVersioned() {
        return versioned;
    }

    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(GWTJahiaNode other) {
        return getPath().equals(other.getPath());
    }

    public String getNormalizedName() {
        if (normalizedName == null) {
            return getName();
        } else {
            return normalizedName;
        }
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public int compareTo(GWTJahiaNode o) {
        if (isFile()) {
            if (o.isFile()) {
                return getNormalizedName().compareToIgnoreCase(o.getNormalizedName());
            } else {
                return -1;
            }
        } else {
            if (o.isFile()) {
                return 1;
            } else {
                return getNormalizedName().compareToIgnoreCase(o.getNormalizedName());
            }
        }

    }


    public String getLockOwner() {
        return get("lockOwner");
    }

    public void setLockOwner(String lockOwner) {
        set("lockOwner", lockOwner);
    }

    public Style.SortDir getSortDir() {
        return sortInfo.getSortDir();
    }

    public String getSortField() {
        return sortInfo.getSortField();
    }

    public SortInfo getSortInfo() {
        return sortInfo;
    }

    public void setSortDir(Style.SortDir sortDir) {
        sortInfo.setSortDir(sortDir);
    }

    public void setSortField(String s) {
        sortInfo.setSortField(s);
    }

    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo ;
    }

    public boolean isPortlet() {
        return portlet;
    }

    public void setPortlet(boolean portlet) {
        this.portlet = portlet;
    }

    public void setVersions(List<GWTJahiaNodeVersion> versions) {
        this.versions = versions;
    }

    public List<GWTJahiaNodeVersion> getVersions() {
        return versions;
    }

    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public String getSelectedVersion() {
        return selectedVersion;
    }

    public String getTemplate() {
        return get("template");
    }

    public void setTemplate(String template) {
       set("template",template);
    }

    public GWTJahiaNode getReferencedNode() {
        return referencedNode;
    }

    public void setReferencedNode(GWTJahiaNode referencedNode) {
        this.referencedNode = referencedNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaNode that = (GWTJahiaNode) o;

        if (getPath() != null ? !getPath().equals(that.getPath()) : that.getPath() != null) return false;

        return true;
    }
}
