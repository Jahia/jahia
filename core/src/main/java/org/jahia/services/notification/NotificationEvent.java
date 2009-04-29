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
package org.jahia.services.notification;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.data.events.JahiaEvent;

/**
 * Notification data object.
 * 
 * @author Sergiy Shyrkov
 */
public class NotificationEvent extends JahiaEvent {

    private String author;

    private String context;

    private String description;

    private String eventType;

    private Map<String, Object> extraInfo;

    private int id;

    private String language;

    private String objectKey;

    private String objectPath;

    private int objectType;

    private int pageId;

    private String pageTitle;

    private String pageUrl;

    private int siteId;

    private String siteTitle;

    private Set<Principal> subscribers;

    private String title;

    private String url;

    /**
     * Initializes an instance of this class.
     * 
     * @param objectKey
     *            the source of the event
     * @param eventType
     *            the type of the event
     */
    public NotificationEvent(String objectKey, String eventType) {
        super(objectKey, null, null);
        this.objectKey = objectKey;
        this.eventType = eventType;
        extraInfo = new HashMap<String, Object>(1);
        subscribers = new HashSet<Principal>();
    }

    public boolean containsExtraInfo(String key) {
        return extraInfo.containsKey(key);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            NotificationEvent castOther = (NotificationEvent) obj;
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                    .isEquals();
        }
        return false;
    }

    public String getAuthor() {
        return author;
    }

    public String getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getEventType() {
        return eventType;
    }

    public Object getExtraInfo(String key) {
        return extraInfo.get(key);
    }

    public int getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public int getObjectType() {
        return objectType;
    }

    public int getPageId() {
        return pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public int getSiteId() {
        return siteId;
    }

    public String getSiteTitle() {
        return siteTitle;
    }

    public Set<Principal> getSubscribers() {
        return subscribers;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object setExtraInfo(String key, Object info) {
        return extraInfo.put(key, info);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
