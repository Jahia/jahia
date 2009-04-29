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
package org.jahia.services.acl;

import org.jahia.admin.permissions.ManageServerPermissions;
import org.jahia.admin.permissions.ManageSitePermissions;
import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaAclManager;
import org.jahia.hibernate.manager.JahiaAclNameManager;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.toolbar.bean.Toolbar;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserAliasing;
import org.jahia.utils.JahiaTools;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.security.Principal;
import java.security.acl.Group;
import java.util.*;


/**
 * ACL Services
 *
 * @author Fulco Houkes
 * @author MAP
 * @version 1.2
 */
public class JahiaACLManagerService extends JahiaService {

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaACLManagerService.class);

    private static JahiaACLManagerService mACLService = new JahiaACLManagerService();

    // the preloaded container acls by page.
    public static final String PRELOADED_CTNR_ACL_BY_PAGE_CACHE = "PreloadedCtnrACLByPageCache";
    // the preloaded field acls by page.
    public static final String PRELOADED_FIELD_ACL_BY_PAGE_CACHE = "PreloadedFieldACLByPageCache";
    // the ACL Tree cache name.
    public static final String ACL_TREE_CACHE = "ACLTreeCache";

    private Cache mACLCache;

    private Cache mPreloadedContainerACLsByPageCache;

    private Cache mPreloadedFieldACLsByPageCache;

    private CacheService cacheService;

    private JahiaSitesService siteService;

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    protected JahiaAclManager manager;
    protected JahiaAclNameManager nameManager;

    protected Map sitePermissionsMap;
    protected Map serverPermissionsMap;

    public void setAclManager(JahiaAclManager manager) {
        this.manager = manager;
    }

    public void setAclNameManager(JahiaAclNameManager nameManager) {
        this.nameManager = nameManager;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Default constructor.
     *
     * @throws JahiaException
     */
    protected JahiaACLManagerService() {
    }


    /**
     * Initialization
     *
     * @throws JahiaInitializationException when the initialization process failed
     */
    public void start()
            throws JahiaInitializationException {

        try {
            mPreloadedContainerACLsByPageCache = cacheService.createCacheInstance(PRELOADED_CTNR_ACL_BY_PAGE_CACHE);
            mPreloadedFieldACLsByPageCache = cacheService.createCacheInstance(PRELOADED_FIELD_ACL_BY_PAGE_CACHE);

        } catch (JahiaInitializationException e) {
            logger.warn(e);
        }
        mACLCache = cacheService.createCacheInstance(ACL_TREE_CACHE);

        if (settingsBean.isAclPreloadActive()) {
            long startTime = System.currentTimeMillis();
            logger.info("Preloading ACLs from the database...");
            manager.preloadACLs(mACLCache);
            logger.info("Preloading ACLs from the database took "
                    + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    // Javadoc inherited from parent
    public synchronized void stop() {
        // flush the ACL cache
        mACLCache.flush();
    }

    /**
     * Return the singleton instance of the ACL Manager service.
     *
     * @return Return the reterence on the ACL Manager Service.
     */
    public static JahiaACLManagerService getInstance() {
        return mACLService;
    }


    /**
     * Create a new reference on a JahiaACL object.
     *
     * @param parent Reference on a parent JahiaACL object. Setting the
     *               parent reference object ot null, means the object will
     *               not be able to inherit access rights from a parent
     *               object.
     * @return Return a reference on a new ACL object. Return null if the
     *         ACL object could not be created.
     */
    public synchronized JahiaAcl createACL(JahiaAcl parent) {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(parent);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        manager.saveAcl(acl);
        return acl;
    }

    public synchronized JahiaAcl createACL(JahiaAcl parent, JahiaAcl pickedAcl) {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(parent);
        acl.setPickedAcl(pickedAcl);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        manager.saveAcl(acl);
        return acl;
    }

    /**
     * Create a new named JahiaACL object
     *
     * @param name   the name to use. Warning, no test is done on wether this
     *               name is already used or not. It is up to the users of this method to
     *               previously check for existence !
     * @param parent Reference on a parent JahiaACL object. Setting the
     *               parent reference object ot null, means the object will
     *               not be able to inherit access rights from a parent
     *               object.
     * @return a reference on a JahiaAclName object, that also contains the
     *         JahiaAcl object that was created for the name.
     */
    public synchronized JahiaAclName createACLName(String name, JahiaAcl parent) {
        JahiaAcl acl = new JahiaAcl();
        acl.setParent(parent);
        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
        manager.saveAcl(acl);
        JahiaAclName aclName = new JahiaAclName();
        aclName.setAclName(name);
        aclName.setAcl(acl);
        nameManager.saveAclName(aclName);
        return aclName;
    }


    /**
     * Return the specified ACL.
     *
     * @param aclID Unique identification number of the required ACL.
     * @return Return the reference on the requested ACL
     * @throws ACLNotFoundException Throws this excption if the specified ACL is not present in the
     *                              system.
     */
    public JahiaAcl lookupACL(int aclID)
            throws ACLNotFoundException {
        JahiaAcl result = (JahiaAcl) mACLCache.get(new Integer(aclID));
        if (result == null) {
            // not found in cache, let's try to load it from the database.
            try {
                result = manager.findJahiaAclById(String.valueOf(aclID));
                mACLCache.put(result.getId(), result);
            } catch (ObjectRetrievalFailureException je) {
                throw new ACLNotFoundException(aclID);
            }
        }
        return result;
    }

    /**
     * Lookup an named ACL.
     *
     * @param name the unique name
     * @return an JahiaAcl object if found.
     */
    public JahiaAcl lookupACL(String name) {
        JahiaAcl result = null;
        JahiaAclName aclName = nameManager.findJahiaAclNameByName(name);
        if (aclName != null) {
            result = aclName.getAcl();
        }
        return result;
    }

    /**
     * Remove the specified ACL from DB also from cache.
     *
     * @param acl the ACL to remove.
     * @return true if no problems.
     */
    public synchronized boolean deleteACL(JahiaAcl acl) {
        boolean result = false;
        try {
            manager.remove(acl.getId().toString());
            mACLCache.remove(acl.getId());
            result = true;
        } catch (ObjectRetrievalFailureException ex) {
            result = false;
        }

        return result;
    }

    /**
     * Preloads Container ACLs from database for a given page
     *
     * @param pageID the pageID
     * @throws JahiaException thrown if there was an error while loading ACLs
     *                        from the database.
     */
    public void preloadContainerACLsByPage(int pageID)
            throws JahiaException {
        if (!mPreloadedContainerACLsByPageCache
                .containsKey(String.valueOf(pageID))) {
            synchronized (this) {
                manager.preloadACLs(mACLCache);
                this.mPreloadedContainerACLsByPageCache.put(String
                        .valueOf(pageID), String.valueOf(pageID));
            }
        }
    }

    /**
     * Preloads field ACLs from database for a given page
     *
     * @param pageID the page ID
     * @throws JahiaException thrown if there was an error while loading ACLs from the
     *                        database.
     */
    public void preloadFieldACLsByPage(int pageID)
            throws JahiaException {
        if (!mPreloadedFieldACLsByPageCache
                .containsKey(String.valueOf(pageID))) {
            synchronized (this) {
                manager.preloadACLs(mACLCache);
                this.mPreloadedFieldACLsByPageCache.put(String.valueOf(pageID),
                        String.valueOf(pageID));
            }
        }
    }

    /**
     * Remove the specified user from all Jahia ACLs.
     *
     * @param user The user in question.
     * @return true if no problems.
     */
    public synchronized boolean removeUserFromAllACLs(JahiaUser user) {
        if (user == null) {
            return false;
        }

        boolean result = false;
        try {
            // remove the entries from the database
            manager.removeAclUserEntries(user.getUserKey());

            // The cache need to be flushed
            mACLCache.flush();
            result = true;

        } catch (Exception ex) {
            result = false;
            logger.warn(ex);
        }
        return result;
    }


    /**
     * Remove the specified group from all Jahia ACLs.
     *
     * @param group The group in question.
     * @return true if no problem.
     */
    public synchronized boolean removeGroupFromAllACLs(JahiaGroup group) {
        if (group == null) {
            return false;
        }

        boolean result = false;
        try {
            manager.removeAclGroupEntries(group.getGroupKey());

            // The cache need to be flushed
            mACLCache.flush();
            result = true;

        } catch (Exception ex) {
            result = false;
            logger.warn(ex);
        }

        return result;
    }

    /**
     * Checks to see if the given group is used by at least one ACLEntry.
     * (for example this is useful for checking if a group originating
     * from LDAP is used anywhere by Jahia)
     *
     * @param groupName the group name to search for
     * @return true if this group is used, false otherwise.
     */
    public boolean isGroupUsedInAclEntries(String groupName) {
        if (groupName == null || "".equals(groupName)) {
            return false;
        }

        try {
            return manager.isGroupUsedInAclEntries(groupName);
        } catch (Exception ex) {
            logger.warn(ex);
        }

        return false;
    }

    // NK
    /**
     * return a DOM document of requested acl and all parents and their own parents...
     *
     * @param ids         the list of acl ids
     * @param withParents if true, return parents too
     * @return JahiaDOMObject a DOM representation of this object
     * @throws JahiaException
     */
    public JahiaDOMObject getAclsAsDOM(List ids, boolean withParents)
            throws JahiaException {
        return null;
    }

    // NK
    /**
     * return a DOM document of requested acl entries
     *
     * @param ids the list of acl ids
     * @return JahiaDOMObject a DOM representation of this object
     * @throws JahiaException
     */
    public JahiaDOMObject getAclEntriesAsDOM(List ids)
            throws JahiaException {
        return null;
    }

    /**
     * This method updates the cache but ALSO persists modifications in the database.
     *
     * @param jahiaACL
     */
    public void updateCache(JahiaAcl jahiaACL) {
        manager.update(jahiaACL);
        mACLCache.flush();
    }

    /**
     * Test if we are authorized for a permission on an object. This method also checks within
     * the license if this action is authorized.
     * <p/>
     * For convenience, we also offer two other methods : getActionPermission and getSiteActionPermission
     * that simplify API usage of retrieving permissions for action and site-specific actions.
     *
     * @param objectName a String containing the name of the object (component) to check for
     *                   authorization.
     * @param p          a security principal (user or group) for which to check the permission
     * @param permission the permission, based on JahiaBaseACL constants to check, such as
     *                   read, write, administer, ...
     * @param siteID     the site on which to check the permissions, also used when the permission
     *                   doesn't yet exist and is created based on a default entry specified in the
     *                   applicationcontext-manager.xml init file, and a $SITE_ID variable is replaced with the
     *                   value passed here (only if siteID > 0).
     * @return -1 if the license doesn't authorize the permission, 0 if the ACL doesn't
     *         authorize the permission, and 1 if both the license and the ACL authorize the
     *         permission.
     */
    public int getPermission(String objectName, Principal p, int permission, int siteID, boolean siteAdminsHaveAllRights) {
        JahiaAclName jahiaAclName = nameManager.findOrCreateJahiaAclNameByName(objectName, siteID);
        JahiaAcl acl = jahiaAclName.getAcl();
        boolean allowedByACL;
        if (isGroup(p)) {
            allowedByACL = acl.getPermission((JahiaGroup) p, permission);
        } else {
            allowedByACL = acl.getPermission(null, null, (JahiaUser) p, permission, siteAdminsHaveAllRights, siteID);
        }
        // by default if the license doesn't prevent it it is authorized.
        boolean licenseAuthorized = LicenseActionChecker.isAuthorizedByLicense(objectName, siteID);
        if (licenseAuthorized) {
            if (allowedByACL) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public JahiaAclName getJahiaAclName(String objectName, int siteID) {
        return nameManager.findOrCreateJahiaAclNameByName(objectName, siteID);
    }


    /**
     * Shortcut method to test permissions on an action.
     *
     * @param actionName the name of the action to retrieve (or create) permission for. This
     *                   action will be prepended with {@link ManageServerPermissions#SERVER_PERMISSIONS_PREFIX} before passing it to getPermission.
     * @param p          a security principal (user or group) for which to check the permission
     * @param permission the permission, based on JahiaBaseACL constants to check, such as
     *                   read, write, administer, ...
     * @param siteID     the site on which to check the permissions, also used when the permission
     *                   doesn't yet exist and is created based on a default entry specified in the
     *                   applicationcontext-manager.xml init file, and a $SITE_ID variable is replaced with the
     *                   value passed here (only if siteID > 0).
     * @return -1 if the license doesn't authorize the permission, 0 if the ACL doesn't
     *         authorize the permission, and 1 if both the license and the ACL authorize the
     *         permission.
     */
    public int getServerActionPermission(String actionName, Principal p, int permission, int siteID) {
        return getPermission(ManageServerPermissions.SERVER_PERMISSIONS_PREFIX + actionName, p, permission, siteID, false);
    }

    /**
     * Shortcut method to test permissions on a site-specific action.
     *
     * @param actionName the name of the action to retrieve (or create) permission for. This
     *                   action will be prepended with "org.jahia.actions."+siteID+"." before passing it to
     *                   get permission.
     * @param p          a security principal (user or group) for which to check the permission
     * @param permission the permission, based on JahiaBaseACL constants to check, such as
     *                   read, write, administer, ...
     * @param siteID     the site on which to check the permissions, also used when the permission
     *                   doesn't yet exist and is created based on a default entry specified in the
     *                   applicationcontext-manager.xml init file, and a $SITE_ID variable is replaced with the
     *                   value passed here (only if siteID > 0).
     * @return -1 if the license doesn't authorize the permission, 0 if the ACL doesn't
     *         authorize the permission, and 1 if both the license and the ACL authorize the
     *         permission.
     */
    public int getSiteActionPermission(String actionName, Principal p, int permission, int siteID) {
        JahiaUserAliasing userAliasing = new JahiaUserAliasing((JahiaUser)p);
        return getPermission(ManageSitePermissions.SITE_PERMISSIONS_PREFIX + siteID + "." + actionName,
                userAliasing, permission, siteID, true);
    }

    public List getAclNamesStartingWith(String startWithStr) {
        return nameManager.findJahiaAclNamesStartingWith(startWithStr);
    }

    /**
     * List of permission names for a site grouped by key names that will be used
     * in the administration to check for existence. As these permissions are initially
     * created on first access, we can force the creation of a group of permissions by
     * using these lists.
     * This list is the site specific permission list, that may use a wildcard character
     * "*" that will be replaced by the site ID upon existence check.
     *
     * @param sitePermissionsMap a list of String values that are the names of the
     *                           permissions to check for existence. If a star character "*" is encountered, it
     *                           will be replaced with the value of the site ID.
     */
    public void setSitePermissionsMap(Map sitePermissionsMap) {
        this.sitePermissionsMap = sitePermissionsMap;
    }

    /*
    * List of permission names grouped by key names that will be used in the administration
    * to check for existence. As these permissions are initially created on first
    * access, we can force the creation of a group of permissions by using these lists.
    * Contrary to the sitePermissionsMap, this list is not site-specific, and does not
    * do any substitution of the "*" character.
    *
    * @param serverPermissionsMap a list of String values that are the names of the
    * permissions to check for existence.
    */
    public void setServerPermissionsMap(Map serverPermissionsMap) {
        this.serverPermissionsMap = serverPermissionsMap;
    }

    /**
     * Checks for the existence of the site-specific permissions specified in the
     * sitePermissionsMap structure. This is used to force the existence before
     * presenting the administration UI of these permissions.
     *
     * @param siteID the site for which to check the existence of the site-specific
     *               permissions.
     */
    public void checkSitePermissionsExistence(int siteID) {
        if (sitePermissionsMap == null) {
            return;
        }
        if (sitePermissionsMap.size() == 0) {
            return;
        }
        Iterator permissionGroupIter = sitePermissionsMap.entrySet().iterator();
        while (permissionGroupIter.hasNext()) {
            Map.Entry permissionGroupEntry = (Map.Entry) permissionGroupIter.next();
            List permissionGroupList = (List) permissionGroupEntry.getValue();
            Iterator permissionNameIter = permissionGroupList.iterator();
            while (permissionNameIter.hasNext()) {
                String curPermissionName = (String) permissionNameIter.next();
                String processedPermissionName = curPermissionName.replaceAll("\\*", Integer.toString(siteID));
                JahiaAclName jahiaAclName = nameManager.findOrCreateJahiaAclNameByName(processedPermissionName, siteID);
                if (jahiaAclName == null) {
                    logger.warn("Something went wrong when loading or creating ACL name" + processedPermissionName);
                }
            }
        }
    }

    /**
     * Checks for the existence of permissions specified in the
     * serverPermissionsMap structure. This is used to force the existence before
     * presenting the administration UI of these permissions.
     */
    public void checkServerPermissionsExistence() {
        if (serverPermissionsMap == null) {
            return;
        }
        if (serverPermissionsMap.size() == 0) {
            return;
        }
        Iterator permissionGroupIter = serverPermissionsMap.entrySet().iterator();
        while (permissionGroupIter.hasNext()) {
            Map.Entry permissionGroupEntry = (Map.Entry) permissionGroupIter.next();
            List permissionGroupList = (List) permissionGroupEntry.getValue();
            Iterator permissionNameIter = permissionGroupList.iterator();
            while (permissionNameIter.hasNext()) {
                String curPermissionName = (String) permissionNameIter.next();
                JahiaAclName jahiaAclName = nameManager.findOrCreateJahiaAclNameByName(curPermissionName, 0);
                if (jahiaAclName == null) {
                    logger.warn("Something went wrong when loading or creating ACL name" + curPermissionName);
                }
            }
        }
    }

    /**
     * Retrieve a group a ACL names as defined in the sitePermissionMap.
     *
     * @param groupName the name to retrieve, for example "administration",
     *                  "data", "tools", etc...
     * @param siteID    the site on which to retrieve the group
     * @return a List of JahiaAclName objects
     */
    public List getSitePermissionsGroup(String groupName, int siteID) {
        if (groupName == null) {
            return new ArrayList();
        }
        if (sitePermissionsMap == null) {
            return new ArrayList();
        }
        if (sitePermissionsMap.size() == 0) {
            return new ArrayList();
        }
        List permissionList = (List) sitePermissionsMap.get(groupName);
        if ("languages".equals(groupName)) {
            // build languages permission list based on the current site languages  
            try {
                List<Locale> locales = (List<Locale>) ServicesRegistry
                        .getInstance().getJahiaSitesService().getSite(siteID)
                        .getLanguageSettingsAsLocales(false);
                permissionList = new ArrayList(locales.size());
                for (Locale locale : locales) {
                    permissionList
                            .add(ManageSitePermissions.SITE_PERMISSIONS_PREFIX
                                    + "*.engines.languages." + locale);
                }
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve site language settings", e);
                permissionList = (List) sitePermissionsMap.get(groupName);
            }
        }
        if ("toolbars".equals(groupName)) {
            // build toolbars permission list based on the current site toolbars
            List<Toolbar> toolbars = ServicesRegistry.getInstance().getJahiaToolbarService().getToolbars();
            permissionList = new ArrayList(toolbars.size());
            for (Toolbar toolbar : toolbars) {
                permissionList.add(ManageSitePermissions.SITE_PERMISSIONS_PREFIX + "*." + toolbar.getVisibility().getSiteActionPermission());
            }

        }
        if (permissionList == null) {
            return new ArrayList();
        }
        List result = new ArrayList();
        Iterator permissionNameIter = permissionList.iterator();
        while (permissionNameIter.hasNext()) {
            String permissionName = (String) permissionNameIter.next();
            permissionName = JahiaTools.replacePattern(permissionName, "*", Integer.toString(siteID));
            JahiaAclName jahiaAclName = nameManager.findOrCreateJahiaAclNameByName(permissionName, siteID);
            if (jahiaAclName == null) {
                logger.warn("Couldn't load Jahia ACL by name " + permissionName + ", ignoring...");
                continue;
            }
            result.add(jahiaAclName);
        }
        return result;
    }

    /**
     * Retrieve of list of JahiaAclName objects as defined in the serverPermissionMap.
     *
     * @param groupName the name of the group to retrieve acl names for, for example
     *                  "administration"
     * @return a List of JahiaAclName objects.
     */
    public List getServerPermissionsGroup(String groupName) {
        if (groupName == null) {
            return new ArrayList();
        }
        if (serverPermissionsMap == null) {
            return new ArrayList();
        }
        if (serverPermissionsMap.size() == 0) {
            return new ArrayList();
        }
        List permissionList = (List) serverPermissionsMap.get(groupName);
        if (permissionList == null) {
            return new ArrayList();
        }
        List result = new ArrayList();
        Iterator permissionNameIter = permissionList.iterator();
        while (permissionNameIter.hasNext()) {
            String permissionName = (String) permissionNameIter.next();
            JahiaAclName jahiaAclName = nameManager.findJahiaAclNameByName(permissionName);
            if (jahiaAclName == null) {
                logger.warn("Couldn't load Jahia ACL by name " + permissionName + ", ignoring...");
                continue;
            }
            result.add(jahiaAclName);
        }
        return result;
    }

    public void flushCache() {
        manager.flushCache();
        nameManager.flushCache();
    }

    /**
     * returns true if the principal is a Group
     *
     * @param p reference to a JahiaUser or JahiaGroup.
     * @return boolean true if the Principal is a Group
     */
    private static boolean isGroup(Principal p) {

        return (p instanceof Group);
    }

    public boolean hasWriteAccesOnAllLangs(final ProcessingContext jParams) throws JahiaException {
        final List<SiteLanguageSettings> siteLangs = jParams.getSite().getLanguageSettings(true);
        boolean result = true;
        for (Iterator<SiteLanguageSettings> it = siteLangs.iterator(); it.hasNext();) {
            final String languageCode = ((SiteLanguageSettings) it.next()).getCode();
            result = result && getSiteActionPermission("engines.languages." +
                    languageCode,
                    jParams.getUser(),
                    JahiaBaseACL.READ_RIGHTS,
                    jParams.getSiteID()) > 0;
        }
        return result;
    }

    public Collection<String> getAllUsersInAcl() {
        return manager.findAllTarget(1);
    }

    public Collection<String> getAllGroupsInAcl() {
        return manager.findAllTarget(2);
    }

    public List<JahiaAcl> getChildAcls(int parentAclId) {
        return manager.getChildAcls(parentAclId);
    }

    public List<JahiaAcl> getChildAclsOnPage(List<Integer> parentAclIds, int pageId) {
        return manager.getChildAclsOnPage(parentAclIds, pageId);
    }
}
