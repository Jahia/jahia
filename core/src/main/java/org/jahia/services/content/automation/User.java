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

    public List getProperties() {
        List r = new ArrayList();
        JahiaUser jahiaUser = getJahiaUser();
        if (jahiaUser != null) {
            Properties p = jahiaUser.getProperties();
            for (Iterator iterator = p.keySet().iterator(); iterator.hasNext();) {
                String s = (String) iterator.next();
                String v = p.getProperty(s);
                r.add(new UserProperty(this, s,v));
            }
        }
        return r;
    }

    public List getGroups() {
        List r = new ArrayList();
        JahiaGroupManagerService grpManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaUser jahiaUser = getJahiaUser();
        if (jahiaUser != null) {
            List groups = grpManager.getUserMembership(jahiaUser);
            for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
                String groupname = (String) iterator.next();
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
