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
package org.jahia.services.cache;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: rincevent Date: 5 févr. 2008 Time: 11:29:39 To change this template use File |
 * Settings | File Templates.
 */
public class CacheKeyGeneratorService extends JahiaService {
    public static final String USERNAME_PREFIX = "USERNAME-";
    public static final String LANGUAGECODE_PREFIX = "LANGUAGECODE-";
    public static final String WORKFLOWSTATE_PREFIX = "WORKFLOWSTATE-";
    public static final String SITE_PREFIX = "SITE-";
    public static final String USERKEY_CACHE_NAME="UserKeyCache";
    private SortedMap<String, JahiaGroup> groups;
    private SortedSet<String> users;
    private JahiaACLManagerService jahiaACLManagerService;
    private JahiaGroupManagerService groupManagerService;
    private static CacheKeyGeneratorService instance;
    private CacheService cacheService;
    private Cache<String, String> userKeyCache;

    public void setJahiaACLManagerService(JahiaACLManagerService jahiaACLManagerService) {
        this.jahiaACLManagerService = jahiaACLManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void start() throws JahiaInitializationException {
        groups = new TreeMap<String, JahiaGroup>();
        Collection<String> names = jahiaACLManagerService.getAllGroupsInAcl();
        for (String name : names) {
            JahiaGroup g = groupManagerService.lookupGroup(name);
            groups.put(name, g);
        }

        users = new TreeSet<String>(jahiaACLManagerService.getAllUsersInAcl());
        userKeyCache = cacheService.createCacheInstance(USERKEY_CACHE_NAME);
    }

    public void stop() throws JahiaException {
    }

    public void rightsUpdated() throws JahiaInitializationException {
        SortedSet<String> newusers = new TreeSet<String>(jahiaACLManagerService.getAllUsersInAcl());
        final CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
        ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> containerHTMLCache = cacheService.getContainerHTMLCacheInstance();
        SkeletonCache skeletonCache = cacheService.getSkeletonCacheInstance();
        if (!newusers.equals(users)) {
            Set<String> removedUsers = new HashSet<String>(users);
            removedUsers.removeAll(newusers);

            users = newusers;

            for (String removedUser : removedUsers) {
                String s = USERNAME_PREFIX + removedUser;
                containerHTMLCache.flushGroup(s);
                skeletonCache.flushGroup(s);
            }
            userKeyCache.flush();
        }

        SortedSet<String> newgroupskeys = new TreeSet<String>(jahiaACLManagerService.getAllGroupsInAcl());
        if (!newgroupskeys.equals(groups.keySet())) {
            SortedMap<String, JahiaGroup> newgroups = new TreeMap<String, JahiaGroup>();
            for (String name : newgroupskeys) {
                JahiaGroup g = groupManagerService.lookupGroup(name);
                newgroups.put(name, g);
            }

            groups = newgroups;

            containerHTMLCache.flush();
            skeletonCache.flush();
            userKeyCache.flush();
        }
    }

    public String getUserCacheKey(JahiaUser user, int siteID) {
        String usercachekey;
        final String s = user.getUserKey() + SITE_PREFIX + siteID;
        if (!AdvPreviewSettings.isInUserAliasingMode() && userKeyCache.containsKey(s)) {
            return userKeyCache.get(s);
        }
        Collection<String> users = getAllUsers();

        if (users.contains(user.getUserKey())) {
            usercachekey = user.getUserKey();
        } else {
            Collection<JahiaGroup> groups = getAllGroups();
            StringBuffer b = new StringBuffer();
            for (JahiaGroup g : groups) {
                if (g != null && (g.getSiteID() == siteID || g.getSiteID() == 0) && g.isMember(user)) {
                    if(b.length()>0)b.append("|");
                    b.append(g.getGroupname());
                }
            }
            usercachekey = b.toString();
            if(usercachekey.equals(JahiaGroupManagerService.GUEST_GROUPNAME) && !user.getUsername().equals(JahiaUserManagerService.GUEST_USERNAME)) {
                usercachekey = b.append("|"+JahiaGroupManagerService.USERS_GROUPNAME).toString();
            }
        }

        // useraliasing mode
        if (AdvPreviewSettings.isInUserAliasingMode()) {
            usercachekey += "_" + AdvPreviewSettings.getThreadLocaleInstance().getAliasedUser().getUserKey();
        } else {
            CacheEntry<String> cacheEntry = new CacheEntry<String>(usercachekey);
            cacheEntry.setExpirationDate(new Date(System.currentTimeMillis() + (300 * 1000)));
            userKeyCache.putCacheEntry(s,cacheEntry, true);
        }
        return usercachekey;
    }


    private Collection<JahiaGroup> getAllGroups() {
        return groups.values();
    }

    private Collection<String> getAllUsers() {
        return users;

    }

    /**
     * <p>Builds the cache key that is used to reference the cache entries in the lookup table.</p>
     *
     * @param container    the container identification number
     * @param cacheKey     the cacheKey
     * @param user         the user name
     * @param languageCode the language code
     * @param mode         the mode
     * @param scheme       the request scheme (http/https)
     * @return the generated cache key
     */
    public GroupCacheKey computeContainerEntryKey(JahiaContainer container,
                                                  String cacheKey,
                                                  JahiaUser user,
                                                  String languageCode,
                                                  String mode,
                                                  String scheme) {
        int id = 0;
        if (container != null)
            id = container.getID();
        String usercachekey = getUserCacheKey(user, Jahia.getThreadParamBean().getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, cacheKey, scheme);

        return new GroupCacheKey(key, new HashSet<String>());
    }
    public GroupCacheKey computeContainerEntryKeyWithGroups(JahiaContainer container,
                                                            String group,
                                                            JahiaUser user,
                                                            String languageCode,
                                                            String mode,
                                                            String scheme,
                                                            Set<ContentObjectKey> dependencies) {
        int id = 0;
        if (container != null)
            id = container.getID();
        String containerkey = new ContentContainerKey(id).toString();
        String usercachekey = getUserCacheKey(user, Jahia.getThreadParamBean().getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, group, scheme);

        Set<String> groups = new HashSet<String>();
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            groups.add(containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            groups.add(USERNAME_PREFIX + usercachekey);
            groups.add(SITE_PREFIX + Jahia.getThreadParamBean().getSiteID());
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            }
        } else {
            groups.add(Integer.toString((containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            groups.add(Integer.toString((USERNAME_PREFIX + usercachekey).hashCode()));
            groups.add(Integer.toString((SITE_PREFIX + Jahia.getThreadParamBean().getSiteID()).hashCode()));
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(Integer.toString((objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            }
        }
        return new GroupCacheKey(key, groups);
    }

    /**
     * <p>Builds the cache key that is used to reference the cache entries in the lookup table.</p>
     *
     * @param skeleton     the skeleton identification number
     * @param cacheKey     the cacheKey
     * @param user         the user name
     * @param languageCode the language code
     * @param mode         the mode
     * @param scheme       the request scheme (http/https)
     * @return the generated cache key
     */
    public GroupCacheKey computeSkeletonEntryKey(JahiaPage skeleton,
                                                 String cacheKey,
                                                 JahiaUser user,
                                                 String languageCode,
                                                 String mode,
                                                 String scheme) {
        int id = skeleton.getID();
        String usercachekey = getUserCacheKey(user, skeleton.getSiteID());

        Object key = getKey(id, mode, languageCode, usercachekey, cacheKey, scheme);

        return new GroupCacheKey(key, new HashSet<String>());
    }

    public GroupCacheKey computeSkeletonEntryKeyWithGroups(JahiaPage skeleton,
                                                           String group,
                                                           JahiaUser user,
                                                           String languageCode,
                                                           String mode,
                                                           String scheme,
                                                           Set<ContentObjectKey> dependencies) {
        int id = skeleton.getID();
        String containerkey = new ContentPageKey(id).toString();
        final int siteID = skeleton.getSiteID();
        String usercachekey = getUserCacheKey(user, siteID);

        Object key = getKey(id, mode, languageCode, usercachekey, group, scheme);

        Set<String> groups = new HashSet<String>();
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            groups.add(containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            groups.add(USERNAME_PREFIX + usercachekey);
            groups.add(SITE_PREFIX + siteID);
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode);
            }
        } else {
            groups.add(Integer.toString((containerkey + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            groups.add(Integer.toString((USERNAME_PREFIX + usercachekey).hashCode()));
            groups.add(Integer.toString((SITE_PREFIX + siteID).hashCode()));
            for (ContentObjectKey objectKey : dependencies) {
                groups.add(Integer.toString((objectKey.toString() + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode()));
            }
        }
        return new GroupCacheKey(key, groups);
    }

    private Object getKey(int id, String mode, String languageCode, String usercachekey, String group, String scheme) {
        String key = id + "-" + mode + "-" + languageCode + "-" + usercachekey;
        if (group != null) {
            key += "-" + group;
        }
        if (!"http".equals(scheme)) {
            key += "-" + scheme;
        }
        if (SettingsBean.getInstance().isDevelopmentMode()) {
         return key;  
        } else {
        return key.hashCode();
        }
    }

    public String getPageKey(String containerID, String mode, String languageCode) {
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            return containerID + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode;
        } else {
            return Integer.toString((containerID + WORKFLOWSTATE_PREFIX + mode + LANGUAGECODE_PREFIX + languageCode).hashCode());
        }
    }

    /**
     * Returns an instance of the service class
     *
     * @return the unique instance of this class
     * @throws org.jahia.exceptions.JahiaException in case of error
     */
    public static synchronized CacheKeyGeneratorService getInstance()
            throws JahiaException {
        if (instance == null) {
            instance = new CacheKeyGeneratorService();
        }
        return instance;
    }

    public GroupCacheKey computeContainerEntryKey(int ctnid, String group, JahiaUser user, String languageCode,
                                                  String operationMode, String scheme, int siteID) {
        String usercachekey = getUserCacheKey(user, siteID);

        Object key = getKey(ctnid, operationMode, languageCode, usercachekey, group, scheme);

        return new GroupCacheKey(key, new HashSet<String>());
    }
}
