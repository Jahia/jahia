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
// NK - 18 Dec. 2001 :
//   1. Added properties to group

package org.jahia.services.usermanager;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;


public abstract class JahiaGroupManagerService extends JahiaService {

    public static final String USERS_GROUPNAME = "users";
    public static final String ADMINISTRATORS_GROUPNAME = "administrators";
    public static final String GUEST_GROUPNAME = "guest";


    /**
     * Create a new group in the system.
     *
     * @param int       siteID the site owner of this group
     * @param groupname Group's unique identification name
     *
     * @return Retrun a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */
    public abstract JahiaGroup createGroup (int siteID, String name, Properties properties);


    //-------------------------------------------------------------------------
    /**
     * Delete a group from the system. Updates the database automatically, and
     * signal the ACL Manager that the group no longer exists.
     *
     * @param group Reference to a JahiaGroup object.
     *
     * @return Return true on success, or false on any failure.
     */
    public abstract boolean deleteGroup (JahiaGroup group);
    //-------------------------------------------------------------------------
    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param JahiaUser user, the user you want to get his access grantes sites list.
     *
     * @return Return a List containing all JahiaSite objects where the user has an access.
     *
     * @author Alexandre Kraft
     */
    public abstract List<JahiaSite> getAdminGrantedSites (JahiaUser user) throws JahiaException;


    //-------------------------------------------------------------------------
    /**
     *
     */
    public abstract JahiaGroup getAdministratorGroup (int siteID);

    public abstract JahiaUser getAdminUser (int siteId);

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @param int the site id
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public abstract List<String> getGroupList ();

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * groups of a site.
     *
     * @param int the site id
     *
     * @return Return a List of identifier of all groups of this site.
     */
    public abstract List<String> getGroupList (int siteID);

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */
    public abstract List<String> getGroupnameList ();

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @param int the site id
     *
     * @return Return a List of strings containing all the group names.
     */
    public abstract List<String> getGroupnameList (int siteID);


    //-------------------------------------------------------------------------
    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */
    public abstract JahiaGroup getGuestGroup (int siteID);


    //-------------------------------------------------------------------------
    /**
     * Returns a List of GroupManagerProviderBean object describing the
     * available group management providers
     *
     * @return result a List of GroupManagerProviderBean objects that describe
     *         the providers. This will never be null but may be empty if no providers
     *         are available.
     */
    public abstract List<? extends JahiaGroupManagerProvider> getProviderList ();

    //-------------------------------------------------------------------------
    /**
     * Return the list of groups to shich the specified user has access.
     *
     * @param user Valid reference on an existing group.
     *
     * @return Return a List of strings holding all the group names to
     *         which the user as access.
     */
    public abstract List<String> getUserMembership (JahiaUser user);


    //-------------------------------------------------------------------------
    /**
     * This function checks on a gived site if the groupname has already been
     * assigned to another group.
     *
     * @param int       siteID the site id
     * @param groupname String representing the unique group name.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public abstract boolean groupExists (int siteID, String name);


    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param String groupID Group's unique identification id.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (String groupID);


    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param int       siteID the site id
     * @param groupname Group's unique identification name.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (int siteID, String name);


    //-------------------------------------------------------------------------
    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     *
     * @return Return true on success, or false on any failure.
     */
    public abstract boolean removeUserFromAllGroups (JahiaUser user);

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     *
     * @return List a List of JahiaGroup elements that correspond to those
     *         search criterias
     */
    public abstract Set<JahiaGroup> searchGroups (int siteID, Properties searchCriterias);

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param providerKey     key of the provider in which to search, may be
     *                        obtained by calling getProviderList()
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaGroup elements that correspond to those
     *         search criterias
     */
    public abstract Set<JahiaGroup> searchGroups (String providerKey, int siteID,
                                      Properties searchCriterias);


    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public abstract void updateCache(JahiaGroup jahiaGroup);
    
    /**
	 * Validates provided group name against a regular expression pattern,
	 * specified in the Jahia configuration.
	 * 
	 * @param name
	 *            the group name to be validated
	 * @return <code>true</code> if the specified group name matches the
	 *         validation pattern
	 */
	public abstract boolean isGroupNameSyntaxCorrect(String name);

}
