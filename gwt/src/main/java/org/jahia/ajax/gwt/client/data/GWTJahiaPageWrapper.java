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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 dec. 2007
 */
public class GWTJahiaPageWrapper extends BaseModelData implements Serializable {

    public GWTJahiaPageWrapper() {
        super();
        set("link", "#");
        setSiteRoot(false);
    }

    public boolean isSiteRoot() {
        return getSiteRoot().booleanValue();
    }

    public Boolean getSiteRoot() {
        return get("siteRoot");
    }

    public void setSiteRoot(boolean isSite) {
        set("siteRoot", Boolean.valueOf(isSite));
    }

    public int getPid() {
        return getPageId().intValue();
    }

    private Integer getPageId() {
        return get("pid");
    }

    public int getParentPid() {
        return getParentPageId().intValue();
    }

    public Integer getParentPageId() {
        return get("parentpid");
    }

    public void setPid(int pid) {
        set("pid", Integer.valueOf(pid));
    }

    public void setParentPid(int parentPid) {
        set("parentpid", Integer.valueOf(parentPid));
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String title) {
        set("title", title);
    }

    public boolean hasChildren() {
        return hasChildPages().booleanValue();
    }

    private Boolean hasChildPages() {
        return get("hasChildren");
    }

    public void setHasChildren(boolean hasChildren) {
        set("hasChildren", Boolean.valueOf(hasChildren));
    }

    public String getLink() {
        return get("link");
    }

    public void setLink(String link) {
        set("link", link);
    }

    public boolean isLocked() {
        return (Boolean) get("locked");
    }

    public void setLocked(boolean locked) {
        set("locked", Boolean.valueOf(locked));
    }

    public void setSubpages(List<GWTJahiaPageWrapper> pages) {
        set("subpages", pages);
    }

    public List<GWTJahiaPageWrapper> getSubpages() {
        return get("subpages");
    }

    public void setHasLive(Boolean i) {
        set("hasLive", i);
    }

    public Boolean isHasLive() {
        return get("hasLive");
    }

    public void setWorkflowStatus(String i) {
        set("workflowStatus", i);
    }

    public String getWorkflowStatus() {
        return get("workflowStatus");
    }

}
