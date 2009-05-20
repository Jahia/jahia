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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.EnumerationIterator;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 6, 2005
 * Time: 3:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpSessionState implements SessionState {

    private HttpSession httpSession;

    public HttpSessionState(final HttpSession session) {
        this.httpSession = session;
    }

    public String getId() {
        return httpSession.getId();
    }

    public Iterator getAttributeNames() {
        return new EnumerationIterator(httpSession.getAttributeNames());
    }

    public void setAttribute(final String name, final Object value) {
        httpSession.setAttribute(name, value);
    }

    public Object getAttribute(final String name) {
        return httpSession.getAttribute(name);
    }

    public void removeAttribute(final String name) {
        httpSession.removeAttribute(name);
    }

    public void removeAllAtttributes() {
        final Enumeration attributeNameEnum = httpSession.getAttributeNames();

        // first we copy the names as we cannot remove using an
        // enumeration
        final List namesToRemove = new ArrayList();
        while (attributeNameEnum.hasMoreElements()) {
            String curAttrName = (String) attributeNameEnum.nextElement();
            namesToRemove.add(curAttrName);
        }
        final Iterator namesToRemoveIter = namesToRemove.iterator();
        while (namesToRemoveIter.hasNext()) {
            String curAttrName = (String) namesToRemoveIter.next();
            if (!"org.apache.jetspeed.container.session.PortalSessionMonitor".equals(curAttrName)) {
                httpSession.removeAttribute(curAttrName);
            }    
        }
    }

    public String getID() {
        return httpSession.getId();
    }

    public int getMaxInactiveInterval() {
        return httpSession.getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(final int interval) {
        httpSession.setMaxInactiveInterval(interval);
    }
}
