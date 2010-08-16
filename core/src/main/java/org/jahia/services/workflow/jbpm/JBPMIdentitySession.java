/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.rbac.Role;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Identity Manager for connecting jBPM to Jahia Users
 */
public class JBPMIdentitySession implements IdentitySession {
    protected JahiaGroupManagerService groupService;
    protected JahiaUserManagerService userService;

    public JBPMIdentitySession() {
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
    }

    public String createUser(String s, String s1, String s2, String s3) {
        throw new UnsupportedOperationException();
    }

    public User findUserById(String userId) {
        JahiaUserManagerService service = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaUser user = service.lookupUserByKey(userId);
        if (user != null) {
            Properties properties = user.getProperties();
            return new UserImpl(userId, properties.getProperty("j:firstName"), properties.getProperty("j:lastName"), properties.getProperty("j:email"));
        }
        return null;
    }

    public List<User> findUsersById(String... strings) {
        throw new UnsupportedOperationException();
    }

    public List<User> findUsers() {
        throw new UnsupportedOperationException();
    }

    public void deleteUser(String s) {
        throw new UnsupportedOperationException();
    }

    public String createGroup(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    public List<User> findUsersByGroup(String s) {
        throw new UnsupportedOperationException();
    }

    public Group findGroupById(String groupId) {
        JahiaGroupManagerService service = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup group = service.lookupGroup(groupId);
        if (group != null) {
            return new GroupImpl(groupId, group.getName(),"jahia");
        }
        return null;
    }

    public List<Group> findGroupsByUserAndGroupType(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    public List<Group> findGroupsByUser(String userId) {
        List<Group> results = new ArrayList<Group>();
        JahiaUser user = this.userService.lookupUserByKey(userId);
        if (user != null) {
            List<String> l = groupService.getUserMembership(user);
            for (String groupKey : l) {
                results.add(findGroupById(groupKey));
            }
            Set<Role> roleSet = user.getRoles();
            for (Role role : roleSet) {
                results.add(new GroupImpl("{role}"+role.getName()+":"+role.getSite(),role.getName(),"jahia"));
            }
        }
        return results;
    }

    public void deleteGroup(String s) {
        throw new UnsupportedOperationException();
    }

    public void createMembership(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    public void deleteMembership(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    class UserImpl implements User {
        private String id;
        private String givenName;
        private String familyName;
        private String businessEmail;

        UserImpl(String id, String givenName, String familyName, String businessEmail) {
            this.id = id;
            this.givenName = givenName;
            this.familyName = familyName;
            this.businessEmail = businessEmail;
        }

        public String getId() {
            return id;
        }

        public String getGivenName() {
            return givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public String getBusinessEmail() {
            return businessEmail;
        }
    }

    class GroupImpl implements Group {
        private String id;
        private String name;
        private String type;

        GroupImpl(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}

