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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.hibernate.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * User subscription data object.
 * 
 * @hibernate.class table="jahia_subscriptions"
 * @hibernate.cache usage="nonstrict-read-write"
 * @author Sergiy Shyrkov
 */
public class SubscriptionData implements Serializable {

    private char channel;

    private String confirmationKey;

    private long confirmationRequestTimestamp;

    private boolean enabled;
    
    private String eventType;    

    private int id;

    private boolean includeChildren;

    private String objectKey;

    private int siteId;

    private boolean suspended;

    private char type;

    private String username;

    /**
     * Initializes an instance of this class.
     */
    public SubscriptionData() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     */
    public SubscriptionData(int id) {
        this();
        this.id = id;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     * @param objectKey
     * @param includeChildren
     * @param eventType
     * @param username
     * @param siteId
     * @param channel
     * @param type
     * @param enabled
     * @param suspended
     */
    public SubscriptionData(int id, String objectKey, boolean includeChildren,
            String eventType, String username, int siteId, char channel,
            char type, boolean enabled, boolean suspended) {
        this(id);
        this.objectKey = objectKey;
        this.includeChildren = includeChildren;
        this.eventType = eventType;
        this.username = username;
        this.siteId = siteId;
        this.channel = channel;
        this.type = type;
        this.enabled = enabled;
        this.suspended = suspended;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            SubscriptionData castOther = (SubscriptionData) obj;
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                    .isEquals();
        }
        return false;
    }

    /**
     * Returns the notification channel type
     * 
     * @hibernate.property column="channel" not-null="true" type="character"
     * @return the notification channel type
     */
    public char getChannel() {
        return channel;
    }

    /**
     * Returns the confirmation key.
     * 
     * @hibernate.property column="confirmation_key" not-null="false"
     *                     type="string" length="32"
     * @return the confirmation key
     */
    public String getConfirmationKey() {
        return confirmationKey;
    }

    /**
     * Returns the confirmation request timestamp, i.e. the date, when the confirmation e-mail was sent to the subscriber.
     * 
     * @hibernate.property column="confirmation_request_timestamp" not-null="false" type="long"
     * @return the confirmation request timestamp, i.e. the date, when the confirmation e-mail was sent to the subscriber
     */
    public long getConfirmationRequestTimestamp() {
        return confirmationRequestTimestamp;
    }

    /**
     * Returns the event type
     * 
     * @hibernate.property column="event_type" not-null="true" type="string"
     *                     length="50"
     * @return the type of the event
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the subscription ID.
     * 
     * @hibernate.id column="id_jahia_subscriptions" type="int"
     *               generator-class="org.jahia.hibernate.dao.JahiaIdentifierGenerator"
     *               unsaved-value="0"
     * @return the subscription ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the object key.
     * 
     * @hibernate.property column="object_key" not-null="false" type="string"
     *                     length="40"
     * @return the object key
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Returns the site ID.
     * 
     * @hibernate.property column="site_id" not-null="true" type="int"
     * @return the site ID
     */
    public int getSiteId() {
        return siteId;
    }

    /**
     * Returns the notification type
     * 
     * @hibernate.property column="notification_type" not-null="true"
     *                     type="character"
     * @return the notification type
     */
    public char getType() {
        return type;
    }

    /**
     * Returns the username.
     * 
     * @hibernate.property column="username" not-null="true" type="string"
     *                     length="255"
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    /**
     * Returns <code>true</code> if this subscription is enabled.
     * 
     * @hibernate.property column="enabled" not-null="true" type="boolean"
     * @return <code>true</code> if this subscription is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns <code>true</code> if the event on the child objects should be
     * also captured.
     * 
     * @hibernate.property column="include_children" not-null="true"
     *                     type="boolean"
     * @return <code>true</code> if the event on the child objects should be
     *         also captured
     */
    public boolean isIncludeChildren() {
        return includeChildren;
    }

    /**
     * Returns <code>true</code> if this subscription is (temporary) disabled.
     * 
     * @hibernate.property column="suspended" not-null="true" type="boolean"
     * @return <code>true</code> if this subscription is (temporary) disabled
     */
    public boolean isSuspended() {
        return suspended;
    }

    public void setChannel(char channel) {
        this.channel = channel;
    }

    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }

    public void setConfirmationRequestTimestamp(long confirmationRequestTimestamp) {
        this.confirmationRequestTimestamp = confirmationRequestTimestamp;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void setType(char type) {
        this.type = type;
    }

    public void setUsername(String userKey) {
        this.username = userKey;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
