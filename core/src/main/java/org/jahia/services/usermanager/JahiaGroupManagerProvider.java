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
 package org.jahia.services.usermanager;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Predrag Viceic <Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */

public abstract class JahiaGroupManagerProvider extends JahiaService {
// ------------------------------ FIELDS ------------------------------

    private static Logger logger = Logger
	        .getLogger(JahiaGroupManagerProvider.class);

	private static Pattern groupNamePattern;

	private boolean defaultProvider = false;
    private boolean readOnly = false;
    private int priority = 99;
    private String key;

    protected static Pattern getGroupNamePattern() {
		if (groupNamePattern == null) {
			synchronized (JahiaUserManagerProvider.class) {
				if (groupNamePattern == null) {
					groupNamePattern = Pattern.compile(org.jahia.settings.SettingsBean.getInstance()
					        .lookupString("userManagementGroupNamePattern"));
				}
			}
		}
		return groupNamePattern;
	}

    /**
	 * Validates provided group name against a regular expression pattern,
	 * specified in the Jahia configuration.
	 * 
	 * @param name
	 *            the group name to be validated
	 * @return <code>true</code> if the specified group name matches the
	 *         validation pattern
	 */
	public boolean isGroupNameSyntaxCorrect(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}

		boolean nameValid = getGroupNamePattern().matcher(name).matches();
		if (!nameValid && logger.isDebugEnabled()) {
			logger.debug("Validation failed for the group name: " + name
			        + " against pattern: " + getGroupNamePattern().pattern());
		}
		return nameValid;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isDefaultProvider() {
        return defaultProvider;
    }

    /**
     * There can be only one... default provider
     * @param defaultProvider
     */
    public void setDefaultProvider(boolean defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

// -------------------------- OTHER METHODS --------------------------

//-------------------------------------------------------------------------
    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return Retrun a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */
    public abstract JahiaGroup createGroup(int siteID, String name, Properties properties, boolean hidden);

    public abstract boolean deleteGroup(JahiaGroup g);

    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param JahiaUser user, the user you want to get his access grantes sites list.
     *
     * @return Return a List containing all JahiaSite objects where the user has an access.
     *
     * @author Alexandre Kraft
     */
    public abstract List<JahiaSite> getAdminGrantedSites (JahiaUser user);

    /**
     *
     */
    public abstract JahiaGroup getAdministratorGroup (int siteID);

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public abstract List<String> getGroupList ();

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public abstract List<String> getGroupList (int siteID);

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */
    public abstract List<String> getGroupnameList ();

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @return Return a List of strings containing all the group names.
     */
    public abstract List<String> getGroupnameList (int siteID);

    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */
    public abstract JahiaGroup getGuestGroup (int siteID);

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     *
     * @return Return a List of strings holding all the group names to
     *         which the user as access. On any error, the returned List
     *         might be null.
     */
    public abstract List<String> getUserMembership (JahiaUser user);

    /**
     * Return an instance of the users group.
     *
     * @return Return the instance of the users group. Return null on any failure
     */
    public abstract JahiaGroup getUsersGroup (int siteID);

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

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param String groupKey Group's unique identification key.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (String groupKey);

    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param int  siteID the site id
     * @param name Group's unique identification name.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (int siteID, String name);

    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     *
     * @return Return true on success, or false on any failure.
     */
    public abstract boolean removeUserFromAllGroups (JahiaUser user);

    public abstract Set<JahiaGroup> searchGroups(int siteID, Properties searchCriterias);


    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public abstract void updateCache(JahiaGroup jahiaGroup);
}
