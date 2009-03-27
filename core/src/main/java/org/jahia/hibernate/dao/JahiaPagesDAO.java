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

import org.apache.commons.collections.FastArrayList;
import org.hibernate.Query;
import org.jahia.content.ContentPageKey;
import org.jahia.hibernate.model.JahiaPagesData;
import org.jahia.hibernate.model.JahiaPagesDataPK;
import org.jahia.hibernate.model.JahiaPagesProp;
import org.jahia.hibernate.model.JahiaPagesPropPK;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageContentRights;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.version.EntryLoadRequest;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 25 févr. 2005
 * Time: 10:28:49
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPagesDAO extends AbstractGeneratorDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllIds() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd");
    }

    public List<JahiaPagesData> getAllPagesInfos() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("from JahiaPagesData pd order by pd.comp_id.id");
    }

    public Integer getNbPages() {
        Long retVal = null;
        String hql = "select count( pd.comp_id.id ) from JahiaPagesData pd ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Long> list = template.find(hql);
        if (!list.isEmpty()) {
            retVal = list.get(0);
        }
        return retVal.intValue();
    }

    public Integer getRealActiveNbPages() {
        Long retVal = null;
        String hql = "select count( distinct pd.comp_id.id ) from JahiaPagesData pd " +
                "where pd.pageType=0 and pd.comp_id.workflowState = " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Long> list = template.find(hql);
        if (!list.isEmpty()) {
            retVal = list.get(0);
        }
        return retVal.intValue();
    }

// -------------------------- OTHER METHODS --------------------------

    public void delete(JahiaPagesData data) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(data);
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public void delete(Integer pageId, Integer workflowState, String languageCode) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaPagesData pd where pd.comp_id.id=? and pd.comp_id.workflowState=? " +
                "and pd.comp_id.languageCode=?", new Object[]{pageId, workflowState, languageCode}));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public void deletePageProperty(Integer pageID, String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaPagesProp p where p.comp_id.name=? " +
                "and p.comp_id.pageId=?", new Object[]{name, pageID}));
    }

    public JahiaPagesData findByPK(JahiaPagesDataPK jahiaPagesDataPK) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaPagesData) template.get(JahiaPagesData.class, jahiaPagesDataPK);
    }

    public List<Integer> getActivePageChildIDs(Integer pageId) {
        List<Integer> retVal = null;
        String hql = "select distinct pd.comp_id.id, pd.comp_id.workflowState from JahiaPagesData pd " +
                "where pd.parentID = ? and pd.comp_id.workflowState = " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE +
                " order by pd.comp_id.id, pd.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{pageId});
            retVal = fillPagesId(list);
        }
        return retVal;
    }

    private List<Integer> fillPagesId(List<Object[]> list) {
        FastArrayList retVal = new FastArrayList(list.size());
        for (Object[] objects : list) {
            retVal.add(objects[0]);
        }
        retVal.setFast(true);
        return retVal;
    }

    private List<JahiaPageContentRights> fillPageRightObjects(List<Object[]> list) {
        FastArrayList retVal = new FastArrayList(list.size());
        for (Object[] objects : list) {
            retVal.add(new JahiaPageContentRights((Integer) objects[0],
                    (Integer) objects[1], (Integer) objects[2]));
        }
        retVal.setFast(true);
        return retVal;
    }

    public List<Integer> getActivePageIDsPointingOnPage(Integer pageId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd " +
                "where pd.pageLinkId=? and pd.pageType=1 " +
                "and pd.comp_id.workflowState>=" + EntryLoadRequest.ACTIVE_WORKFLOW_STATE,
                new Object[]{pageId});
    }

    public List<JahiaPagesData> getActivePageInfo(Integer pageId) {
        List<JahiaPagesData> retVal = null;
        String hql = "from JahiaPagesData pd where pd.comp_id.id = ? " +
                "and pd.comp_id.workflowState >= " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE + " ";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<Integer> getAllAclId(Integer siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct pd.jahiaAclId from JahiaPagesData pd where pd.siteId = ? ";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{siteId});
        }
        return retVal;
    }

    public List<Object[]> getAllNonDeletedPageChildIDs(Integer pageId) {
        List<Object[]> retVal = null;
        String hql = "select distinct pd.comp_id.id, pd.comp_id.versionId,pd.comp_id.workflowState " +
                "from JahiaPagesData pd where pd.parentID = ? " +
                "and pd.pageType in (0,1) and pd.comp_id.workflowState > " + EntryLoadRequest.VERSIONED_WORKFLOW_STATE + " " +
                "order by pd.comp_id.id, pd.comp_id.versionId desc, pd.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<Object[]> getAllPageChildIDs(Integer pageId) {
        FastArrayList retVal = new FastArrayList();
        String hql = "select distinct pd.comp_id.id, pd.comp_id.versionId,pd.comp_id.workflowState " +
                "from JahiaPagesData pd where pd.parentID = ? and pd.comp_id.workflowState <> " +
                EntryLoadRequest.VERSIONED_WORKFLOW_STATE + " " +
                "order by pd.comp_id.id, pd.comp_id.versionId desc, pd.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> vals = template.find(hql, new Object[]{pageId});
            Set<Integer> l = new HashSet<Integer>();
            for (Object[] tupe : vals) {
                Integer id = (Integer) tupe[0];
                Integer version = (Integer) tupe[1];
                Integer workflow = (Integer) tupe[2];
                if (!l.contains(id)) {
                    try {
                        if (workflow
                                == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                            ContentPage contentPage = ContentPage.getPage(id,
                                    false);
                            if (!contentPage.isDeletedOrDoesNotExist(version) ||
                                    pageId != contentPage.getParentID(
                                            EntryLoadRequest.STAGED)) {
                                continue;
                            }
                        }
                        l.add(id);
                        retVal.add(tupe);
                    } catch (Exception t) {
                        logger.debug(
                                "Exception retrieving page childs for pid[" + pageId + "]",
                                t);
                    }
                }
            }
            retVal.setFast(true);
        }
        return retVal;
    }

    public Integer getNbPages(Integer siteId) {
        Long retVal = null;
        String hql = "select count( pd.comp_id.id ) from JahiaPagesData pd where pd.siteId = ? ";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            List<Long> list = template.find(hql, new Object[]{siteId});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal.intValue();
    }

    public List<Integer> getPageIDsInSiteWithSpecifiedLink(Integer siteId, Integer linkType) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd " +
                "where pd.siteId=? and pd.pageType=?",
                new Object[]{siteId, linkType});
    }

    public List<Integer> getPageIDsWithTemplate(Integer templateId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd " +
                "where pd.pageDefinition.id=? and (pd.pageType=0 or pd.pageType=1)",
                new Object[]{templateId});
    }

    public List<JahiaPageContentRights> getPageIDsWithAclIDs(Set aclIDs) {
        Query query = this.getSession().createQuery(
                "select distinct pd.comp_id.id, pd.parentID, pd.jahiaAclId from JahiaPagesData pd "
                        + "where pd.jahiaAclId in (:aclIDs) and (pd.pageType=0 or pd.pageType=1) and "
                        + "pd.comp_id.workflowState > " + EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
        query.setParameterList("aclIDs", aclIDs);
        return fillPageRightObjects(query.list());
    }

    public List<Integer> getPageIdsInSite(Integer siteId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd where pd.siteId=?",
                new Object[]{siteId});
    }

    public List<Integer> getPageIdsInSiteOrderById(Integer siteId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd where pd.siteId=? order by pd.comp_id.id",
                new Object[]{siteId});
    }

    public List<Object[]> getPageProperties(Integer pageId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.name,p.comp_id.languageCode, p.value " +
                "from JahiaPagesProp p " +
                "where p.comp_id.pageId=?", pageId);
    }

    public List<Object[]> getPagePropertiesByValue(String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.pageId,p.comp_id.name,p.comp_id.languageCode, p.value " +
                "from JahiaPagesProp p " +
                "where p.value=?", value);
    }

    public List<Integer> findPageIdByPropertyNameAndValue(String name, String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.pageId from JahiaPagesProp p " +
                "where p.comp_id.name=? and p.value=?", new Object[]{name, value});
    }

    public List<Object[]> getPagePropertiesByName(String name) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.pageId,p.comp_id.name, p.value " +
                             "from JahiaPagesProp p " +
                             "where p.comp_id.name=?", name);
    }


    public Integer getRealActiveNbPages(Integer siteId) {
        Long retVal = null;
        String hql = "select count( pd.comp_id.id ) from JahiaPagesData pd where pd.siteId = ? " +
                "and pd.type=0 and pd.comp_id.workflowState = " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Long> list = template.find(hql, new Object[]{siteId});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal.intValue();
    }

    public List<Integer> getStagingPageChildIDs(Integer pageId) {
        List<Integer> retVal = null;
        String hql = "select distinct pd.comp_id.id, pd.comp_id.workflowState from JahiaPagesData pd " +
                "where pd.parentID = ? and pd.comp_id.workflowState > " +
                EntryLoadRequest.VERSIONED_WORKFLOW_STATE + " " +
                "order by pd.comp_id.id, pd.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{pageId});
            retVal = fillPagesId(list);
        }
        return retVal;
    }

    public List<Integer> getVersionedPageIDsPointingOnPage(Integer pageId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct pd.comp_id.id from JahiaPagesData pd " +
                "where pd.pageLinkId=? and pd.pageType=1",
                new Object[]{pageId});
    }

    public List<JahiaPagesData> getVersionedPageInfo(Integer pageId) {
        List<JahiaPagesData> retVal = null;
        String hql = "from JahiaPagesData pd where pd.comp_id.id = ? " +
                "and pd.comp_id.workflowState < " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE + " " +
                "order by pd.comp_id.versionId desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<Integer> getVersioningPageChildIDs(Integer pageId, Integer version) {
        List<Integer> retVal = null;
        String hql = "select distinct pd.comp_id.id, pd.comp_id.versionId from JahiaPagesData pd " +
                "where pd.parentID = ? and pd.comp_id.workflowState <= " +
                EntryLoadRequest.ACTIVE_WORKFLOW_STATE + " and pd.comp_id.versionId <= ? " +
                "order by pd.comp_id.id, pd.comp_id.versionId desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{pageId, version});
            retVal = fillPagesId(list);
        }
        return retVal;
    }

    public void save(JahiaPagesData data) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (data.getComp_id().getId() == null) {
            synchronized (this) {
                // List list = hibernateTemplate.find("select max(pd.comp_id.id) from JahiaPagesData pd");
                // data.getComp_id().setId(new Integer(((Integer) list.get(0)).intValue() + 1));
                data.getComp_id().setId(getNextInteger(data));
                hibernateTemplate.save(data);
            }
        } else {
            hibernateTemplate.merge(data);
        }
    }

    public void savePageProperty(Integer pageID, String name, Iterator<Map.Entry<String, String>> languageCodes) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCheckWriteOperations(false);
        List<JahiaPagesProp> entities = template.find("from JahiaPagesProp p where p.comp_id.name=? " +
                "and p.comp_id.pageId=?", new Object[]{name, pageID});

        if (entities == null || entities.isEmpty()) {
            try {
                while (languageCodes.hasNext()) {
                    Map.Entry<String, String> entry1 = languageCodes.next();
                    JahiaPagesPropPK comp_id = new JahiaPagesPropPK(pageID, name,
                            (String) entry1.getKey());
                    template.save(new JahiaPagesProp(comp_id, (String) entry1.getValue()));
                }
            } catch (Exception t) {
            }
        } else {
            while (languageCodes.hasNext()) {
                Map.Entry<String, String> entry1 = languageCodes.next();
                JahiaPagesPropPK comp_id = new JahiaPagesPropPK(pageID, name, (String) entry1.getKey());
                JahiaPagesProp entity = new JahiaPagesProp(comp_id, (String) entry1.getValue());
                if (entities.contains(entity)) {
                    template.merge(entity);
                } else template.save(entity);
            }
        }
    }

    public void saveProperties(Integer pageId, Map<String, Map<String, String>> properties) {
        if (pageId != null && properties != null && !properties.isEmpty()) {
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            List<JahiaPagesProp> entities = template.find(
                    "from JahiaPagesProp p where p.comp_id.pageId=?", pageId);
            for (Map.Entry<String, Map<String, String>> entry : properties.entrySet()) {
                Map<String, String> languageValues = entry.getValue();
                for (Map.Entry<String, String> entry1 : languageValues.entrySet()) {
                    JahiaPagesPropPK comp_id = new JahiaPagesPropPK(pageId,
                            entry.getKey(), entry1.getKey());
                    JahiaPagesProp entity = new JahiaPagesProp(comp_id,
                            entry1.getValue());
                    if (entities.contains(entity)) {
                        template.merge(entity);
                    } else
                        template.save(entity);
                }
            }
        }
    }

    public void update(JahiaPagesData data) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(data);
    }

    public void deleteProperties(Integer pageId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaPagesProp p where p.comp_id.pageId=?", pageId));
    }

    public int getNBPages(int pageID) {
        List<Long> nbPages = getHibernateTemplate().find("select count(pd.comp_id.id) from JahiaPagesData pd where pd.comp_id.id=?", pageID);
        return nbPages.get(0).intValue();
    }

    public Map<Serializable, Integer> deleteAllPagesFromSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List<JahiaPagesData> entities = template.find("from JahiaPagesData pd where pd.siteId=?", siteID);
        Map<Serializable, Integer> map = new HashMap<Serializable, Integer>(entities.size());
        for (JahiaPagesData jahiaPagesData : entities) {
            deleteProperties(jahiaPagesData.getComp_id().getId());
            map.put(new ContentPageKey(jahiaPagesData.getComp_id().getId()), jahiaPagesData.getJahiaAclId());
        }
        template.deleteAll(entities);
        return map;
    }

    public List<Object []> getPagePropertiesByValueAndSiteID(String propertyValue, Integer siteID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.pageId,p.comp_id.name,p.comp_id.languageCode, p.value " +
                "from JahiaPagesProp p, JahiaPagesData data " +
                "where p.value=? and p.comp_id.pageId=data.comp_id.id and data.siteId=?", new Object[]{propertyValue, siteID});
    }

    public List<Object []> getPagePropertiesByNameValueSiteIDAndParentID(String propertyName, String propertyValue, Integer siteID, Integer parentPageID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.pageId,p.comp_id.languageCode " +
                "from JahiaPagesProp p, JahiaPagesData data " +
                "where p.comp_id.name=? and p.value=? and p.comp_id.pageId=data.comp_id.id and data.siteId=? and data.parentID=?", new Object[]{propertyName, propertyValue, siteID, parentPageID});
    }

    /**
     * Returns the page ID with the specified URL key value for the given site.
     *
     * @param pageURLKey the page URL key value to search for
     * @param siteID     the target site ID
     * @return the page ID with the specified URL key value for the given site
     */
    public int getPageIDByURLKeyAndSiteID(String pageURLKey, Integer siteID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Integer> items;
        if (siteID > 0)
            items = template
                    .find(
                            "select p.comp_id.pageId "
                                    + "from JahiaPagesProp p, JahiaPagesData data "
                                    + "where p.comp_id.name='"
                                    + PageProperty.PAGE_URL_KEY_PROPNAME
                                    + "' and p.value=? and p.comp_id.pageId=data.comp_id.id and data.siteId=?",
                            new Object[]{pageURLKey, siteID});
        else
            items = template.find("select p.comp_id.pageId "
                    + "from JahiaPagesProp p, JahiaPagesData data "
                    + "where p.comp_id.name='"
                    + PageProperty.PAGE_URL_KEY_PROPNAME
                    + "' and p.value=? and p.comp_id.pageId=data.comp_id.id",
                    new Object[]{pageURLKey});
        return (!items.isEmpty() ? items.get(0) : 0);
    }

    public Map<String, String> getVersions(int site, String lang) {
        final HibernateTemplate template = getHibernateTemplate();
        List<Object[]> items = template.find("select uuid.comp_id.name, uuid.value, version.comp_id.name, version.value " +
                "from JahiaPagesProp uuid, JahiaPagesProp version, JahiaPagesData data " +
                "where data.comp_id.id=uuid.comp_id.pageId and data.siteId=? and uuid.comp_id.pageId=version.comp_id.pageId and uuid.comp_id.name='originalUuid' " +
                "and version.comp_id.name='lastImportedVersion-" + lang + "'", site);
        Map<String, String> results = new HashMap<String, String>();
        for (Object[] o : items) {
            results.put((String)o[1], (String)o[3]);
        }
        return results;
    }
}
