/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.data.job;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:13:35 PM
 * 
 */
public class GWTJahiaJobDetail extends BaseModelData implements Serializable, Comparable<GWTJahiaJobDetail> {

    public GWTJahiaJobDetail() {
    }

    public GWTJahiaJobDetail(String name,
                             Date creationTime,
                             String user,
                             String description,
                             String status,
                             String message,
                             List<String> targetPaths,
                             String group,
                             String jobClassName,
                             Long beginTime,
                             Long endTime,
                             Long duration,
                             String locale,
                             String fileName,
                             String targetNodeIdentifier,
                             String targetAction,
                             String targetWorkspace) {
        setName(name);
        setCreationTime(creationTime);
        setUser(user);
        setDescription(description);
        setStatus(status);
        setMessage(message);
        setTargetPaths(targetPaths);
        setGroup(group);
        setJobClassName(jobClassName);
        setBeginTime(beginTime);
        setEndTime(endTime);
        setDuration(duration);
        setLocale(locale);
        setFileName(fileName);
        setTargetNodeIdentifier(targetNodeIdentifier);
        setTargetAction(targetAction);
        setTargetWorkspace(targetWorkspace);
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label", label);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public Date getCreationTime() {
        return get("creationTime");
    }

    public void setCreationTime(Date creationTime) {
        set("creationTime", creationTime);
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String description) {
        set("description", description);
    }

    public String getUser() {
        return get("user");
    }

    public void setUser(String user) {
        set("user", user);
    }

    public String getStatus() {
        return get("status");
    }

    public void setStatus(String status) {
        set("status", status);
    }

    public String getMessage() {
        return get("message");
    }

    public void setMessage(String message) {
        set("message", message);
    }

    public List<String> getTargetPaths() {
        return get("targetPaths");
    }

    public void setTargetPaths(List<String> targetPaths) {
        set("targetPaths", targetPaths);
    }

    public String getGroup() {
        return get("group");
    }

    public void setGroup(String group) {
        set("group", group);
    }

    public String getJobClassName() {
        return get("jobClassName");
    }

    public void setJobClassName(String jobClassName) {
        set("jobClassName", jobClassName);
    }

    public Long getBeginTime() {
        return get("beginTime");
    }

    public void setBeginTime(Long beginTime) {
        set("beginTime", beginTime);
    }

    public Long getEndTime() {
        return get("endTime");
    }

    public void setEndTime(Long endTime) {
        set("endTime", endTime);
    }

    public Long getDuration() {
        return get("duration");
    }

    public void setDuration(Long duration) {
        set("duration", duration);
    }

    public String getLocale() {
        return get("locale");
    }

    public void setLocale(String locale) {
        set("locale", locale);
    }

    public String getFileName() {
        return get("fileName");
    }

    public void setFileName(String fileName) {
        set("fileName", fileName);
    }

    public void setTargetNodeIdentifier(String targetNodeIdentifier) {
        set("targetNodeIdentifier", targetNodeIdentifier);
    }

    public String getTargetNodeIdentifier() {
        return get("targetNodeIdentifier");
    }

    public void setTargetAction(String targetAction) {
        set("targetAction", targetAction);
    }

    public String getTargetAction() {
        return get("targetAction");
    }

    public void setTargetWorkspace(String targetWorkspace) {
        set("targetWorkspace", targetWorkspace);
    }

    public String getTargetWorkspace() {
        return get("targetWorkspace");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GWTJahiaJobDetail)) {
            return false;
        }

        GWTJahiaJobDetail that = (GWTJahiaJobDetail) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public int compareTo(GWTJahiaJobDetail o) {
        if (getCreationTime() != null) {
            return getCreationTime().compareTo(o.getCreationTime());
        } else {
            if (getBeginTime() != null) {
                return getBeginTime().compareTo(o.getBeginTime());
            }
        }
        return 0;
    }
}
