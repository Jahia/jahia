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
