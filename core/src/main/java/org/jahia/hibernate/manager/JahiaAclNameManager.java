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
package org.jahia.hibernate.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaAclDAO;
import org.jahia.hibernate.dao.JahiaAclNamesDAO;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclEntryPK;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.utils.JahiaTools;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: Serge Huber Date: 14 d�c. 2005 Time: 14:52:14 Copyright (C) Jahia Inc.
 */
public class JahiaAclNameManager {
    public static final String JAHIA_ACL_NAMES = "AclNames";

    private Log log = LogFactory.getLog(JahiaAclNameManager.class);

    private JahiaAclDAO aclDao = null;
    private JahiaAclNamesDAO aclNamesDao = null;

    private Map<String, Map<String, String>> aclNamesDefaultEntries;

    private Cache<String, JahiaAclName> cache;

    private void checkCache() {
        if (cache == null) cache = ServicesRegistry.getInstance().getCacheService().getCache(JAHIA_ACL_NAMES);
        if (cache == null) try {
            cache = ServicesRegistry.getInstance().getCacheService().createCacheInstance(JAHIA_ACL_NAMES);
        } catch (JahiaInitializationException e) {
            log.error(e);
        }
    }

    public void setAclDao(JahiaAclDAO aclDao) {
        this.aclDao = aclDao;
    }

    public void setAclNamesDao(JahiaAclNamesDAO aclNamesDao) {
        this.aclNamesDao = aclNamesDao;
    }

    public void setAclNamesDefaultEntries(Map<String, Map<String, String>> aclNamesDefaultEntries) {
        this.aclNamesDefaultEntries = aclNamesDefaultEntries;
    }

    /**
     * This is a powerful method, that not only tries to lookup an acl name, but also creates it if default entries
     * exist. Default entries are also looked up recursively to parent definition, using a "." seperator. Basically if
     * we lookup the acl name : org.jahia.admin.ManageUsers.addUser.sitekey_defaultSite the lookup will be done in the
     * following way : org.jahia.admin.ManageUsers.addUser.sitekey_defaultSite org.jahia.admin.ManageUsers.addUser
     * org.jahia.admin.ManageUsers org.jahia.admin org.jahia org
     *
     * @param aclName the ACL name to lookup, or to create if it doesn't exist yet
     * @param siteID  if >0, will be used to replace * markers used in principal names in the default ACL entries to
     *                specify the site on which the users/groups will be used.
     *
     * @return the ACL name object that was found or created.
     */
    public synchronized JahiaAclName findOrCreateJahiaAclNameByName(String aclName, int siteID) {
        if (aclName != null) {
            try {
                if (!"".equals(aclName)) {
                    checkCache();

                    JahiaAclName aclNameResult = cache.get(aclName);
                    if (aclNameResult != null) {
                        return aclNameResult;
                    }

                    try {
                        aclNameResult = aclNamesDao.findAclNameByName(aclName);
                    } catch (ObjectRetrievalFailureException orfe) {
                        // this is expected, the name might not yet exist.
                    }
                    if (aclNameResult != null) {
                        cache.put(aclName, aclNameResult);
                        return aclNameResult;
                    }
                    // we didn't find the name, is there a default set of entries
                    // defined for it, in which case we will build it.
                    // first we lookup recursively to find either a definition for
                    // the acl name, or for any of it's parents.
                    String curAclName = aclName;
                    if (siteID > 0) {
                        // if there is a siteID, we replace it with the "*" character
                        // before looking for defaults.
                        curAclName = JahiaTools.replacePattern(curAclName, Integer.toString(siteID), "*");
                    }
                    boolean found = false;
                    while ((!"".equals(curAclName)) && (!found)) {
                        if (aclNamesDefaultEntries.get(curAclName) == null) {
                            // no default entry found for this name, let's resolve
                            // the parent's name and look in there.
                            int lastDotPos = curAclName.lastIndexOf(".");
                            if (lastDotPos > -1) {
                                curAclName = curAclName.substring(0, lastDotPos);
                            }
                            else {
                                // no more dot separators, we stop searching here
                                break;
                            }
                        }
                        else {
                            // we have found a default entry set definition, let's
                            // stop the searching here.
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // no default entries could be found, so we create the
                        // acl name anyway, but without any default entries.
                        JahiaAcl acl = new JahiaAcl();
                        acl.setParent(null);
                        acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
                        aclDao.saveAcl(acl);
                        JahiaAclName jahiaAclName = new JahiaAclName();
                        jahiaAclName.setAclName(aclName);
                        jahiaAclName.setAcl(acl);
                        saveAclName(jahiaAclName);
                        return jahiaAclName;
                    }
                    // ok we found a definition either for this name or in a parent
                    // name, let's use it to create the aclName.
                    JahiaAcl acl = new JahiaAcl();
                    acl.setParent(null);
                    acl.setInheritance(new Integer(ACLInfo.INHERITANCE));
                    // todo : we still have to insert default entries here.
                    Map<String, String> defaultEntries = aclNamesDefaultEntries.get(curAclName);
                    Map<String, JahiaAclEntry> userAclEntries = new HashMap<String, JahiaAclEntry>();
                    Map<String, JahiaAclEntry> groupAclEntries = new HashMap<String, JahiaAclEntry>();
                    Iterator<Map.Entry<String, String>> entryIterator = defaultEntries.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry<String, String> curEntry = (Map.Entry<String, String>) entryIterator.next();
                        String key = (String) curEntry.getKey();
                        Integer userType = null;
                        String principalName = null;
                        if (key.startsWith("g:")) {
                            userType = new Integer(ACLInfo.GROUP_TYPE_ENTRY);
                            principalName = key.substring("g:".length());
                        }
                        else if (key.startsWith("u:")) {
                            userType = new Integer(ACLInfo.USER_TYPE_ENTRY);
                            principalName = key.substring("u:".length());
                        }
                        else {
                            log.warn("Invalid principal name found : " + key + ", ignoring default entry");
                            continue;
                        }
                        if (siteID > 0) {
                            principalName = principalName.replaceAll("\\*", Integer.toString(siteID));
                        }
                        JahiaAclEntry aclEntry =
                                new JahiaAclEntry(new JahiaAclEntryPK(acl, userType, principalName), 0, 0);
                        String permissionStr = ((String) curEntry.getValue()).toLowerCase();
                        if (permissionStr.indexOf("r") != -1) {
                            aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
                        }
                        if (permissionStr.indexOf("w") != -1) {
                            aclEntry.setPermission(JahiaBaseACL.WRITE_RIGHTS, JahiaAclEntry.ACL_YES);
                        }
                        if (permissionStr.indexOf("a") != -1) {
                            aclEntry.setPermission(JahiaBaseACL.ADMIN_RIGHTS, JahiaAclEntry.ACL_YES);
                        }
                        if (userType.intValue() == ACLInfo.GROUP_TYPE_ENTRY) {
                            groupAclEntries.put(principalName, aclEntry);
                        }
                        else {
                            userAclEntries.put(principalName, aclEntry);
                        }
                    }
                    if (userAclEntries.size() > 0) {
                        acl.setUserEntries(userAclEntries);
                    }
                    if (groupAclEntries.size() > 0) {
                        acl.setGroupEntries(groupAclEntries);
                    }
                    aclDao.saveAcl(acl);
                    JahiaAclName jahiaAclName = new JahiaAclName();
                    jahiaAclName.setAclName(aclName);
                    jahiaAclName.setAcl(acl);
                    saveAclName(jahiaAclName);
                    return jahiaAclName;
                }
            } catch (NumberFormatException e) {
                log.warn("Try to find an acl with passing an empty string " + aclName, e);
            }
        }
        throw new ObjectRetrievalFailureException(JahiaAcl.class, aclName);
    }

    public JahiaAclName findJahiaAclNameByName(String aclName) {
        if (aclName != null) {
            try {
                if (!"".equals(aclName)) {
                    checkCache();

                    JahiaAclName aclNameResult = cache.get(aclName);
                    if (aclNameResult != null) {
                        return aclNameResult;
                    }

                    aclNameResult = aclNamesDao.findAclNameByName(aclName);
                    if (aclNameResult != null) {
                        cache.put(aclName, aclNameResult);
                        return aclNameResult;
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("Try to find an acl with passing an empty string " + aclName, e);
            }
        }
        throw new ObjectRetrievalFailureException(JahiaAcl.class, aclName);
    }

    public List<JahiaAclName> findJahiaAclNamesStartingWith(String aclName) {
        if (aclName != null) {
            try {
                if (!"".equals(aclName)) {
                    return aclNamesDao.findAclNamesStartingWith(aclName);
                }
            } catch (NumberFormatException e) {
                log.warn("Try to find an acl with passing an empty string " + aclName, e);
            }
        }
        throw new ObjectRetrievalFailureException(JahiaAcl.class, aclName);
    }

    public void saveAclName(JahiaAclName jahiaAclName) {
        aclNamesDao.saveAclName(jahiaAclName);
        cache.put(jahiaAclName.getAclName(), jahiaAclName);
    }

    public void updateAclName(JahiaAclName jahiaAclName) {
        aclNamesDao.updateAclName(jahiaAclName);
        cache.put(jahiaAclName.getAclName(), jahiaAclName);
    }

    public void remove(String aclName) {
        JahiaAclName jahiaAclName = findJahiaAclNameByName(aclName);
        Integer aclId = new Integer(jahiaAclName.getAcl().getAclID());
        aclNamesDao.removeAclName(aclName);
        aclDao.removeAcl(aclId);
        cache.remove(jahiaAclName.getAclName());
    }

    /**
     * Copy of the method from JahiaAclManager
     */
    public void flushCache() {
        ServicesRegistry instance = ServicesRegistry.getInstance();
        if (instance != null) {
            CacheService cacheService = instance.getCacheService();
            if (cacheService != null) {
                cache = cacheService.getCache(JahiaAclNameManager.JAHIA_ACL_NAMES);
                if (cache != null) cache.flush(true);
            }
        }
    }
}
