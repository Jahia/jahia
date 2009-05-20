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
package org.jahia.services.content.impl.jahia;

import javax.jcr.observation.Event;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 2, 2008
 * Time: 2:21:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventImpl implements Event {

    private int type;
    private String path;
    private String userID;

    public EventImpl(int type, String path, String userID) {
        this.type = type;
        this.path = path;
        this.userID = userID;
    }

    public int getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        return "EventImpl{" +
                "type=" + type +
                ", path='" + path + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventImpl event = (EventImpl) o;

        if (type != event.type) return false;
        if (path != null ? !path.equals(event.path) : event.path != null) return false;
        if (userID != null ? !userID.equals(event.userID) : event.userID != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = type;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (userID != null ? userID.hashCode() : 0);
        return result;
    }
}
