/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaGrp;
import org.jahia.hibernate.model.JahiaGrpProp;
import org.jahia.hibernate.model.JahiaGrpPropPK;
import org.jahia.hibernate.model.JahiaSitesGrp;
import org.jahia.services.usermanager.JahiaGroupManagerDBProvider;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 16:39:08
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGroupDAO extends AbstractGeneratorDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<String> getGroupKeys() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct g.key from JahiaGrp g where g.hidden=false");
    }

    public List<String> getGroupNames() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct g.name from JahiaGrp g where g.hidden=false");
    }

// -------------------------- OTHER METHODS --------------------------

    public void delete(String groupKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaGrpProp p where p.comp_id.groupKey=?", groupKey));
        template.deleteAll(template.find("from JahiaGrp g where g.key=?", groupKey));
    }

    public List<String> getGroupKeys(Integer siteId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if (siteId == null) {
            return template.find("select distinct g.key from JahiaGrp g where g.site.id is null");
        } else {
            return template.find("select distinct g.key from JahiaGrp g where ((g.site.id=?) ) and g.hidden=false",
                                 siteId);
        }
    }

    public List<String> getGroupNames(Integer siteId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if (siteId == null) {
            return template.find("select distinct g.name from JahiaGrp g where g.site.id is null");
        } else {
            return template.find("select distinct g.name from JahiaGrp g where ((g.site.id=?) ) and g.hidden=false",
                                 siteId);
        }
    }

    public JahiaGrp loadJahiaGroupByGroupKey(String groupKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaGrp jahiaGrp = null;
        if (groupKey != null) {
            List<JahiaGrp> list = template.find("from JahiaGrp g where g.key=?",
                                      new Object[]{groupKey});
            if (list.size() > 0) {
                jahiaGrp = list.get(0);
            }
        }
        return jahiaGrp;
    }

    public JahiaGrp loadJahiaGroupByName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaGrp jahiaGrp = null;
        if (name != null) {
            List<JahiaGrp> list = template.find("from JahiaGrp g where g.site is null and g.name=?",
                                      new Object[]{name});
            if (list.size() > 0) {
                jahiaGrp = list.get(0);
            }
        }
        return jahiaGrp;
    }

    public JahiaGrp loadJahiaGroupBySiteAndName(Integer siteId, String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaGrp jahiaGrp = null;
        if (siteId != null && name != null) {
            List<JahiaGrp> list = template.find("from JahiaGrp g where g.site.id=? and g.name=?",
                                      new Object[]{siteId, name});
            if (list.size() > 0) {
                jahiaGrp = list.get(0);
            }
        }
        return jahiaGrp;
    }

    public Properties loadProperties(Integer groupId, String key, String providerName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Properties properties = new Properties();
        if (groupId != null && key != null && providerName != null) {
            List list = template.find("select g.comp_id.name,g.value from JahiaGrpProp g where g.comp_id.id=? " +
                                      "and g.comp_id.groupKey=? and g.comp_id.provider=?",
                                      new Object[]{groupId, key, providerName});
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

    public void save(JahiaGrp jahiaGrp, String providerName, Map<Object, Object> map) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (jahiaGrp.getId() == null) {
            jahiaGrp.setId(getNextInteger(jahiaGrp));
        }
        template.save(jahiaGrp);
        if (map != null && map.size() > 0) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                JahiaGrpProp prop = new JahiaGrpProp(new JahiaGrpPropPK(jahiaGrp.getId(),
                                                                        (String)entry.getKey(),
                                                                        providerName, jahiaGrp.getKey()),
                                                     (String)entry.getValue());
                template.save(prop);
            }
        }
    }

    public List<String> searchGroupName(String curCriteriaValue, Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<String> list = null;
        if (curCriteriaValue != null && siteID != null) {
            list = template.find("select distinct s.group.key from JahiaSitesGrp s " +
                                 "where s.comp_id.site.id=? and s.group.name like ? ",
                                 new Object[]{siteID, curCriteriaValue});
        } else if (curCriteriaValue != null) {
            list = template.find("select distinct s.group.key from JahiaSitesGrp s " +
                                 "where s.comp_id.site.id is null and s.group.name like ? ",
                                 new Object[]{curCriteriaValue});
        }
        return list;
    }

    public List<String> searchGroupNameInJahiaGrp(String curCriteriaValue, Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<String> list = null;
        if (curCriteriaValue != null && siteID != null) {
            list = template.find("select distinct s.key from JahiaGrp s " +
                                 "where s.site.id=? and s.name like ? ",
                                 new Object[]{siteID, curCriteriaValue});
        } else if (curCriteriaValue != null) {
            list = template.find("select distinct s.key from JahiaGrp s " +
                                 "where s.site is null and s.name like ? ",
                                 new Object[]{curCriteriaValue});
        }
        return list;
    }

    public List<String> searchGroupName(List<String> criteriaNameList, List<String> criteriaValueList, Integer siteID, String providerName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<String> list = null;
        if (criteriaNameList != null && criteriaValueList != null &&
            criteriaNameList.size() == criteriaValueList.size() && siteID != null) {
            List<Object> args = new ArrayList<Object>(criteriaNameList.size() * 4 + 2);
            args.add(siteID);
            args.add(providerName);
            StringBuffer hql = new StringBuffer("select distinct s.group.key from JahiaSitesGrp s,JahiaGrpProp p ");
            hql.append("where s.comp_id.site.id=? and p.comp_id.provider=?");
            hql.append("and s.group.id=p.comp_id.id ");
            for (int i = 0; i < criteriaNameList.size(); i++) {
                String name = (String) criteriaNameList.get(i);
                String value = (String) criteriaValueList.get(i);
                if (name.equals("*")) {
                    hql.append(" and (s.group.name like ? or p.value like ?) ");
                    args.add(value);
                    args.add(value);
                } else if (!name.equalsIgnoreCase(JahiaGroupManagerDBProvider.GROUPNAME_PROPERTY_NAME)) {
                    hql.append(" and (p.comp_id.name=? and p.value like ?) ");
                    args.add(name);
                    args.add(value);
                }
            }
            list = template.find(hql.toString(), args.toArray());
        }
        return list;
    }

    public void removeProperty(String key, Integer groupId, String providerName, String groupKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaGrpProp g where g.comp_id.id=? " +
        							"and g.comp_id.groupKey=? and g.comp_id.provider=? and g.comp_id.name=?",
        							new Object[]{groupId, groupKey, providerName, key}));
    }

    public JahiaGrpProp getProperty(String key, Integer groupId, String providerName, String groupKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaGrpProp) template.load(JahiaGrpProp.class,
                                                          new JahiaGrpPropPK(groupId, key, providerName, groupKey));
    }

    public void saveProperty(JahiaGrpProp grpProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(grpProp);
    }
    public void updateProperty(JahiaGrpProp grpProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(grpProp);
    }

    public void addGroupToSite(JahiaSitesGrp jahiaSitesGrp) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.save(jahiaSitesGrp);
    }

    public void removeGroupFromSite(Integer siteID, String groupname) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesGrp s where s.comp_id.site.id=? " +
                                      "and s.comp_id.groupName=?",new Object[]{siteID,groupname}));
    }

    public void removeGroupFromAllSites(String groupName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesGrp s where s.group.name=? ",
                                      new Object[]{groupName}));
    }

    public void removeAllGroupsFromSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSitesGrp s where s.comp_id.site.id=? ",
                                      new Object[]{siteID}));
    }

    public List<JahiaSitesGrp> getGroupsInSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if (siteID == null) {
            return template.find("from JahiaSitesGrp s where s.comp_id.site.id is null");
        } else {
            return template.find("from JahiaSitesGrp s where s.comp_id.site.id=?",
                                 siteID);
        }
    }

    public List<String> deleteAllFromSite(Integer siteID) {
        List<JahiaSitesGrp> list = getGroupsInSite(siteID);
        List<String> res = new ArrayList<String>();
        removeAllGroupsFromSite(siteID);
        for (JahiaSitesGrp jahiaGrp : list) {
            JahiaGrp group = jahiaGrp.getGroup();
            if(group.getSite() != null && group.getSite().getId().equals(siteID)) {
                res.add(group.getKey());
                delete(group.getKey());
            }
        }
        return res;

    }
}

