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
 package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 mars 2006
 * Time: 16:44:20
 * To change this template use File | Settings | File Templates.
 */
public class UsersGroup extends JahiaDBGroup {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(UsersGroup.class);

    public UsersGroup(int id, int siteId, Properties properties) {
        super(id, JahiaGroupManagerDBProvider.USERS_GROUPNAME,
                JahiaGroupManagerDBProvider.USERS_GROUPNAME+":"+siteId, siteId, new ConcurrentHashMap<String, Principal>(), properties, false);
    }


    public Enumeration<Principal> members() {
        return new Vector<Principal>(getRecursiveUserMembers()).elements();
    }

    public Set<Principal> getRecursiveUserMembers() {
        Set<Principal> users = new HashSet<Principal> ();

        List<Principal> userList = null;
        try {
            userList = ServicesRegistry.getInstance().
                          getJahiaSiteUserManagerService().getMembers(
                getSiteID());
            JahiaUser guest = ServicesRegistry.getInstance().
                    getJahiaSiteUserManagerService().getMember(mSiteID,JahiaUserManagerDBProvider.GUEST_USERNAME);
            userList.remove(guest);
        } catch (JahiaException ex) {
            UsersGroup.logger.error("Error while trying to retrieve full user list for site " + getSiteID(), ex);
        }
        if (userList != null) {
            users.addAll(userList);
        }
        // should we list ldap users here ?
        return users;
    }

    public boolean addMember(Principal principal) {
        return false;
    }

    public boolean removeMember(Principal principal) {
        return false;
    }

    public boolean isMember(Principal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.getName().equals(JahiaUserManagerService.GUEST_USERNAME)) {
            return false;
        }

        if (!(principal instanceof JahiaDBUser)) {
            return true;
        } else {
            if (ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(mSiteID, principal.getName()) != null) {
                return true;
            }
        }

        return false;
    }
}
