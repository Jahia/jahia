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

 package org.jahia.soap;

import org.apache.log4j.Logger;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.registries.ServicesRegistry;

import java.util.*;

/**
 *
 *
 * Created by IntelliJ IDEA.
 * Date: Dec 1, 2003
 * Copyright Codeva 2003
 * @author Thomas Draier
 */
public class UserManager {
    
    private static final transient Logger logger = Logger
            .getLogger(UserManager.class);
    
    public boolean authenticate(String userKey, String password) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
        if (jahiaUser == null) {
            return false;
        }
        return jahiaUser.verifyPassword(password);
    }

    public String[] getUsers(int siteId) {
        List v = ServicesRegistry.getInstance().getJahiaUserManagerService().getUsernameList();
        String[] r = new String[v.size()];
        v.toArray(r);
        return r;
    }

    public String[] getGroups(int siteId) {
        List v = ServicesRegistry.getInstance().getJahiaGroupManagerService().getGroupnameList(siteId);
        String[] r = new String[v.size()];
        v.toArray(r);
        return r;
    }

    public String[] getMembersWithKey(String groupKey) {
        JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup g = gm.lookupGroup(groupKey);
        Set s = g.getRecursiveUserMembers();
        String[] res = new String[s.size()];
        int i = 0;
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            JahiaUser jahiaUser = (JahiaUser) iterator.next();
            res[i++] = jahiaUser.getUserKey();
        }
        return res;
    }

    public String[] getMembers(String groupName, int siteId) {
        JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup g = gm.lookupGroup(siteId, groupName);
        Set s = g.getRecursiveUserMembers();
        String[] res = new String[s.size()];
        int i = 0;
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            JahiaUser jahiaUser = (JahiaUser) iterator.next();
            res[i++] = jahiaUser.getUsername();
        }
        return res;
    }


    public String[] getMembershipWithKey(String userKey) {
        String[] res = new java.lang.String[0];
        try {
            JahiaUserManagerService um = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser u = um.lookupUserByKey(userKey);
            JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            List v = gm.getUserMembership(u);
            res = new String[v.size()];
            v.toArray(res);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    public String[] getMembership(String userName, int siteId) {
        String[] res = new java.lang.String[0];
        try {
            JahiaUserManagerService um = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser u = um.lookupUser(userName);
            if (u ==null) {
                u = um.lookupUser(userName);
            }
            JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            List v = gm.getUserMembership(u);
            res = new String[v.size()];
            int i = 0;
            for (Iterator iterator = v.iterator(); iterator.hasNext();) {
                String gkey = (String) iterator.next();
                JahiaGroup g = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(gkey);
                res[i++] = g.getGroupname();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    public boolean isMember(String userName, String groupName, int siteId) {
        boolean res = false;
        try {
            JahiaUserManagerService um = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser u = um.lookupUser(userName);
            if (u ==null) {
                u = um.lookupUser(userName);
            }
            JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            JahiaGroup g = gm.lookupGroup(siteId, groupName);
            if (g ==null) {
                g = gm.lookupGroup(0, groupName);
            }
            res = g.isMember(u);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    public Map getPropertiesWithKey(String userKey ) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
        return jahiaUser.getProperties();
    }

    public Map getProperties(String userName, int siteId ) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userName);
        if (jahiaUser ==null) {
            jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userName);
        }
        return jahiaUser.getProperties();
    }

    public Map getMembersPropertiesWithKey(String groupKey) {
        JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup g = gm.lookupGroup(groupKey);
        Set s = g.getRecursiveUserMembers();
        Map res = new HashMap();

        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            JahiaUser jahiaUser = (JahiaUser) iterator.next();
            res.put(jahiaUser.getUserKey(),jahiaUser.getProperties());
        }
        return res;
    }

    public Map getMembersProperties(String groupName,int siteId) {
        JahiaGroupManagerService gm = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup g = gm.lookupGroup(siteId, groupName);
        Set s = g.getRecursiveUserMembers();
        Map res = new HashMap();

        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            JahiaUser jahiaUser = (JahiaUser) iterator.next();
            res.put(jahiaUser.getUserKey(),jahiaUser.getProperties());
        }
        return res;
    }


    public boolean checkPassword(String userName, String password, int siteId) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userName);
        if (jahiaUser ==null) {
            jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userName);
        }
        return jahiaUser.verifyPassword(password);
    }
    

}
