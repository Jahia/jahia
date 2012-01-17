/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.params;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.EnumerationIterator;

/**
 * 
 * User: loom
 * Date: Mar 6, 2005
 * Time: 3:59:01 PM
 * 
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
