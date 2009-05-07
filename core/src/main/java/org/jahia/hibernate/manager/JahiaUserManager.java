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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaUserDAO;
import org.jahia.hibernate.dao.JahiaAclDAO;
import org.jahia.hibernate.model.JahiaSitesUser;
import org.jahia.hibernate.model.JahiaSitesUserPK;
import org.jahia.hibernate.model.JahiaUserProp;
import org.jahia.hibernate.model.JahiaUserPropPK;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.usermanager.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 31 mars 2005
 * Time: 11:16:17
 * To change this template use File | Settings | File Templates.
 */
public class JahiaUserManager {
// ------------------------------ FIELDS ------------------------------
    public static final String USER_MANAGER_CACHE = "JahiaUserManagerCache";
    private static Logger log = Logger.getLogger(JahiaUserManager.class);

    private CacheService cacheService = null;
    private Cache<String, JahiaUser> cache;

    private JahiaUserDAO userDAO = null;
    private JahiaAclDAO aclDAO = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getNumberOfUsers() {
        final Integer numberOfUsers = userDAO.getNumberOfUsers();
        int i = 0;
        if (numberOfUsers != null) i = numberOfUsers.intValue();
        return i;
    }

    public List<String> getUserkeyList() {
        return userDAO.getUserkeyList();
    }

    public void setJahiaUserDAO(JahiaUserDAO dao) {
        this.userDAO = dao;
    }

    public void setJahiaAclDAO(JahiaAclDAO aclDAO) {
        this.aclDAO = aclDAO;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public Cache<String, JahiaUser> getCache() {
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(USER_MANAGER_CACHE);
            } catch (JahiaInitializationException e) {
                log.warn("Error could not initialize cache for " + USER_MANAGER_CACHE, e);
            }
        }
        return cache;
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean addMemberToSite(int siteID, JahiaUser user) {
        org.jahia.hibernate.model.JahiaUser jahiaUser = userDAO.loadJahiaUserByUserKey(user.getUserKey());
        if (jahiaUser != null) {
            userDAO.addMemberToSite(new JahiaSitesUser(new JahiaSitesUserPK(user.getUsername(), new Integer(siteID)), jahiaUser));
            if (getCache() != null) {
                cache.put("m"+siteID+"_"+user, user);
            }
            return true;
        }
        return false;
    }

    public boolean addProperty(String key, String value, int id, String providerName, String userKey) {
        try {
            JahiaUserProp prop = new JahiaUserProp(new JahiaUserPropPK(new Integer(id), key, providerName, userKey),
                                                   value);
            userDAO.saveProperty(prop);
            String[] userKeys = userKey.split(":");
            if(userKeys!=null && userKeys.length==2)
            getCache().remove("m"+userKeys[1]+"_"+userKeys[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createUser(JahiaUser user, String password) {
        org.jahia.hibernate.model.JahiaUser jahiaUser = new org.jahia.hibernate.model.JahiaUser();
        jahiaUser.setKey(user.getUserKey());
        jahiaUser.setName(user.getUsername());
        jahiaUser.setPassword(password);
        userDAO.save(jahiaUser, user.getProviderName(), user.getProperties());
    }

    public boolean deleteUser(JahiaUser user) {
        try {
            userDAO.delete(user.getUserKey());
            aclDAO.removeUserAclEntries(user.getUserKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public JahiaUser findJahiaUserByUsername(String name) {
        org.jahia.hibernate.model.JahiaUser jahiaUser = null;
        jahiaUser = userDAO.loadJahiaUserByName(name);
        JahiaUser user = null;
        if (jahiaUser != null) {
            Properties properties = userDAO.loadProperties(jahiaUser.getId(), jahiaUser.getKey(),
                                                           JahiaUserManagerDBProvider.PROVIDER_NAME);
            user = new JahiaDBUser(jahiaUser.getId().intValue(),
                                   jahiaUser.getName(), jahiaUser.getPassword(), jahiaUser.getKey(),
                    new UserProperties(properties,false));
        }
        return user;
    }

    public JahiaUser findJahiaUserByUserKey(String userKey) {
        String userKeyS = userKey;
        if(userKeyS.startsWith("root:")) {
            userKeyS = "root:0";
        }
        org.jahia.hibernate.model.JahiaUser jahiaUser = userDAO.loadJahiaUserByUserKey(userKeyS);
        JahiaUser user = null;
        if (jahiaUser != null) {
            Properties properties = userDAO.loadProperties(jahiaUser.getId(), jahiaUser.getKey(),
                                                           JahiaUserManagerDBProvider.PROVIDER_NAME);
            user = new JahiaDBUser(jahiaUser.getId().intValue(),
                                   jahiaUser.getName(), jahiaUser.getPassword(), jahiaUser.getKey(),
                    new UserProperties(properties,false));
        }
        return user;
    }

    public Map<String, String> getAllMembersNameOfSite(int siteID) {
        List<JahiaSitesUser> list = userDAO.getAllMembersNameOfSite(siteID > 0 ? new Integer(siteID) : null);
        FastHashMap map = new FastHashMap(list.size());
        for (JahiaSitesUser user : list) {
            map.put(user.getComp_id().getUsername(), user.getUser().getKey());
        }
        map.setFast(true);
        return map;
    }

    public List<JahiaUser> getAllMembersOfSite(int siteID) {
        List<org.jahia.hibernate.model.JahiaUser> allMembersOfSite = userDAO.getAllMembersOfSite(siteID > 0 ? new Integer(siteID) : null);
        FastArrayList retList = new FastArrayList(allMembersOfSite.size());
        for (org.jahia.hibernate.model.JahiaUser jahiaUser : allMembersOfSite) {
            JahiaUser user = null;
            if (jahiaUser != null) {
                Properties properties = userDAO.loadProperties(jahiaUser.getId(), jahiaUser.getKey(),
                                                               JahiaUserManagerDBProvider.PROVIDER_NAME);
                user = new JahiaDBUser(jahiaUser.getId().intValue(),
                                       jahiaUser.getName(), jahiaUser.getPassword(), jahiaUser.getKey(),
                        new UserProperties(properties,false));
            }
            if (user != null) retList.add(user);
        }
        retList.setFast(true);
        return retList;
    }

    public JahiaUser getMemberInSite(int siteID, String username) {
        JahiaUser user = null;
        if (getCache() != null) {
            if (cache.containsKey("m"+siteID+"_"+username)) {
                user = cache.get("m"+siteID+"_"+username);
                if (user != null) {
                    return user;
                }
            }
        }
        String name = userDAO.getMemberNameInSite(new Integer(siteID), username);
        if (name != null) {
            user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(name);
        }
        if (getCache() != null) {
            cache.put("m"+siteID+"_"+username, user);
        }
        return user;
    }

    public UserProperties getUserProperties(int id, String providerName, String usingUserKey) {
        Properties properties = userDAO.loadProperties(new Integer(id), usingUserKey, providerName);
        UserProperties userProperties = new UserProperties(properties, false);
        return userProperties;
    }

    public List<String> getUsernameList() {
        return userDAO.getUsernameList();
    }

    public void removeMemberFromAllSite(JahiaUser user) {
        userDAO.removeMemberFromAllSite(user.getUserKey());
        getCache().flush();
    }

    public void removeMemberFromSite(int siteID, JahiaUser user) {
        userDAO.removeMemberFromSite(new Integer(siteID), user.getUsername());
        getCache().remove("m"+siteID+"_"+user.getUsername());
        if (userDAO.getUserSiteMembershipCount(user.getUsername())==0) {
            deleteUser(user);
        }

    }

    public List<Integer> getUserSiteMembership(JahiaUser user) {
        List<Integer> res = new ArrayList<Integer>();
        List<JahiaSitesUser> l = userDAO.getUserSiteMembership(user.getUsername());
        for (JahiaSitesUser jahiaSitesUser : l) {
            res.add(jahiaSitesUser.getComp_id().getSiteId());
        }
        return res;
    }

    public boolean removeProperty(String key, int id, String providerName, String userKey) {
        try {
            JahiaUserProp prop = userDAO.getProperty(new JahiaUserPropPK(new Integer(id), key, providerName, userKey));
            userDAO.deleteProperty(prop);
            String[] userKeys = userKey.split(":");
            if(userKeys!=null && userKeys.length==2)
            getCache().remove("m"+userKeys[1]+"_"+userKeys[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void searchUserName(String curCriteriaValue, Set<String> userKeys) {
        List<String> list = userDAO.searchUserName(curCriteriaValue);
        userKeys.addAll(list);
    }

    public void searchUserName(List<String> criteriaNameList, List<String> criteriaValueList, Set<String> userKeys, String providerName) {
        List<String> list = userDAO.searchUserName(criteriaNameList, criteriaValueList, providerName);
        userKeys.addAll(list);
    }

    public boolean setPassword(String tmp, int id) {
        try {
            org.jahia.hibernate.model.JahiaUser jahiaUser = userDAO.loadJahiaUserById(new Integer(id));
            jahiaUser.setPassword(tmp);
            userDAO.update(jahiaUser);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateProperty(String key, String value, int id, String providerName, String userKey) {
        try {
            JahiaUserProp prop = userDAO.getProperty(new JahiaUserPropPK(new Integer(id), key, providerName, userKey));
            prop.setValue(value);
            userDAO.updateProperty(prop);
            String[] userKeys = userKey.split(":");
            if(userKeys!=null && userKeys.length==2)
            getCache().remove("m"+userKeys[1]+"_"+userKeys[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

