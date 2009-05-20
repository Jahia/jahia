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

import java.util.Iterator;

/**
 * Session state interface. This is designed for storing data in a longer
 * scope than the ProcessingContext scope. This is an abstraction of a
 * HttpSessionState because we want to be able to use this data out of
 * a servlet context.
 */
public interface SessionState {

    public String getId();

    public Iterator getAttributeNames();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    public void removeAttribute(String name);

    public void removeAllAtttributes();

    public String getID();

    public int getMaxInactiveInterval();

    public void setMaxInactiveInterval(int interval);

}
