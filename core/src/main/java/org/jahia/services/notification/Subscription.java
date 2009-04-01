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

package org.jahia.services.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * User subscription object.
 * 
 * @author Sergiy Shyrkov
 */
public class Subscription implements Serializable, Cloneable {

    public enum Channel {
        EMAIL;

        public static Channel valueOf(char channel) {
            switch (channel) {
            case 'E':
                return EMAIL;
            default:
                throw new IllegalArgumentException("Unsupported channel type '"
                        + channel + "'");
            }
        }

        public char toChar() {
            return this.toString().charAt(0);
        }
    }

    public enum Type {
        IMMEDIATE;

        public static Type valueOf(char type) {
            switch (type) {
            case 'I':
                return IMMEDIATE;
            default:
                throw new IllegalArgumentException(
                        "Unsupported subscription type '" + type + "'");
            }
        }

        public char toChar() {
            return this.toString().charAt(0);
        }
    }

    private static final Channel DEFAULT_CHANNEL = Channel.EMAIL;

    private static final Type DEFAULT_TYPE = Type.IMMEDIATE;

    private Channel channel = DEFAULT_CHANNEL;

    private String confirmationKey;

    private long confirmationRequestTimestamp;

    private boolean enabled;

    private String eventType;

    private int id;

    private boolean includeChildren;

    private String objectKey;

    private Map<String, String> properties;

    private int siteId;

    private boolean suspended;

    private Type type = DEFAULT_TYPE;

    private String username;

    private boolean userRegistered;

    /**
     * Initializes an instance of this class.
     */
    public Subscription() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     */
    public Subscription(int id) {
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
     * @param userRegistered
     * @param siteId
     * @param channel
     * @param type
     * @param enabled
     * @param suspended
     * @param confirmationKey
     * @param properties
     */
    public Subscription(int id, String objectKey, boolean includeChildren,
            String eventType, String username, boolean userRegistered,
            int siteId, Channel channel, Type type, boolean enabled,
            boolean suspended, String confirmationKey,
            Map<String, String> properties) {
        this();
        this.id = id;
        this.objectKey = objectKey;
        this.includeChildren = includeChildren;
        this.eventType = eventType;
        this.username = username;
        this.userRegistered = userRegistered;
        this.siteId = siteId;
        this.channel = channel;
        this.type = type;
        this.enabled = enabled;
        this.suspended = suspended;
        this.confirmationKey = confirmationKey;
        this.properties = properties != null ? properties
                : new HashMap<String, String>();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param objectKey
     * @param includeChildren
     * @param eventType
     * @param username
     * @param siteId
     */
    public Subscription(String objectKey, boolean includeChildren,
            String eventType, String username, int siteId) {
        this(objectKey, includeChildren, eventType, username, siteId, true);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param objectKey
     * @param includeChildren
     * @param eventType
     * @param username
     * @param siteId
     * @param enabled
     */
    public Subscription(String objectKey, boolean includeChildren,
            String eventType, String username, int siteId, boolean enabled) {
        this(0, objectKey, includeChildren, eventType, username, true, siteId,
                DEFAULT_CHANNEL, DEFAULT_TYPE, enabled, false, null, null);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            Subscription castOther = (Subscription) obj;
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                    .isEquals();
        }
        return false;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getConfirmationKey() {
        return confirmationKey;
    }

    /**
     * Returns the confirmation request timestamp, i.e. the date, when the
     * confirmation e-mail was sent to the subscriber
     * 
     * @return the confirmation request timestamp, i.e. the date, when the
     *         confirmation e-mail was sent to the subscriber
     */
    public long getConfirmationRequestTimestamp() {
        return confirmationRequestTimestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public int getId() {
        return id;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public int getSiteId() {
        return siteId;
    }

    public Type getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Returns <code>true</code> if the subscription user is a registered Jahia
     * user.
     * 
     * @return <code>true</code> if the subscription user is a registered Jahia
     *         user
     */
    public boolean isUserRegistered() {
        return userRegistered;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }

    public void setConfirmationRequestTimestamp(
            long confirmationRequestTimestamp) {
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

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserRegistered(boolean userRegistered) {
        this.userRegistered = userRegistered;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
