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

import org.jahia.jaas.JahiaLoginModule;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 janv. 2008
 * Time: 19:55:10
 * To change this template use File | Settings | File Templates.
 */
public class User {
    private String username;
    private JahiaUser user;

    public User(JahiaUser user) {
        this.user = user;
    }

    public User(String username) {
        this.username = username;
    }

    public String getName() {
        if (username != null) {
            return username;
        }
        return user.getUsername();
    }

    public List<UserProperty> getProperties() {
        List<UserProperty> r = new ArrayList<UserProperty>();
        JahiaUser jahiaUser = getJahiaUser();
        if (jahiaUser != null) {
            Properties p = jahiaUser.getProperties();
            for (Iterator<?> iterator = p.keySet().iterator(); iterator.hasNext();) {
                String s = (String) iterator.next();
                String v = p.getProperty(s);
                r.add(new UserProperty(this, s,v));
            }
        }
        return r;
    }

    public List<Group> getGroups() {
        List<Group> r = new ArrayList<Group>();
        JahiaGroupManagerService grpManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaUser jahiaUser = getJahiaUser();
        if (jahiaUser != null) {
            List<String> groups = grpManager.getUserMembership(jahiaUser);
            for (String groupname : groups) {
                JahiaGroup group = grpManager.lookupGroup(groupname);
                r.add(new Group(group));
            }
        }
        return r;
    }

    public JahiaUser getJahiaUser() {
        if (user == null) {
            if (!username.equals(JahiaLoginModule.SYSTEM) && !username.equals(JahiaLoginModule.GUEST)) {
                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);
            }
        }
        return user;
    }

}
