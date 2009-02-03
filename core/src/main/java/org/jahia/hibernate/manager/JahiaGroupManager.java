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

import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaGroupAccessDAO;
import org.jahia.hibernate.dao.JahiaGroupDAO;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.dao.JahiaUserDAO;
import org.jahia.hibernate.model.*;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.usermanager.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.spring.advice.CacheAdvice;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 16:20:59
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGroupManager {
// ------------------------------ FIELDS ------------------------------
    private static final String CACHE_NAME = "JahiaGroupManagerCache";
    private static final String CACHE_KEY_GROUPPREFIX = "JahiaGroup_";
    private static final String CACHE_KEY_SITEPREFIX = "JahiaSite_";
    private JahiaGroupAccessDAO accessDAO = null;
    private JahiaGroupDAO dao = null;
    private JahiaSiteDAO siteDAO = null;
    private CacheService cacheService = null;
    private Logger log = Logger.getLogger(JahiaGroupManager.class);
    private Cache cache = null;
    private Cache membership = null;

    private JahiaUserManagerService userService;

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<String> getGroupKeys() {
        return dao.getGroupKeys();
    }

    public List<String> getGroupNames() {
        return dao.getGroupNames();
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaGroupAccessDAO(JahiaGroupAccessDAO dao) {
        this.accessDAO = dao;
    }

    public void setJahiaGroupDAO(JahiaGroupDAO dao) {
        this.dao = dao;
    }

    public void setJahiaSiteDAO(JahiaSiteDAO dao) {
        this.siteDAO = dao;
    }

    public void setJahiaUserDAO(JahiaUserDAO dao) {
        // do nothing
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    public boolean addGroupToSite(int siteID, JahiaGroup grp) {
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_SITEPREFIX + siteID);
        }
        if (membership != null) {
            membership.flush();
        }
        JahiaSite jahiaSite = null;
        JahiaGrp grpidSitesGrps = null;
        if (siteID > 0) {
            Integer siteid = new Integer(siteID);
            jahiaSite = siteDAO.findById(siteid);
            grpidSitesGrps = dao.loadJahiaGroupBySiteAndName(siteid,
                                                             grp.getGroupname());
        } else {
            grpidSitesGrps = dao.loadJahiaGroupByName(grp.getGroupname());
        }
        if (grpidSitesGrps != null) {
            dao.addGroupToSite(new JahiaSitesGrp(new JahiaSitesGrpPK(grp.getGroupname(), jahiaSite), grpidSitesGrps));
            return true;
        }
        return false;
    }

    public void addMemberToGroup(String memberName, String groupName, int type) {
        JahiaGrpAccess access = new JahiaGrpAccess(new JahiaGrpAccessPK(memberName, groupName, new Integer(type)));
        accessDAO.save(access);
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_GROUPPREFIX + groupName);
        }
        if (membership != null) {
            membership.flush();
        }
    }

    public boolean addProperty(String key, String value, int id, String providerName, String groupKey) {
        try {
            JahiaGrpProp prop = new JahiaGrpProp(new JahiaGrpPropPK(new Integer(id), key, providerName, groupKey),
                                                 value);
            dao.saveProperty(prop);
            if (cache != null) {
                cache.flushGroup(CACHE_KEY_GROUPPREFIX + groupKey);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createGroup(JahiaGroup group) {
        JahiaGrp jahiaGrp = new JahiaGrp();
        jahiaGrp.setKey(group.getGroupKey());
        jahiaGrp.setName(group.getGroupname());
        if (group.getSiteID() > 0) {
            jahiaGrp.setSite(siteDAO.findById(new Integer(group.getSiteID())));
        }
        dao.save(jahiaGrp, group.getProviderName(), group.getProperties());
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_SITEPREFIX + group.getSiteID());
        }
        if (membership != null) {
            membership.flush();
        }
    }

    public boolean deleteGroup(JahiaGroup group) {
        try {
            deleteGroup(group.getGroupKey(), group.getSiteID());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteGroup(String key, Integer siteId) {
        dao.delete(key);
        accessDAO.delete(key);
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_SITEPREFIX + siteId);
        }
        if (membership != null) {
            membership.flush();
        }
    }

    public JahiaGroup findGroupBySiteAndName(int siteID, String name) {
        JahiaGrp jahiaGrp;
        JahiaGroup group = null;
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache for group manager", e);
            }
        }
        if (cache != null) {
            group = (JahiaGroup) cache.get(CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_GROUPPREFIX + name + ":" + siteID,
                                                                                    CACHE_KEY_SITEPREFIX + siteID}));
        }
        if (group == null) {
            if (siteID > 0) {
                jahiaGrp = dao.loadJahiaGroupBySiteAndName(new Integer(siteID), name);
            } else {
                jahiaGrp = dao.loadJahiaGroupByName(name);
            }

            if (jahiaGrp != null) {
                // Find Members                
                Map<String, Principal> members = null;
                boolean shouldPreload =
                       org.jahia.settings.SettingsBean.getInstance()
                        .isPreloadDBGroupMembersActivated()
                        ||
                                JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME
                                .equals(jahiaGrp.getName());
//                boolean shouldPreload = false;
                if (shouldPreload) {
                    members = getGroupMembers(jahiaGrp);
                } else {
                    members = new ConcurrentHashMap<String, Principal>();
                }

                // Load Properties
                Properties properties = dao.loadProperties(jahiaGrp.getId(), jahiaGrp.getKey(),
                                                           JahiaGroupManagerDBProvider.PROVIDER_NAME);

                group = getJahiaGroup(jahiaGrp, siteID, members, properties, shouldPreload);
                if (cache != null) {
                    cache.put(CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_GROUPPREFIX + name + ":" + siteID,
                                                                       CACHE_KEY_SITEPREFIX + siteID}), group);
                }
            }
        }
        return group;
    }

    public JahiaGroup findJahiaGroupByGroupKey(String groupKey) {
        JahiaGroup group = null;
        GroupCacheKey entryKey = null;
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.warn("Could not initialize cache for group manager", e);
            }
        }
        if (cache != null) {
            int keys = groupKey.indexOf(":");
            if (keys == -1) {
                return null;
            }
            entryKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_GROUPPREFIX + groupKey,
                                                                CACHE_KEY_SITEPREFIX + groupKey.substring(keys + 1)});
            group = (JahiaGroup) cache.get(entryKey);
        }
        if (group == null) {
            JahiaGrp jahiaGrp = dao.loadJahiaGroupByGroupKey(groupKey);
            if (jahiaGrp != null) {
                // Find Members    
                Map<String, Principal> members = null;
                boolean shouldPreload = org.jahia.settings.SettingsBean.getInstance()
                        .isPreloadDBGroupMembersActivated()
                        || JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME
                                .equals(jahiaGrp.getName());
//                boolean shouldPreload = false;
                if (shouldPreload) {
                    members = getGroupMembers(jahiaGrp);
                } else {
                    members = new ConcurrentHashMap<String, Principal>();
                }
                // Load Properties
                Properties properties = dao.loadProperties(jahiaGrp.getId(), jahiaGrp.getKey(),
                                                           JahiaGroupManagerDBProvider.PROVIDER_NAME);
                int siteID = 0;
                if (jahiaGrp.getSite() != null) {
                    siteID = jahiaGrp.getSite().getId().intValue();
                }
                group = getJahiaGroup(jahiaGrp, siteID, members, properties, shouldPreload);
                if (cache != null) {
                    cache.put(entryKey, group);
                }
            }
        }
        return group;
    }

    private JahiaGroup getJahiaGroup(JahiaGrp jahiaGrp, int siteID, Map<String, Principal> members, Properties properties, boolean preloadedGroups) {
        JahiaGroup group;
        if (JahiaGroupManagerDBProvider.GUEST_GROUPNAME.equals(jahiaGrp.getName())) {
            group = new GuestGroup(jahiaGrp.getId().intValue(), siteID, properties);
        } else if (JahiaGroupManagerDBProvider.USERS_GROUPNAME.equals(jahiaGrp.getName())) {
            group = new UsersGroup(jahiaGrp.getId().intValue(), siteID, properties);
        } else {
            group = new JahiaDBGroup(jahiaGrp.getId().intValue(), jahiaGrp.getName(), jahiaGrp.getKey(),
                                     siteID, members, properties, preloadedGroups);
        }
        return group;
    }

    public List<String> getGroupKeys(int siteID) {
        return dao.getGroupKeys(new Integer(siteID));
    }

    public List<String> getGroupNames(int siteID) {
        return dao.getGroupNames(new Integer(siteID));
    }

    public Properties getGroupProperties(int id, String providerName, String groupKey) {
        return dao.loadProperties(new Integer(id), groupKey, providerName);
    }

    public List<String> getUserMembership(String name) {
        if (membership == null) {
            try {
                membership = cacheService.createCacheInstance("Acl_Manager_Membership_Cache");
            } catch (JahiaInitializationException e) {
                log.error(e);
            }
        }
        if (membership != null) {
            if (membership.containsKey(name)) {
                List<String> result = (List<String>) membership.get(name);
                if (result != null) {
                    return result;
                }
            }
            List<String> list = accessDAO.getUserMembership(name);
            membership.put(name, list);
            return list;
        } else {
            return accessDAO.getUserMembership(name);
        }
    }

    public void removeAllGroupsFromSite(int siteID) {
        if (siteID > 0) {
            dao.removeAllGroupsFromSite(new Integer(siteID));
            if (cache != null) {
                cache.flushGroup(CACHE_KEY_SITEPREFIX + siteID);
            }
            if (membership != null) {
                membership.flush();
            }
        }
    }

    public void removeGroupFromAllSites(JahiaGroup grp) {
        dao.removeGroupFromAllSites(grp.getGroupname());
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_GROUPPREFIX + grp.getGroupKey());
        }
        if (membership != null) {
            membership.flush();
        }
    }

    public void removeGroupFromSite(int siteID, JahiaGroup grp) {
        if (siteID > 0) {
            dao.removeGroupFromSite(new Integer(siteID), grp.getGroupname());
            if (cache != null) {
                cache.flushGroup(CACHE_KEY_SITEPREFIX + siteID);
            }
            if (membership != null) {
                membership.flush();
            }
        }
    }

    public void removeMember(String memberName, String groupName, int type) {
        JahiaGrpAccess access = accessDAO.findById(new JahiaGrpAccessPK(memberName, groupName, new Integer(type)));
        accessDAO.delete(access);
        if (cache != null) {
            cache.flushGroup(CACHE_KEY_GROUPPREFIX + groupName);
        }
        if (membership != null) {
            membership.flush();
        }
    }

    public boolean removeProperty(String key, int id, String providerName, String groupKey) {
        try {
            dao.removeProperty(key, new Integer(id), providerName, groupKey);
            if (cache != null) {
                cache.flushGroup(CACHE_KEY_GROUPPREFIX + groupKey);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeUserFromAllGroups(String name) {
        try {
            accessDAO.removeUserFromAllGroups(name);
            if (cache != null) {
                cache.flush();
            }
            if (membership != null) {
                membership.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void searchGroupName(String curCriteriaValue, int siteID, Set<String> groupKeys) {
        List<String> list = dao.searchGroupName(curCriteriaValue, new Integer(siteID));
        groupKeys.addAll(list);
    }

    public void searchGroupName(List<String> criteriaNameList, List<String> criteriaValueList, int siteID, Set<String> groupKeys, String providerName) {
        List<String> list = dao.searchGroupName(criteriaNameList, criteriaValueList, new Integer(siteID), providerName);
        groupKeys.addAll(list);
    }

    public boolean updateProperty(String key, String value, int id, String providerName, String groupKey) {
        try {
            JahiaGrpProp prop = dao.getProperty(key, new Integer(id), providerName, groupKey);
            prop.setValue(value);
            dao.updateProperty(prop);
            if (cache != null) {
                cache.flushGroup(CACHE_KEY_GROUPPREFIX + groupKey);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Principal> getGroupMembers(String groupKey) {
        List<Object[]> memberIds = accessDAO.findMemberIdsFromGroupName(groupKey);
        Map<String, Principal> members = new ConcurrentHashMap<String, Principal>(memberIds.size());
        for (Object[] objects : memberIds) {        
            String memberKey = (String) objects[0];
            int memberType = ((Integer) objects[1]).intValue();
            if (memberType == JahiaGroupManagerDBProvider.USERTYPE) {
                JahiaUser jahiaUser = userService.lookupUserByKey(memberKey);
                if (jahiaUser != null) {
                    members.put(memberKey, jahiaUser);
                }
            } else if (memberType == JahiaGroupManagerDBProvider.GROUPTYPE) {
                JahiaGroup grp = JahiaGroupManagerRoutingService.getInstance().lookupGroup(memberKey);
                if (grp != null) {
                    members.put(memberKey, grp);
                }
            }
        }
        return members;
    }    
    
    private Map<String, Principal> getGroupMembers(JahiaGrp jahiaGrp) {
        return getGroupMembers(jahiaGrp.getKey());
    }

    public Map<String, String> getGroupsInSite(int siteID) {
        List<JahiaSitesGrp> list = dao.getGroupsInSite(siteID > 0 ? new Integer(siteID) : null);
        FastHashMap map = new FastHashMap(list.size());
        for (JahiaSitesGrp grp : list) {
            map.put(grp.getComp_id().getGroupName(), grp.getGroup().getName());
        }
        map.setFast(true); 
        return map;
    }

    public void searchAndDelete(String criteria, Integer siteId) {
        for (String s : dao.searchGroupNameInJahiaGrp(criteria, siteId)) {
            deleteGroup(s, siteId);
        }
    }

}

