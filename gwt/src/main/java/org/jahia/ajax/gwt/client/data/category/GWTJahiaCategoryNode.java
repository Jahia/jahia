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
package org.jahia.ajax.gwt.client.data.category;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:38
 */
public class GWTJahiaCategoryNode extends BaseTreeModel implements Serializable {
    private List<GWTJahiaCategoryTitle> categoryTitles;

    public GWTJahiaCategoryNode() {
        super();
        setCategoryProperties(new ArrayList<GWTJahiaNodeProperty>());
        setCategoryTitles(new ArrayList<GWTJahiaCategoryTitle>());
        setLeaf(Boolean.valueOf(false));
    }

    public GWTJahiaCategoryNode(String key, String name, String path, Date date) {
        this();
        setKey(key);
        setName(name);
        setPath(path);
        setDate(date);
    }


    public void setCategoryProperties(List<GWTJahiaNodeProperty> list) {
        set("properties", list);
    }


    public List<GWTJahiaNodeProperty> getCategoryProperties() {
        return get("properties");
    }

    public List<GWTJahiaCategoryTitle> getCategoryTitles() {
        return categoryTitles;
    }

    public void setCategoryTitles(List<GWTJahiaCategoryTitle> categoryTitles) {
        this.categoryTitles = categoryTitles;
    }

    public String getName() {
        return get("name");
    }

    public void setACLLink(String aclLink) {
        set("aclLink", aclLink);
    }

    public String getACLLink() {
        return get("aclLink");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getLog() {
        return get("log");
    }

    public void setLog(String log) {
        set("name", log);
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path);
    }

    public void setKey(String key) {
        set("key", key);
    }

    public String getKey() {
        return get("key");
    }

    public void setParentKey(String key) {
        set("parentKey", key);
    }

    public String getParentKey() {
        return get("parentKey");
    }


    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public void setLeaf(Boolean leaf) {
        set("leaf", leaf);
    }

    public boolean isLeaf() {
        return false;
    }

    public void setWriteable(Boolean writeable) {
        set("writeable", writeable);
    }

    public Boolean isWriteable() {
        return get("writeable");
    }

    public void setRead(Boolean writeable) {
        set("readable", writeable);
    }

    public Boolean isReadable() {
        return get("readable");
    }

    public void setAdmin(Boolean writeable) {
        set("admin", writeable);
    }

    public Boolean isAdmin() {
        return get("admin");
    }

    public String getCategoryId() {
        return get("categoryId");
    }

    public void setCategoryId(String id) {
        set("categoryId", id);
    }

    public String toString() {
        return getName();
    }

    public void addCategoryProperties(GWTJahiaNodeProperty property) {
        getCategoryProperties().add(property);
    }

    public void removeCategoryProperties(GWTJahiaNodeProperty property) {
        getCategoryProperties().remove(property);
    }

    public boolean equals(GWTJahiaCategoryNode other) {
        return other != null && getCategoryId().equals(other.getCategoryId());
    }


}
