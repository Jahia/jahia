/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(JahiaGroup.class);

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
     */
    public abstract boolean removeProperty (String key);


    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     */
    public abstract boolean setProperty (String key, String value);


    /**
     * Adds the specified member to the group.
     *
     * @param principal The principal to add to this group.
     *
     * @return Return true if the member was successfully added, false if the
     *         principal was already a member.
     */
    public abstract boolean addMember (Principal principal);


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
     */
    public boolean isMember (Principal principal) {
        if (principal == null) {
            return false;
        }

        Boolean isMember = membership.get(JahiaUserManagerService.getKey(principal));
        if (isMember != null) {
            return isMember;
        }
        
        boolean result = false;
        if (mMembers != null) {
            // For each member check if it's the member we are looking for,
            // otherwise, if the member is a group, check recursively in this group
            // for the requested member.
            String pname = JahiaUserManagerService.getKey(principal);
            boolean principalIsGuest = pname.startsWith(JahiaUserManagerService.GUEST_USERNAME + ":");
            for (Principal member : mMembers) {
                if (member != null) {
                    // check if the member is the one we are looking for
                    String mname = JahiaUserManagerService.getKey(member);
                    if (mname .equals (pname) ||
                        (principalIsGuest && mname.startsWith(JahiaUserManagerService.GUEST_USERNAME+":"))) {
                        result = true;
                    } else {
    
                        // if the member is a group look for the principal in this
                        // group. Groups are already loaded.
                        if (member instanceof Group) {
                            result = ((Group) member).isMember (principal);
                        }
                    }
                    if (result) {
                        break;
                    }
                }
            }
        }
        
        if (!result) {
            /** @todo this is a temporary solution until we have implicit
             *  group implementation. Then we will have guest_provider, users_provider
             *  groups that are contained within the global users and guest groups
             */
            // let's now check if we are in the special case of guest and users
            // for external sources users
            // user could be external database user, let's look him up...
            if ((JahiaGroupManagerService.GUEST_GROUPNAME.equals (mGroupname)) ||
                    (JahiaGroupManagerService.USERS_GROUPNAME.equals (mGroupname))) {
                JahiaUser extUser = ServicesRegistry.getInstance ().getJahiaUserManagerService ()
                        .lookupUser(principal.getName ());
                if (extUser != null) {
                    if (!(extUser instanceof JCRUser)) {
                        result = true;
                    }
                }
            }
        }

        return result;

        //Principal tmp = (Principal)getMembers().get(principal.getName());
        //return (tmp != null);
    }

    /**
     * Returns an Iterator of the members in the group. The returned objects
     * can be instances of either <code>Principal</code> or <code>Group</code>.
     * </br></br>
     * Note that the <code>Group</code> is an instanciation of the
     * <code>Principal</code> class.
     *
     * @return An Iterator of the group members.
     */
    public Enumeration<Principal> members () {
        return new Vector<Principal>(getMembersMap()).elements();
    }

    public Collection<Principal> getMembers() {
        return getMembersMap();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     * 
     * @return members of this group
     */
    protected abstract Set<Principal> getMembersMap();

    /**
     * This method returns ONLY a list of users. All sub groups are expanded
     * to return only the full list of members.
     *
     * @return Set a set of JahiaUsers that are all the implicit and explicit
     *         users in this group
     */
    public Set<Principal> getRecursiveUserMembers () {
        Set<Principal> users = new HashSet<Principal> ();

        /** @todo this is a temporary solution until we have implicit
         *  group implementation. Then we will have guest_provider, users_provider
         *  groups that are contained within the global users and guest groups
         */
        // let's now check if we are in the special case of guest and users
        // for external sources users
        // user could be external database user, let's look him up...
        if ((JahiaGroupManagerService.GUEST_GROUPNAME.equals (mGroupname)) ||
                (JahiaGroupManagerService.USERS_GROUPNAME.equals (mGroupname))) {
            List<Principal> userList = new LinkedList<Principal>();
            JahiaUserManagerService jahiaUserManagerService = ServicesRegistry.getInstance()
                    .getJahiaUserManagerService();
            List<String> l = jahiaUserManagerService.getUserList();
            for (String s : l) {
                userList.add(jahiaUserManagerService.lookupUserByKey(s));
            }
            if (userList != null) {
                users.addAll(userList);
            }
            // now we still need to get the list of users coming that don't
            // belong to any site and that may come from an LDAP repository.
            List<String> userKeyList = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserList();
            if (userKeyList != null) {
                for (String curUserKey : userKeyList) {
                    JahiaUser curUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(curUserKey);
                    if (!(curUser instanceof JCRUser) && (!users.contains(curUser))) {
                        // this should add all users that don't come from
                        // the database provider and that aren't root.
                    users.add (curUser);
                }
                }
                return users;
            }
        }

        // For each member check if it's the member we are looking for,
        // otherwise, if the member is a group, check recursively in this group
        // for the requested member.
        for (Principal curMember : getMembersMap()) {
            // if the member is a group look for the principal in this
            // group. Groups are already loaded.
            if (curMember instanceof JahiaGroup) {
                JahiaGroup groupMember = (JahiaGroup) curMember;
                users.addAll (groupMember.getRecursiveUserMembers ());
            } else {
                users.add (curMember);
            }
        }

        return users;
    }


    /**
     * Removes the specified member from the group.
     *
     * @param principal The principal to remove from this group.
     *
     * @return Return true if the principal was removed, or false if the
     *         principal was not a member.
     */
    public abstract boolean removeMember (Principal principal);


    /**
     * Removes all members from the group.
     *
     * @return Return false on error
     */
    public boolean removeMembers () {
        for (Principal aMember : getMembersMap()) {
            removeMember (aMember);
        }
        return true;
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