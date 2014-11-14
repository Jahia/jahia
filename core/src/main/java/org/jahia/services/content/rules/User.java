/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username, realm);
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
