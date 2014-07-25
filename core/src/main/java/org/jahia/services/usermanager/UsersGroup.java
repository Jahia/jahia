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
 package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.Node;
import java.util.*;
import java.security.Principal;

/**
 * Special user group that represents all users of the system.
 * 
 * @author Thomas Draier
 */
public class UsersGroup extends JahiaGroup {
    private static final long serialVersionUID = 8220270074777283565L;

    public UsersGroup(Node nodeWrapper, JCRTemplate jcrTemplate, int siteID) {
    }

    @Override
    public Enumeration<Principal> members() {
        return new Vector<Principal>(getRecursiveUserMembers()).elements();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     *
     * @return members of this group
     */
    @Override
    protected Set<Principal> getMembersMap() {
        return null;
    }

    @Override
    public Set<Principal> getRecursiveUserMembers() {
        Set<Principal> users = new HashSet<Principal> ();

        List<Principal> userList = new LinkedList<Principal>();
        JahiaUserManagerService jahiaUserManagerService = ServicesRegistry.getInstance()
                .getJahiaUserManagerService();
        List<String> l = jahiaUserManagerService.getUserList();
        for (String s : l) {
            userList.add(jahiaUserManagerService.lookupUserByKey(s).getJahiaUser());
        }
        JahiaUser guest = jahiaUserManagerService
                .lookupUser(JahiaUserManagerService.GUEST_USERNAME).getJahiaUser();
        userList.remove(guest);
        if (userList != null) {
            users.addAll(userList);
        }
        // should we list ldap users here ?
        return users;
    }

    /**
     * Get grp's properties list.
     *
     * @return Return a reference on the grp's properties list, or null if no
     * property is present.
     */
    @Override
    public Properties getProperties() {
        return null;
    }

    /**
     * Retrieve the requested grp property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     * property does not exist.
     */
    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public boolean addMember(Principal principal) {
        return false;
    }

    @Override
    public boolean removeMember(Principal principal) {
        return false;
    }

    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        return null;
    }

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     */
    @Override
    public String getProviderName() {
        return null;
    }

    @Override
    public boolean isMember(Principal principal) {
        return principal != null && !principal.getName().equals(JahiaUserManagerService.GUEST_USERNAME);
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    @Override
    public int hashCode() {
        return 0;
    }
}
