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

 package org.jahia.services.usermanager;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Predrag Viceic <Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */

public abstract class JahiaGroupManagerProvider extends JahiaService implements InitializingBean {
// ------------------------------ FIELDS ------------------------------

    private static Logger logger = LoggerFactory
	        .getLogger(JahiaGroupManagerProvider.class);

	private static Pattern groupNamePattern;

	private boolean defaultProvider = false;
    private boolean readOnly = false;
    private int priority = 99;
    private String key;
    private JahiaGroupManagerService groupManagerService;

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

    public void afterPropertiesSet() throws Exception {
    	if (groupManagerService != null) {
    		groupManagerService.registerProvider(this);
    	}
    }

//-------------------------------------------------------------------------
    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */
    public abstract JahiaGroup createGroup(int siteID, String name, Properties properties, boolean hidden);

    public abstract boolean deleteGroup(JahiaGroup g);

    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param JahiaUser user, the user you want to get his access grants sites list.
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
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param String groupKey Group's unique identification key.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (String groupKey);

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

	/**
	 * Returns an instance of the group manager.
	 * 
	 * @return an instance of the group manager
	 */
	protected JahiaGroupManagerService getGroupManagerService() {
    	return groupManagerService;
    }

	/**
	 * Injects the group management service instance.
	 * 
	 * @param groupManagerService an instance of the group management service
	 */
	public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
    	this.groupManagerService = groupManagerService;
    }

    public void flushCache(){

    };
}
