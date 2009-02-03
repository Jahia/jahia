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


package org.jahia.services.cache.clusterservice;

import java.io.Serializable;

/**
 * This class encapsulates all the needed information that has to be sent to other
 * clustered jahia caches using the cluster service.
 *
 * @author  Fulco Houkes, Copyright (c) 2003 by Jahia Ltd.
 * @version 1.0
 * @since   Jahia 4.0
 */
public class ClusterCacheMessage implements Serializable {

    /** this type of message will instruct the cache to flush all it's entries. */
    final public static int FLUSH_EVENT = 1;

    /* This type of message indicates that we are flusing a group */
    final public static int FLUSHGROUP_EVENT = 2;

    /** this type of message instructs the cache a new (or updated) entry has been
     * added into the cache. */
    final public static int INVALIDATEENTRY_EVENT = 3;

    /** this type of message instructs the cache to remove the associated cache entry. */
    final public static int REMOVE_EVENT = 4;
    
    /** this type of message instructs the cache key generator to be restarted */
    final public static int KEYGENERATOR_RESTART_EVENT = 5;

    /**
     * this type of message instructs the cache key generator to check its cached groups for ACL changes
     */
    final public static int KEYGENERATOR_ACLUPDATE_EVENT = 6;    

    /** the cache name to which the message should be sent. */
    private String cacheName;

    /** the message type (flush, put, remove). */
    private int messageType;

    /** the cache entry key to which this message is associated. */
    private Object entryKey;

    private String groupName;

    /** Creates a new <code>JMSCacheMessage</code> instance.
     *
     * @param cacheName     the associated cache name
     * @param messageType   the message type
     * @param entryKey      the cache entry key associated with the message
     */
    public ClusterCacheMessage (final String cacheName, final int messageType,
                               final Object entryKey, final String groupName)
    {
        this.cacheName = cacheName;
        this.messageType = messageType;
        this.entryKey = entryKey;
        this.groupName = groupName;
    }

    final public boolean isFlush () {
        return messageType == FLUSH_EVENT;
    }

    final public boolean isFlushGroup() {
        return messageType == FLUSHGROUP_EVENT;
    }

    final public boolean isRemove () {
        return messageType == REMOVE_EVENT;
    }

    final public boolean isInvalidateEntry () {
        return messageType == INVALIDATEENTRY_EVENT;
    }
    

    final public boolean isKeyGeneratorRestart() {
        return messageType == KEYGENERATOR_RESTART_EVENT;
    }

    final public boolean isKeyGeneratorAclUpdate() {
        return messageType == KEYGENERATOR_ACLUPDATE_EVENT;
    }

    /** Retrieve the cache name to which this message should be sent.
     *
     * @return  the related cache name
     */
    final public String getCacheName () {
        return cacheName;
    }

    /** Retrieve the cache entry to to which this message is destinated.
     *
     * @return  the related cache entry key
     */
    final public Object getEntryKey () {
        return entryKey;
    }

    /** Retrieve the displayable form of this message type. Used for debugging purpose only.
     *
     * @return   the textual message type
     */
    final public String getEventTypeStr () {
        switch (messageType) {
            case FLUSH_EVENT:
                return "FLUSH";

            case FLUSHGROUP_EVENT:
                return "FLUSHGROUP";

            case REMOVE_EVENT:
                return "REMOVE";

            case INVALIDATEENTRY_EVENT:
                return "INVALIDATE_ENTRY";
                
            case KEYGENERATOR_RESTART_EVENT:
                return "KEYGENERATOR_RESTART_EVENT";

            case KEYGENERATOR_ACLUPDATE_EVENT:
                return "KEYGENERATOR_ACLUPDATE_EVENT";                
        }
        return "UNKNOWN"; // should never come to this point!
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /** Returns a string representation of this message. Used for debugging purpose only.
     *
     * @return  the message string representation
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer ("ClusterCacheMessage (cache:");
        buffer.append (cacheName);
        buffer.append (", event:");
        buffer.append (getEventTypeStr());
        buffer.append (", entryKey:");
        buffer.append ((entryKey == null ? "null": entryKey.toString()));
        buffer.append (", groupName:");
        buffer.append ((groupName == null ? "null": groupName.toString()));
        buffer.append (")");
        return buffer.toString();
    }
}

