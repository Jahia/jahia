/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
