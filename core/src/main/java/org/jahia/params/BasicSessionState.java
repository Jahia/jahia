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
 package org.jahia.params;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 6, 2005
 * Time: 3:53:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicSessionState implements SessionState, Serializable {

    private String id;
    private Map attributes = new HashMap();
    private int maxInactiveInterval = 30 * 60;

    public BasicSessionState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Iterator getAttributeNames() {
        return new ArrayList(attributes.keySet()).iterator();
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void removeAllAtttributes() {
        attributes.clear();
    }

    public String getID() {
        return id;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }
}
