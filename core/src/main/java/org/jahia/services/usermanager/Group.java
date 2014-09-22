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
/*  ------------------------------------------------------------------------
                                       ____.
                           __/\ ______|    |__/\.     _______
                __   .____|    |       \   |    +----+       \
        _______|  /--|    |    |    -   \  _    |    :    -   \_________
       \\______: :---|    :    :           |    :    |         \________>
               |__\---\_____________:______:    :____|____:_____\
                                          /_____|

                    . . . i n   j a h i a   w e   t r u s t . . .


    ------------------------------------------------------------------------

    CHANGES:
    18-DEC-2001 (NK) : Added properties to group
    19-AUG-2003 (FH) : - Made the class Serializable to comply with the JCS
                         caching system.
                       - Javadoc fixes

    ------------------------------------------------------------------------ */

package org.jahia.services.usermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.*;

/**
 * This class represents a group of <code>Principals</code> which can be users
 * or groups. A group can have subgroups holding again users or subgroups.</br>
 * </br>
 * Each group is defined with a name, which is unique inside a site, but not
 * necessarily unique between two sites. Each group has also a unique
 * identification key, which is unique in the database. This way, a group is
 * uniquely identified in all the sites and Jahia servers sharing the same
 * database.</br>
 * </br>
 * The group names are used only to present an intuitive signification for the
 * administrators of each site. As the key is the only way to assure unicity
 * between the sites and servers, it will be used to represent the group inside
 * of jahia and not the group name.
 *
 * @author Fulco Houkes
 * @version 2.2
 */
public class Group implements JahiaPrincipal, java.security.acl.Group{

    private static final long serialVersionUID = 3192050315335252786L;
    private transient static Logger logger = LoggerFactory.getLogger(JahiaGroup.class);
    /**
     * Group unique identification name
     */
    protected String name;

    /**
     * Group global identification key, unique in all the sites and Jahia
     * servers sharing the same groups data source.
     */
    protected String path;

    private String siteKey;

    public Group(String name, String path, String siteKey) {
        this.name = name;
        this.path = path;
        this.siteKey = siteKey;
    }

    /**
     * Return the group key.
     *
     * @return REturn the unique group identification key.
     */
    public String getGroupKey() {
        return path;
    }


    /**
     * Returns the unique identifier of this principal object.
     *
     * @return The unique identifier of this group.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the name of this group.
     *
     * @return The name of this group.
     */
    public String getGroupname() {
        return name;
    }

    /**
     * Adds the specified member to the group.
     *
     * @param user the principal to add to this group.
     * @return true if the member was successfully added,
     * false if the principal was already a member.
     */
    @Override
    public boolean addMember(Principal user) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified member from the group.
     *
     * @param user the principal to remove from this group.
     * @return true if the principal was removed, or
     * false if the principal was not a member.
     */
    @Override
    public boolean removeMember(Principal user) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the passed principal is a member of the group. This method
     * does a recursive search, so if a principal belongs to a group which is a
     * member of this group, true is returned.
     * Note this method does not load automatically all members of the group if they were not preloaded before.
     *
     * @param principal The principal whose membership is to be checked.
     * @return Return true if the principal is a member of this group, false otherwise.
     */
    public boolean isMember(Principal principal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an Iterator of the members in the group. The returned objects
     * can be instances of either <code>Principal</code> or <code>Group</code>.
     * </br></br>
     * Note that the <code>Group</code> is an instantiation of the
     * <code>Principal</code> class.
     *
     * @return An Iterator of the group members.
     */
    public Enumeration<Principal> members() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        return "JahiaGroup{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", siteKey='" + siteKey + '\'' +
                '}';
    }

    /**
     * Get the path of this user in the local store. For examle for LDAP user this will return the path of
     * the user in the JCR with all necessary encoding.
     *
     * @return String representation of the name of the provider of this user
     */
    @Override
    public String getLocalPath() {
        return path;
    }

    /**
     * Get the providerName of this user.
     *
     * @return String representation of the name of the provider of this user
     */
    @Override
    public String getProviderName() {
        return null;
    }

    public String getSiteKey() {
        return siteKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group that = (Group) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (siteKey != null ? !siteKey.equals(that.siteKey) : that.siteKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (siteKey != null ? siteKey.hashCode() : 0);
        return result;
    }
}