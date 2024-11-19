/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
