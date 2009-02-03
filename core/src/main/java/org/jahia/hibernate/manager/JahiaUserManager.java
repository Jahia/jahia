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

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaUserDAO;
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

