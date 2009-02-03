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
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.*;
import org.jahia.services.usermanager.JahiaUserManagerDBProvider;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 18:04:02
 * To change this template use File | Settings | File Templates.
 */
public class JahiaUserDAO extends AbstractGeneratorDAO {
    public JahiaUser loadJahiaUserFromMemberKey(String memberKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaUser jahiaUser = null;
        if (memberKey != null) {
            List list = template.find("from JahiaUser u where u.key=?",
                                      new Object[]{memberKey});
            if (list.size() > 0) {
                jahiaUser = (JahiaUser) list.get(0);
            }
        }
        return jahiaUser;
    }

    public Properties loadProperties(Integer userId, String key, String providerName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Properties properties = new Properties();
        if (userId != null && key != null && providerName != null) {
            List list = template.find("select u.comp_id.name,u.value from JahiaUserProp u where u.comp_id.id=? " +
                                      "and u.comp_id.userkey=? and u.comp_id.provider=?",
                                      new Object[]{userId, key, providerName});
            for (int i = 0; i < list.size(); i++) {
                Object[] objects = (Object[]) list.get(i);
                String name = (String) objects[0];
                String value = (String) objects[1];
                if (null == value) value = "";
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    public void save(JahiaUser jahiaUser, String providerName, Map map) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (jahiaUser.getId() == null) {
            jahiaUser.setId(getNextInteger(jahiaUser));
        }
        template.save(jahiaUser);
        if (map != null && map.size() > 0) {
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                JahiaUserProp prop = new JahiaUserProp(new JahiaUserPropPK(jahiaUser.getId(),
                                                                           (String) entry.getKey(),
                                                                           providerName, jahiaUser.getKey()),
                                                       (String) entry.getValue());
                template.merge(prop);
            }
        }
    }

    public void delete(String userKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaUserProp p where p.comp_id.userkey=?", userKey));
        template.deleteAll(template.find("from JahiaUser u where u.key=?", userKey));
    }

    public JahiaUser loadJahiaUserByUserKey(String userKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaUser jahiaUser = null;
        if (userKey != null) {
            List list = template.find("from JahiaUser g where g.key=?",
                                      new Object[]{userKey});
            if (list.size() > 0) {
                jahiaUser = (JahiaUser) list.get(0);
            }
        }
        return jahiaUser;
    }

    public JahiaUser loadJahiaUserByName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaUser jahiaGrp = null;
        if (name != null) {
            List list = template.find("from JahiaUser g where g.name=?",
                                      new Object[]{name});
            if (list.size() > 0) {
                jahiaGrp = (JahiaUser) list.get(0);
            }
        }
        return jahiaGrp;
    }

    public List<String> getUsernameList() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct g.name from JahiaUser g");
    }

    public List<String> getUserkeyList() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct g.key from JahiaUser g");
    }

    public List<String> searchUserName(String curCriteriaValue) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<String> list = null;
        if (curCriteriaValue != null) {
            list = template.find("select distinct s.user.key from JahiaSitesUser s " +
                                 "where s.user.name like ? ",
                                 new Object[]{curCriteriaValue});
        }
        return list;
    }

    public List<String> searchUserName(List<String> criteriaNameList, List<String> criteriaValueList, String providerName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<String> list = new ArrayList<String>();
        if (criteriaNameList != null && criteriaValueList != null &&
            criteriaNameList.size() == criteriaValueList.size()) {
            List args = new ArrayList(criteriaNameList.size() * 4 + 1);
            args.add(providerName);
            StringBuffer hql = new StringBuffer("select distinct p.comp_id.userkey from JahiaUserProp p ");
            hql.append("where p.comp_id.provider=?");
            for (int i = 0; i < criteriaNameList.size(); i++) {
                String name = (String) criteriaNameList.get(i);
                String value = (String) criteriaValueList.get(i);
                if (name.equals("*")) {
                    hql.append(" and (p.value like ?) ");
                    args.add(value);
                    if (providerName.equals("jahia")) {
                        list.addAll(searchUserName(value));
                    }
                } else if (name.equalsIgnoreCase(JahiaUserManagerDBProvider.USERNAME_PROPERTY_NAME)) {
                    if (providerName.equals("jahia")) {
                        list.addAll(searchUserName(value));
                    }
                } else {
                    hql.append(" and (p.comp_id.name=? and p.value like ?) ");
                    args.add(name);
                    args.add(value);
                }
            }
            list.addAll(template.find(hql.toString(), args.toArray()));
        }
        return list;
    }

    public Integer getNumberOfUsers() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Long jahiaGrp = null;
        List list = template.find("select count (g.id) from JahiaUser g ");
        if (list.size() > 0) {
            jahiaGrp = (Long) list.get(0);
        }
        return jahiaGrp != null ? jahiaGrp.intValue() : null;
    }

    public JahiaUserProp getProperty(JahiaUserPropPK jahiaUserPropPK) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaUserProp) template.load(JahiaUserProp.class, jahiaUserPropPK);
    }

    public void deleteProperty(JahiaUserProp prop) {
        getHibernateTemplate().delete(prop);
    }

    public JahiaUser loadJahiaUserById(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaUser jahiaUser = (JahiaUser) template.load(JahiaUser.class, id);
        if (jahiaUser != null) {
            return jahiaUser;
        } else {
            throw new ObjectRetrievalFailureException(JahiaUser.class, id);
        }
    }

    public void update(JahiaUser jahiaUser) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(jahiaUser);
    }

    public void saveProperty(JahiaUserProp prop) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(prop);
    }

    public void updateProperty(JahiaUserProp prop) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(prop);
    }

    public void addMemberToSite(JahiaSitesUser jahiaSitesUser) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(jahiaSitesUser);
    }

    public void removeMemberFromSite(Integer siteID, String username) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesUser u where u.comp_id.siteId=? " +
                                      "and u.comp_id.username=?", new Object[]{siteID, username}));
    }

    public void removeMemberFromAllSite(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesUser u where u.user.key=?", name));
    }

    public void removeAllMembersFromSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesUser u where u.comp_id.siteId=? ",
                                      new Object[]{siteID}));
    }

    public List<JahiaSitesUser> getAllMembersNameOfSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if (siteID != null) {
            return template.find("from JahiaSitesUser u where u.comp_id.siteId=? ",
                                 new Object[]{siteID});
        } else {
            return template.find("from JahiaSitesUser u where u.comp_id.siteId is null ");
        }
    }

    public List<JahiaUser> getAllMembersOfSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if (siteID != null) {
            return template.find("select u.user from JahiaSitesUser u where u.comp_id.siteId=? ",
                                 new Object[]{siteID});
        } else {
            return template.find("select u.user from JahiaSitesUser u where u.comp_id.siteId is null ");
        }
    }

    public List<JahiaSitesUser> getUserSiteMembership(String username) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaSitesUser u where u.comp_id.username=? ", new Object[]{username});
    }

    public int getUserSiteMembershipCount(String username) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List list =  template.find("select count(*) from JahiaSitesUser u where u.comp_id.username=? ", new Object[]{username});
        if (list.size() > 0) {
            return ((Long) list.get(0)).intValue();
        }
        return 0;
    }

    public String getMemberNameInSite(Integer siteID, String username) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaSitesUser sitesUser = (JahiaSitesUser) template.get(JahiaSitesUser.class, new JahiaSitesUserPK(username, siteID));
        if (sitesUser != null) {
            return sitesUser.getUser().getKey();
        }
        return null;
    }

    public List deleteAllFromSite(Integer siteID) {
        List list = getAllMembersOfSite(siteID);
        List res = new ArrayList();
        removeAllMembersFromSite(siteID);
        getHibernateTemplate().flush();
        for (int i = 0; i < list.size(); i++) {
            JahiaUser user = (JahiaUser) list.get(i);
            if (getUserSiteMembershipCount(user.getName())==0 && user.getId()>1) {
                res.add(user.getKey());
                delete(user.getKey());
            }
        }
        return res;
    }
}
