/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is keep to maintain compatibility with olds users/groups providers
 * @author kevan
 * @deprecated
 */
@Deprecated
public abstract class JahiaGroupManagerProvider extends JahiaService implements InitializingBean, DisposableBean {

    private static class PatternHolder {
        static final Pattern pattern = Pattern.compile(SettingsBean.getInstance().lookupString("userManagementGroupNamePattern"));
    }

    private static Logger logger = LoggerFactory.getLogger(JahiaGroupManagerProvider.class);

    private boolean defaultProvider = false;
    private boolean readOnly = false;
    private int priority = 99;
    private String key;
    protected JahiaGroupManagerService groupManagerService;

    protected static Pattern getGroupNamePattern() {
        return PatternHolder.pattern;
    }

    /**
     * Validates provided group name against a regular expression pattern, specified in the Jahia configuration.
     *
     * @param name
     *            the group name to be validated
     * @return <code>true</code> if the specified group name matches the validation pattern
     */
    public boolean isGroupNameSyntaxCorrect(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        boolean nameValid = getGroupNamePattern().matcher(name).matches();
        if (!nameValid && logger.isDebugEnabled()) {
            logger.debug("Validation failed for the group name: " + name + " against pattern: "
                    + getGroupNamePattern().pattern());
        }
        return nameValid;
    }

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

    public void afterPropertiesSet() throws Exception {
        if (groupManagerService != null) {
            groupManagerService.registerProvider(this);
        }
    }

    @Override
    public void destroy() {
        if (groupManagerService != null) {
            groupManagerService.unregisterProvider(this);
        }
    }

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
     * @param user, the user you want to get his access grants sites list.
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
     * This function checks on a given site if the groupname has already been
     * assigned to another group.
     *
     * @param siteID siteID the site id
     * @param name String representing the unique group name.
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
     * @param groupKey Group's unique identification key.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public abstract JahiaGroup lookupGroup (String groupKey);

    /**
     * Lookup the group information from the underlying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cache from the database.
     *
     * @param siteID the site id
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

    public void flushCache() {
        // do nothing
    }
}
