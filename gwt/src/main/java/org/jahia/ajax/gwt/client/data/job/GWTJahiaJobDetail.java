/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.job;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.util.EventDataSupplier;

/**
 *
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:13:35 PM
 *
 */
public class GWTJahiaJobDetail extends BaseModelData implements Serializable, Comparable<GWTJahiaJobDetail>, EventDataSupplier {

    public GWTJahiaJobDetail() {
    }

    public GWTJahiaJobDetail(String name,
                             Date creationTime,
                             String user,
                             String site,
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
        setSite(site);
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

    public String getLabelKey() {
        return get("labelkey");
    }

    public void setLabelKey(String label) {
        set("labelkey", label);
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

    public String getSite() {
        return get("site");
    }

    public void setSite(String site) {
        set("site", site);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        GWTJahiaJobDetail that = (GWTJahiaJobDetail) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
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

    @Override
    public Map<String, Object> getEventData() {
        return getProperties();
    }
}
