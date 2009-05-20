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
package org.jahia.services.content.automation;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 janv. 2008
 * Time: 19:57:37
 * To change this template use File | Settings | File Templates.
 */
public class Group {
    private JahiaGroup group;

    public Group(JahiaGroup group) {
        this.group = group;
    }

    public String getName() {
        return group.getGroupname();
    }

    public List<User> getUsers() {
        List<User> r = new ArrayList<User>();
        Enumeration<Principal> en = group.members();
        while (en.hasMoreElements()) {
            Principal p = en.nextElement();
            if (p instanceof JahiaUser) {
                JahiaUser user = (JahiaUser)p;
                r.add(new User(user));
            } else {
                JahiaGroup group = (JahiaGroup)p;
            }

        }
        return r;
    }
}
