package org.jahia.ajax.gwt.client.data.job;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 17, 2010
 * Time: 2:13:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaJobDetail extends BaseModelData implements Serializable, Comparable {

    public GWTJahiaJobDetail() {
    }

    public GWTJahiaJobDetail(String name,
                             Date creationTime,
                             String user,
                             String description,
                             String status,
                             String message,
                             List<String> relatedPaths,
                             String group,
                             String jobClassName,
                             Long beginTime,
                             Long endTime,
                             Integer durationInSeconds,
                             String locale,
                             String fileName) {
        setName(name);
        setCreationTime(creationTime);
        setUser(user);
        setDescription(description);
        setStatus(status);
        setMessage(message);
        setRelatedPaths(relatedPaths);
        setGroup(group);
        setJobClassName(jobClassName);
        setBeginTime(beginTime);
        setEndTime(endTime);
        setDurationInSeconds(durationInSeconds);
        setLocale(locale);
        setFileName(fileName);
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

    public List<String> getRelatedPaths() {
        return get("relatedPaths");
    }

    public void setRelatedPaths(List<String> relatedPaths) {
        set("relatedPaths", relatedPaths);
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

    public Integer getDurationInSeconds() {
        return get("durationInSeconds");
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        set("durationInSeconds", durationInSeconds);
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

    public int compareTo(Object o) {
        return getCreationTime().compareTo(((GWTJahiaJobDetail) o).getCreationTime());
    }
}
