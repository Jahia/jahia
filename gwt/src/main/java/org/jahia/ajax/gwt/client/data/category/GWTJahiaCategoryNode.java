/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
