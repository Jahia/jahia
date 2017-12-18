/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.content.rules;

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * 
 * User: toto
 * Date: 14 janv. 2008
 * Time: 19:55:10
 * 
 */
public class User {

    private static Logger log = LoggerFactory.getLogger(User.class);

    private String username;
    private String realm;
    private JCRUserNode user;

    public User(JCRUserNode user) {
        this.user = user;
    }

    public User(String username, String realm) {
        this.username = username;
        this.realm = realm;
    }

    public String getName() {
        if (username != null) {
            return username;
        }
        if (user != null) {
            return user.getName();
        }
        return null;
    }

    public String getRealm() {
        if (realm != null) {
            return realm;
        }
        if (user != null) {
            return user.getRealm();
        }
        return null;
    }

    public List<UserProperty> getProperties() {
        List<UserProperty> r = new ArrayList<UserProperty>();
        JCRUserNode jahiaUser = getUserNode();
        if (jahiaUser != null) {
            Map<String, String> p = null;
            try {
                p = jahiaUser.getPropertiesAsString();
                for (Map.Entry<String, String> entry : p.entrySet()) {
                    r.add(new UserProperty(this, entry.getKey(), entry.getValue()));
                }
            } catch (RepositoryException e) {
                log.error("Error while getting user properties", e);
            }
        }
        return r;
    }

    public List<Group> getGroups() {
        List<Group> r = new ArrayList<Group>();
        JahiaGroupManagerService grpManager = JahiaGroupManagerService.getInstance();
        JCRUserNode jahiaUser = getUserNode();
        if (jahiaUser != null) {
            List<String> groups = grpManager.getMembershipByPath(jahiaUser.getPath());
            for (String groupname : groups) {
                JCRGroupNode group = grpManager.lookupGroupByPath(groupname);
                r.add(new Group(group));
            }
        }
        return r;
    }

    public JCRUserNode getUserNode() {
        if (user == null && username != null) {
            if (!username.equals(JahiaLoginModule.SYSTEM) && !username.equals(JahiaLoginModule.GUEST)) {
                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username, realm, false);
            }
        }
        return user;
    }

    public JahiaUser getJahiaUser() {
        JCRUserNode userNode = getUserNode();
        if (userNode != null) {
            return userNode.getJahiaUser();
        }
        return null;
    }
}
