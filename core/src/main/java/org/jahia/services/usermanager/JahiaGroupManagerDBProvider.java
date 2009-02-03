/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.usermanager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaGroupManager;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.utils.JahiaTools;

import java.util.*;

public class JahiaGroupManagerDBProvider extends JahiaGroupManagerProvider {
// ------------------------------ FIELDS ------------------------------

    public static final String USERS_GROUPNAME = "users";
    public static final String ADMINISTRATORS_GROUPNAME = "administrators";
    public static final String GUEST_GROUPNAME = "guest";
    public static final String GROUPNAME_PROPERTY_NAME = "groupname";

    public static final String PROVIDER_NAME = "jahia";

    // the DB Group cache name.
    public static final String DB_GROUP_CACHE = "DBGroupsCache";

    /** the overall provider Group cache name. */
    public static final String PROVIDERS_GROUP_CACHE = "ProvidersGroupsCache";

    /** User Member type designation * */
    public static int USERTYPE = 1;

    /** Group Member type designation * */
    public static int GROUPTYPE = 2;

    private static Logger logger = Logger
	        .getLogger(JahiaGroupManagerDBProvider.class);

    private static JahiaGroupManagerDBProvider mGroupManagerDBProvider;

    private JahiaUserManagerService userService;

    private JahiaGroupManager groupManager = null;

// -------------------------- STATIC METHODS --------------------------

    //-------------------------------------------------------------------------
    /**
     * Create an new instance of the Group Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the Group Manager Service.
     */
    public static JahiaGroupManagerDBProvider getInstance () {
        if (mGroupManagerDBProvider == null) {
            try {
                mGroupManagerDBProvider = new JahiaGroupManagerDBProvider ();
            } catch (JahiaException ex) {
                logger.error (
                        "Could not create an instance of the JahiaGroupManagerDBService class",
                        ex);
            }
        }
        return mGroupManagerDBProvider;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor.
     *
     * @throws JahiaException Raise a JahiaException when during initialization
     *                        one of the needed services could not be instanciated.
     */
    protected JahiaGroupManagerDBProvider ()
        throws JahiaException {
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setGroupManager(JahiaGroupManager groupManager) {
        this.groupManager = groupManager;
    }   //--------------------------------------------------------------------------

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void start() {
        groupManager.setUserService(userService);
    }

    public void stop() {}

    //-------------------------------------------------------------------------
    /**
     * Create a new group in the system.
     *
     * @param int       siteID the site owner of this user
     * @param groupname Group's unique identification name
     *
     * @return a reference on a group object on success, or if the groupname
     *         already exists or another error occurred, null is returned.
     */
    public synchronized JahiaGroup createGroup (int siteID, String name,
                                                Properties properties) {
        // try to avoid a NullPointerException
        if (!isGroupNameSyntaxCorrect(name)) {
            return null;
        }

        // Check if the group already exists
        if (groupExists (siteID, name)) {
            return null;
        }

        // Create the group
        JahiaDBGroup group = null;
        String groupKey = name + ":" + String.valueOf(siteID);
        group = new JahiaDBGroup(0, name, groupKey, siteID, null, properties, false);

        groupManager.createGroup(group);
        group = (JahiaDBGroup) groupManager.findGroupBySiteAndName(siteID, name);
        return group;
    }

    /**
     * This function checks on a given site if the groupname has already been
     * assigned to another group.
     *
     * @param int       siteID the site id
     * @param groupname String representing the unique group name.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean groupExists (int siteID, String name) {
        return (lookupGroup (siteID, name) != null);
    }

    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param int  siteID the site id
     * @param name Group's unique identification name.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occurred.
     */
    public JahiaGroup lookupGroup (int siteID, String name) {
        // try to avoid a NullPointerException
        if (name == null) {
            return null;
        }

		/* 2004-16-06 : update by EP
		new cache to browse : cross providers ...  */
	    JahiaGroup group = groupManager.findGroupBySiteAndName(siteID, name);

        return group;
    }

    //-------------------------------------------------------------------------
    /**
     * Delete a group from the system. Updates the database automatically, and
     * signal the ACL Manager that the group no longer exists.
     *
     * @param group Reference to a JahiaGroup object.
     *
     * @return Return true on success, or false on any failure.
     */
    public synchronized boolean deleteGroup (JahiaGroup group) {
        if (group == null) {
            return false;
        }

        // cannot remove the super admin group
        if ((group.getSiteID () == 0) &&
                (group.getGroupname ().equals (ADMINISTRATORS_GROUPNAME))) {
            return false;
        }

        // It's not allowed to remover the admin, guest and users group !
        /*
             if ((group.getGroupname().equals(ADMINISTRATORS_GROUPNAME)) ||
            (group.getGroupname().equals(USERS_GROUPNAME)) ||
            (group.getGroupname().equals(GUEST_GROUPNAME)))
                  {
            return false;
                  }
         */

        JahiaACLManagerService aclService = null;

        // Get the ACL Manager Service.
        try {
            aclService = ServicesRegistry.getInstance ().
                    getJahiaACLManagerService ();
            if (aclService == null) {
                logger.error ("ACL Manager Service instance is null !!");
                return false;
            }
        } catch (NullPointerException ex) {
            logger.error ("Could not get the ACL Manager Service !!", ex);
            return false;
        }

        // delete the group from the database and from the cache.
        boolean result = false;

        if (groupManager.deleteGroup(group)) {
            // remove the group from the cache

            // invalidate the group in the ACL manager
            aclService.removeGroupFromAllACLs (group);

            // invalidate the group
            group = null;
            result = true;
        }
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param JahiaUser user, the user you want to get his access grants sites list.
     *
     * @return Return a List containing all JahiaSite objects where the user has an access.
     *
     * @author Alexandre Kraft
     */
    public List getAdminGrantedSites (JahiaUser user) {
        List grantedSites = new ArrayList();
        try {
            Iterator sitesList = ServicesRegistry.getInstance ().
                    getJahiaSitesService ().getSites ();

            while (sitesList.hasNext ()) {
                JahiaSite jahiaSite = (JahiaSite) sitesList.next ();
                logger.debug ("check granted site " + jahiaSite.getServerName ());

                if ((JahiaSiteTools.getAdminGroup (jahiaSite) != null) &&
                    JahiaSiteTools.getAdminGroup (jahiaSite).isMember (user)) {
                    logger.debug ("granted site for " + jahiaSite.getServerName ());
                    grantedSites.add (jahiaSite);
                }
            }
        } catch (JahiaException e) {
            logger.error("getAdminGrantedSites",e);
        }

        return grantedSites;
    } // end getAdminGrantedSites

    //-------------------------------------------------------------------------
    /**
     *
     */
    public JahiaGroup getAdministratorGroup (int siteID) {
        return lookupGroup (siteID, ADMINISTRATORS_GROUPNAME);
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public List getGroupList () {
        return groupManager.getGroupKeys();
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public List getGroupList (int siteID) {
        return groupManager.getGroupKeys(siteID);
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List getGroupnameList () {
        return groupManager.getGroupNames();
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List getGroupnameList (int siteID) {
        return groupManager.getGroupNames(siteID);
    }

    //-------------------------------------------------------------------------
    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */
    public final JahiaGroup getGuestGroup (int siteID) {
        return lookupGroup (siteID, GUEST_GROUPNAME);
    }

    //-------------------------------------------------------------------------
    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     *
     * @return Return a List of strings holding all the group names to
     *         which the user as access. On any error, the returned List
     *         might be null.
     */
    public List getUserMembership (JahiaUser user) {
        // try to avoid a NullPointerException
        if (user == null) {
            return null;
        }

        return groupManager.getUserMembership(user.getUserKey());
    }

    //-------------------------------------------------------------------------
    /**
     * Return an instance of the users group.
     *
     * @return Return the instance of the users group. Return null on any failure
     */
    public final JahiaGroup getUsersGroup (int siteID) {
        return lookupGroup (siteID, USERS_GROUPNAME);
    }

    //-------------------------------------------------------------------------
    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     *
     * @return Return true on success, or false on any failure.
     */
    public synchronized boolean removeUserFromAllGroups (JahiaUser user) {
        // try to avoid a NullPointerException
        if (user == null) {
            return false;
        }

        boolean result = false;

        // remove all the users from the database
        result = groupManager.removeUserFromAllGroups(user.getUserKey());
        return result;
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criteria
     *
     * @return Set a set of JahiaGroup elements that correspond to those
     *         search criteria
     *
     * @todo this code could be cleaner if groupname was a real group property
     * but as it isn't we have to do a lot of custom handling.
     */
    public Set searchGroups (int siteID, Properties searchCriterias) {
        /** @todo implement siteID into SQL request */
        Set result = new HashSet();
        Set groupKeys = new HashSet();

        if (searchCriterias == null) {
            searchCriterias = new Properties();
            searchCriterias.setProperty("*", "*");
        }

        boolean haveWildCardProperty = false;
        if (searchCriterias.getProperty("*") != null) {
            haveWildCardProperty = true;
        }

        boolean onlyGroupNameInSelect = false;
        if ((searchCriterias.getProperty(GROUPNAME_PROPERTY_NAME) != null) ||
            (haveWildCardProperty)) {
            String curCriteriaValue;
            if (haveWildCardProperty) {
                curCriteriaValue = makeLIKEString(searchCriterias.
                                                  getProperty("*"));
            } else {
                curCriteriaValue = makeLIKEString(searchCriterias.
                                                  getProperty(GROUPNAME_PROPERTY_NAME));
            }
            groupManager.searchGroupName(curCriteriaValue, siteID, groupKeys);
            if ((!haveWildCardProperty) && (searchCriterias.size() == 1)) {
                onlyGroupNameInSelect = true;
            }
        }

        if (!onlyGroupNameInSelect) {
            Iterator criteriaNames = searchCriterias.keySet().iterator();
            List criteriaValueList = new ArrayList(searchCriterias.size());
            List criteriaNameList = new ArrayList(searchCriterias.size());
            while (criteriaNames.hasNext()) {
                String curCriteriaName = (String) criteriaNames.next();
                String curCriteriaValue = makeLIKEString(searchCriterias.getProperty(curCriteriaName));
                criteriaValueList.add(curCriteriaValue);
                criteriaNameList.add(curCriteriaName);
                if ("*".equals(curCriteriaName)) {
                    // we must look in all columns, including special for
                    // the user.
                    onlyGroupNameInSelect = false;
                } else {
                    if (GROUPNAME_PROPERTY_NAME.equals(curCriteriaName)) {
                        // group name filter is a special case and is not
                        // stored in the property table.
                    } else {
                        onlyGroupNameInSelect = false;
                    }
                }
            }
            groupManager.searchGroupName(criteriaNameList,criteriaValueList,siteID,groupKeys, PROVIDER_NAME);
        }


        // now that we have all the keys, let's load all the groups.
        Iterator groupKeyEnum = groupKeys.iterator();
        while (groupKeyEnum.hasNext()) {
            String curGroupKey = (String) groupKeyEnum.next();
            JahiaGroup group = lookupGroup(curGroupKey);
            result.add(group);
        }

        return result;
    }

    /**
     * Transforms a search with "*" characters into a valid LIKE statement
     * with "%" characters. Also escapes the string to remove all "'" and
     * other chars that might disturb the request construct.
     *
     * @param input the original String
     *
     * @return String a resulting string that has
     */
    private String makeLIKEString (String input) {
        String result = JahiaTools.replacePattern (input, "*", "%");
        result = JahiaTools.replacePattern (result, "'", "\\'");
        result = JahiaTools.replacePattern (result, "\"", "\\\"");
        result = JahiaTools.replacePattern (result, "_", "\\_");
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param String groupKey Group's unique identification key.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occurred.
     */
    public JahiaGroup lookupGroup (String groupKey) {
		/* 2004-16-06 : update by EP
		new cache to browse : cross providers ... */
        JahiaGroup group = groupManager.findJahiaGroupByGroupKey(groupKey);
        return group;
    }

    public void updateCache(JahiaGroup jahiaGroup) {
    }
}
