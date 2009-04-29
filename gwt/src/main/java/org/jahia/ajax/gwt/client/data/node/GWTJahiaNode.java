/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
public class GWTJahiaNode extends BaseTreeModel<GWTJahiaNode> implements Serializable, Comparable<GWTJahiaNode>, ListLoadConfig {

    private boolean displayable = false;
    private String url;
    private boolean hasChildren = false;
    private boolean hasFolderChildren = false;
    private String normalizedName = null;
    private int width = 0 ;
    private int height = 0 ;
    private SortInfo sortInfo = new SortInfo("name", Style.SortDir.ASC);

    public GWTJahiaNode() {
        super();
        setFile(Boolean.FALSE);
    }

    public GWTJahiaNode(String uuid, String name, String description, String path, String url, Date date, List<String> nodetypes, List<String> inheritedTypes, String aclContext, boolean writeable, boolean lockable, boolean locked, String lockOwner) {
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
        setFile(Boolean.FALSE);
        setWriteable(writeable);
        setLockable(lockable);
        setExt("icon-dir");
        setLocked(locked);
        setLockOwner(lockOwner);
        setThumbnailsMap(new HashMap<String, String>());
    }

    public GWTJahiaNode(String uuid, String name, String description, String path, String url, Date date, List<String> nodetypes, List<String> inheritedTypes, String aclContext, Long size, String ext, boolean writeable, boolean lockable, boolean locked, String lockOwner) {
        this(uuid, name,description, path, url, date, nodetypes, inheritedTypes, aclContext, writeable, lockable, locked, lockOwner);
        setSize(size);
        setFile(Boolean.TRUE);
        setExt(ext);
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

    public SortInfo getSortInfo() {
        return sortInfo;
    }

    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo ;
    }
}
