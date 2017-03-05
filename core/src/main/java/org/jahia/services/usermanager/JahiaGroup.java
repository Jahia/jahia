/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import java.security.Principal;
import java.security.acl.Group;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
public abstract class JahiaGroup implements JahiaPrincipal, Group {

    private static final long serialVersionUID = 3192050315335252786L;

    /** Group unique identification name */
    protected String mGroupname;

    /**
     * Group global identification key, unique in all the sites and Jahia
     * servers sharing the same groups data source.
     */
    protected String mGroupKey;

    protected boolean hidden = false;

    /** The site id */
    protected int mSiteID;

    protected boolean preloadedGroups;

    protected Map<String, Boolean> membership = new ConcurrentHashMap<String, Boolean>();

    /** Map holding all the group members. */
    protected Set<Principal> mMembers;

    /**
     * Get grp's properties list.
     *
     * @return Return a reference on the grp's properties list, or null if no
     *         property is present.
     */
    public abstract Properties getProperties ();


    /**
     * Retrieve the requested grp property.
     *
     * @param key Property's name.
     *
     * @return Return the property's value of the specified key, or null if the
     *         property does not exist.
     */
    public abstract String getProperty (String key);


    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     * @deprecated
     */
    public abstract boolean removeProperty (String key);


    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     * @deprecated
     */
    public abstract boolean setProperty (String key, String value);


    /**
     * Adds the specified member to the group.
     *
     * @param principal The principal to add to this group.
     *
     * @return Return true if the member was successfully added, false if the
     *         principal was already a member.
     * @deprecated
     */
    public abstract boolean addMember (Principal principal);

    /**
     * Adds the specified members to the group.
     *
     * @param principals The principals to add to this group.
     * @deprecated
     */
    public abstract void addMembers(final Collection<Principal> principals);

    /**
     * Compares this principal to the specified object. Returns true if the object
     * passed in matches the principal represented by the implementation of this
     * interface.
     *
     * @param another Principal to compare with.
     *
     * @return Return true if the principal passed in is the same as that
     *         encapsulated by this principal, and false otherwise.
     */
    public boolean equals(Object another) {

        if (this == another) return true;

        if (another != null && this.getClass() == another.getClass()) {
            return (mGroupKey.equals(((JahiaGroup) another).getGroupKey()));
        }
        return false;
    }


    /**
     * Return the group key.
     *
     * @return REturn the unique group identification key.
     */
    public String getGroupKey () {
        return mGroupKey;
    }


    /**
     * Returns the unique identifier of this principal object.
     *
     * @return The unique identifier of this group.
     */
    public String getName () {
        return mGroupname;
    }


    /**
     * Returns the name of this group.
     *
     * @return The name of this group.
     */
    public String getGroupname () {
        return mGroupname;
    }


    /**
     * Returns the site id.
     *
     * @return int the siteID.
     * @deprecated use siteKey instead
     */
    public int getSiteID () {
        return mSiteID;
    }


    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    public abstract int hashCode ();


    /**
     * Returns true if the passed principal is a member of the group. This method
     * does a recursive search, so if a principal belongs to a group which is a
     * member of this group, true is returned.
     * Note this method does not load automatically all members of the group if they were not preloaded before.
     *
     * @param principal The principal whose membership is to be checked.
     *
     * @return Return true if the principal is a member of this group, false otherwise.
     * @deprecated
     */
    public boolean isMember (Principal principal) {
        throw new UnsupportedOperationException("Method not supported here, use JCRGroupNode instead");
    }

    /**
     * Returns an Iterator of the members in the group. The returned objects
     * can be instances of either <code>Principal</code> or <code>Group</code>.
     * </br></br>
     * Note that the <code>Group</code> is an instantiation of the
     * <code>Principal</code> class.
     *
     * @return An Iterator of the group members.
     * @deprecated use getMembers
     */
    public Enumeration<Principal> members () {
        throw new UnsupportedOperationException("Method not supported here, use JCRGroupNode instead");
    }

    /**
     * @deprecated
     */
    public Collection<Principal> getMembers() {
        return getMembersMap();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     *
     * @return members of this group
     * @deprecated
     */
    protected abstract Set<Principal> getMembersMap();

    /**
     * This method returns ONLY a list of users. All sub groups are expanded
     * to return only the full list of members.
     *
     * @return Set a set of JahiaUsers that are all the implicit and explicit
     *         users in this group
     * @deprecated
     */
    public Set<Principal> getRecursiveUserMembers () {
        throw new UnsupportedOperationException("Method not supported here, use JCRGroupNode instead");
    }


    /**
     * Removes the specified member from the group.
     *
     * @param principal The principal to remove from this group.
     *
     * @return Return true if the principal was removed, or false if the
     *         principal was not a member.
     * @deprecated
     */
    public abstract boolean removeMember (Principal principal);


    /**
     * Removes all members from the group.
     *
     * @return Return false on error
     * @deprecated
     */
    public boolean removeMembers () {
        throw new UnsupportedOperationException("Method not supported here, use JCRGroupNode instead");
    }


    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    public abstract String toString ();

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     * @deprecated
     */
    public abstract String getProviderName ();


    public boolean isPreloadedGroups() {
        return preloadedGroups;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}