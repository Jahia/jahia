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
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentPageKey;
import org.jahia.hibernate.model.JahiaContainer;
import org.jahia.hibernate.model.JahiaContainerProperty;
import org.jahia.hibernate.model.JahiaContainerPropertyPK;
import org.jahia.hibernate.model.JahiaCtnEntryPK;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 14 janv. 2005
 * Time: 15:29:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(getClass());

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllContainerIds() {
        String hql = "select distinct c.comp_id.id from JahiaContainer c";
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find(hql);
    }

    public List<Object[]> getAllContainerIds(BitSet ids) {
        String inClause = JahiaTools.getConditionalInClauseFromBitSet(ids, "c.comp_id.id", "=", "");
        String hql = "select distinct c.comp_id.id,c.rank from JahiaContainer c where " + inClause + " order by c.rank";
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return hibernateTemplate.find(hql);
    }

    public List<Object[]> getAllNonDeletedStagingContainerIds() {
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public List<Object[]> getAllNonDeletedStagingContainerIds(BitSet ids) {
        String inClause = JahiaTools.getConditionalInClauseFromBitSet(ids, "c.comp_id.id", "=", "and");
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where " + inClause + " c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public List<Object[]> getAllPublishedContainerIds() {
        String hql = "select distinct c.comp_id.id,c.rank from JahiaContainer c " +
                "where c.comp_id.workflowState=1 " +
                "order by c.rank,c.comp_id.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public List<Object[]> getAllPublishedContainerIds(BitSet ids) {
        String inClause = JahiaTools.getConditionalInClauseFromBitSet(ids, "c.comp_id.id", "=", "and");
        String hql = "select distinct c.comp_id.id,c.rank from JahiaContainer c where " +
                inClause + " c.comp_id.workflowState=1 " +
                "order by c.rank,c.comp_id.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public List<Object[]> getAllStagingContainerIds() {
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public List<Object[]> getAllStagingContainerIds(BitSet ids) {
        String inClause = JahiaTools.getConditionalInClauseFromBitSet(ids, "c.comp_id.id", "=", "and");
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where " + inClause + " c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

// -------------------------- OTHER METHODS --------------------------

    public void backupContainer(Integer containerID) {
        try {
            JahiaContainer container = loadPublishedContainer(containerID);
            if (container != null) {
                JahiaContainer jahiaContainer = (JahiaContainer) container.clone();
                jahiaContainer.getComp_id().setWorkflowState((0));
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
                hibernateTemplate.save(jahiaContainer);
            }
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaContainer");
        }
    }

    public JahiaContainer createStagedContainer(JahiaContainer container, Integer version, Integer workflowState) {
        try {
            JahiaContainer jahiaContainer = (JahiaContainer) container.clone();
            jahiaContainer.getComp_id().setVersionId(version);
            jahiaContainer.getComp_id().setWorkflowState(workflowState);
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.save(jahiaContainer);
            return jahiaContainer;
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaContainer");
            return null;
        }
    }

    public void deleteAllEntriesForContainerId(Integer integer) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainer c where c.comp_id.id=?", integer));
    }

    public void deleteContainer(JahiaContainer container) {
        if (container != null) {
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.delete(container);
            hibernateTemplate.flush();
        }
    }

    public void deleteContainers(List<JahiaContainer> containers) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(containers);
        hibernateTemplate.flush();
    }

    public Map<Object, Object> getProperties(Integer id) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Object[]> props = template.find("select p.comp_id.name,p.value from JahiaContainerProperty p " +
                "where p.comp_id.containerId=?", id);
        FastHashMap properties = new FastHashMap(props.size());
        for (Object[] objects : props) {
            properties.put(objects[0], objects[1]);
        }
        properties.setFast(true);
        return properties;
    }

    public Map<Integer, Map<Object, Object>>  getProperties(List<JahiaContainer> jahiaContainers) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer buffer = new StringBuffer(1024);
        buffer.append("select p.comp_id.containerId,p.comp_id.name,p.value from JahiaContainerProperty p where p.comp_id.containerId in (");
        for (Iterator<JahiaContainer> it = jahiaContainers.iterator(); it.hasNext();) {
            JahiaContainer container = (JahiaContainer) it.next();
            buffer.append(container.getCtnId());
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        buffer.append(")");
        List<Object[]> props = template.find(buffer.toString());
        Map<Integer, Map<Object, Object>> propertiesByContainerId = new HashMap<Integer, Map<Object, Object>> ();
        for (Object[] objects : props) {
            Integer key = (Integer) objects[0];
            Map<Object, Object> properties = propertiesByContainerId.get(key);
            if (properties == null) {
                properties = new HashMap<Object, Object>();
                propertiesByContainerId.put(key, properties);
            }
            properties.put(objects[1], objects[2]);
        }
        return propertiesByContainerId;
    }

    public Map<Integer, Map<Object, Object>> getProperties(Integer id, int batchSize) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Object[]> props = template.find("select p.comp_id.containerId,p.comp_id.name,p.value from JahiaContainerProperty p " +
                "where p.comp_id.containerId between ? and ?",
                new Object[]{(id - batchSize), (id + batchSize)});
        Map<Integer, Map<Object, Object>> propertiesByContainerId = new HashMap<Integer, Map<Object, Object>>();
        Map<Object, Object> properties;
        Integer key;
        for (Object prop : props) {
            Object[] objects = (Object[]) prop;
            key = (Integer) objects[0];
            properties = (Map<Object, Object>) propertiesByContainerId.get(key);
            if (properties == null) {
                properties = new HashMap<Object, Object>();
                propertiesByContainerId.put(key, properties);
            }
            properties.put(objects[1], objects[2]);
        }
        return propertiesByContainerId;
    }

    public JahiaContainer findContainerById(JahiaCtnEntryPK pk) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final JahiaContainer jahiaContainer = (JahiaContainer) template.get(JahiaContainer.class, pk);
        if (jahiaContainer == null) {
            throw new ObjectRetrievalFailureException(JahiaContainer.class, pk);
        }
        return jahiaContainer;
    }

    public JahiaContainer findContainerById(Integer id, Integer versionID, Integer workflowState) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaContainer) template.get(
                JahiaContainer.class, new JahiaCtnEntryPK(id, versionID,
                workflowState));
    }

    public List<JahiaContainer> findContainers(Integer pageId) {
        List<JahiaContainer> retVal = null;
        String hql = "from JahiaContainer c where c.pageid=? and c.comp_id.workflowState=1";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public Map<Integer, Integer> getAllContainerAclIdsForContainerList(Integer listId) {
        FastHashMap retVal = null;
        String hql = "select distinct c.comp_id.id, c.jahiaAclId from JahiaContainer c where c.listid=?";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> objects = template.find(hql, new Object[]{listId});
            retVal = new FastHashMap(objects.size());
            for (Object object : objects) {
                Object[] objects1 = (Object[]) object;
                retVal.put(objects1[0], objects1[1]);
            }
            retVal.setFast(true);
        }
        return retVal;
    }

    public List<Integer> getAllContainerAclIdsFromSite(Integer siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct c.jahiaAclId from JahiaContainer c where c.siteId=? order by c.jahiaAclId";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{siteId});
        }
        return retVal;
    }

    public Map<Integer, List<Integer>> getAllContainerIdsFromContainerListForPage(Integer pageId) {
        Map<Integer, List<Integer>> retval = null;
        String hql = "select distinct c.comp_id.id,c.listid from JahiaContainer c where c.pageid=?";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> objects = template.find(hql, new Object[]{pageId});
            retval = new FastHashMap(objects.size());
            for (Object object : objects) {
                Object[] objects1 = (Object[]) object;
                final Integer parent = (Integer) objects1[1];
                List<Integer> list = retval.get(parent);
                if (list == null) {
                    list = new ArrayList<Integer>(10);
                    retval.put(parent, list);
                }
                list.add((Integer)objects1[0]);
            }
            ((FastHashMap)retval).setFast(true);
        }
        return retval;
    }

    public List<Integer> getAllContainerIdsFromList(Integer listId) {
        List<Integer> retVal = Collections.emptyList();
        String hql = "select distinct c.comp_id.id,c.rank from JahiaContainer c where c.listid=? order by c.rank";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(false);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{listId});
            if (!list.isEmpty()) {
                FastArrayList tmpList = new FastArrayList(list.size());
                for (Object[] aList : list) {
                    tmpList.add(aList[0]);
                }
                tmpList.setFast(true);
                retVal = tmpList;
            }
        }
        return retVal;
    }

    public List<Integer> getAllContainerIdsFromSite(Integer siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct c.comp_id.id from JahiaContainer c where c.siteId=? order by c.comp_id.id";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{siteId});
        }
        return retVal;
    }

    public List<Integer> getDeletedContainerIdsBySiteAndCtnDef(final Integer siteId,
                                                      final String ctnDefName) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (List<Integer>) template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws
                    HibernateException {
                Criteria crit = session.createCriteria(JahiaContainer.class);
                crit.createAlias("ctndef", "ctndef");
                if (siteId != null && siteId != -1) {
                    crit.add(Expression.eq("siteId", siteId));
                }
                if (ctnDefName != null && !"".equals(ctnDefName.trim())) {
                    crit.add(Expression.eq("ctndef.name", ctnDefName));
                }
                crit.add(Expression.gt("comp_id.workflowState", (1)));
                crit.add(Expression.eq("comp_id.versionId", (-1)));
                crit.setProjection(Projections.distinct(Property.forName(
                        "ctnId")));
                return crit.list();
            }
        });
    }

    public List<Integer> getAllContainerIdsInListSortedByFieldValue(Integer listId, String fieldName, boolean ascendantSort) {
        List<Integer> retVal = null;
        StringBuffer hql;
        hql = new StringBuffer("select distinct c.comp_id.id,data.value from JahiaContainer c, JahiaFieldsData data ");
        hql.append("where c.listid=? and c.comp_id.id=data.containerId and data.fieldDefinition.name=? ");
        hql.append("order by data.value ");
        if (!ascendantSort) {
            hql.append("desc");
        }
        if (listId != null && fieldName != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql.toString(), new Object[]{listId, fieldName});
            retVal = fillContainerIdList(list);
        }
        return retVal;
    }

    private List<Integer> fillContainerIdList(List<Object[]> list) {
        FastArrayList retVal = new FastArrayList(list.size());
        for (Object aList : list) {
            Object[] objects = (Object[]) aList;
            retVal.add(objects[0]);
        }
        retVal.setFast(true);
        return retVal;
    }

    /**
     * Get the entry state for this container.
     *
     * @param containerId
     * @return List of objectArray each array contains an integer (workflowState) and a long (version)
     */
    public List<Object[]> getAllInactiveVersionedEntry(Integer containerId) {
        String queryString = "select c.comp_id.workflowState,c.comp_id.versionId from JahiaContainer c " +
                "where c.comp_id.id=? and c.comp_id.workflowState<=0 ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, new Object[]{containerId});
    }

    public List<Object[]> getAllVersionedContainerIds(Integer version) {
        List<Object[]> retVal = null;
        if (version != null) {
            String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank from JahiaContainer c "
                    + "where c.comp_id.workflowState<=1 and c.comp_id.versionId between 0 and ? "
                    + "order by c.comp_id.id,c.comp_id.versionId desc";
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{version});
        }
        return retVal;
    }

    public List<Object[]> getAllVersionedContainerIds(Integer version, BitSet ids) {
        List<Object[]> retVal = null;
        if (version != null) {
            String inClause = JahiaTools.getConditionalInClauseFromBitSet(ids, "c.comp_id.id", "=", "and");
            String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank from JahiaContainer c "
                    + "where " + inClause + " c.comp_id.workflowState<=1 and c.comp_id.versionId between 0 and ? "
                    + "order by c.comp_id.id,c.comp_id.versionId desc";
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{version});
        }
        return retVal;
    }

    /**
     * Get the entry state for this container.
     *
     * @param containerId
     * @return List of objectArray each array contains an integer (workflowState) and a long (version)
     */
    public List<Object[]> getAllVersionedEntry(Integer containerId) {
        String queryString = "select c.comp_id.workflowState, c.comp_id.versionId from JahiaContainer c " +
                "where c.comp_id.id=? " +
                "and c.comp_id.workflowState<=1 ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, new Object[]{containerId});
    }

    public List<Object[]> getNonDeletedStagingContainerIdsFromList(Integer listId) {
        List<Object[]> retVal = null;
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where c.listid=? and c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(false);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{listId});
        }
        return retVal;
    }

    public List<Integer> getPublishedContainerIdsFromList(Integer listId) {
        List<Integer> retVal = null;
        String hql = "select distinct c.comp_id.id,c.rank from JahiaContainer c " +
                "where c.listid=? and c.comp_id.workflowState=1 " +
                "order by c.rank,c.comp_id.id";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{listId});
            retVal = fillContainerIdList(list);
        }
        return retVal;
    }

    public List<Integer> getPublishedContainerIdsFromPage(Integer pageId) {
        List<Integer> retVal = null;
        String hql = "select c.comp_id.id from JahiaContainer c " +
                "where c.pageid=? and c.comp_id.workflowState=1";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<Integer> getPublishedContainerIdsInListSortedByFieldValue(Integer listId, String fieldName,
                                                                 boolean ascendantSort) {
        List<Integer> retVal = null;
        StringBuffer hql;
        hql = new StringBuffer("select distinct c.comp_id.id,data.value from JahiaContainer c, JahiaFieldsData data ");
        hql.append("where c.listid=? and c.comp_id.id=data.containerId and data.fieldDefinition.name=? ");
        hql.append("and c.comp_id.workflowState=1 ");
        hql.append("order by data.value ");
        if (!ascendantSort) {
            hql.append("desc");
        }
        if (listId != null && fieldName != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql.toString(), new Object[]{listId, fieldName});
            retVal = fillContainerIdList(list);
        }
        return retVal;
    }

    public List<Integer> getStagedContainerInPage(Integer pageId) {
        List<Integer> retVal = null;
        String hql = "select distinct c.comp_id.id from JahiaContainer c " +
                "where c.pageid=? and c.comp_id.workflowState>1 " +
                "order by c.comp_id.id";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<Object[]> getStagingContainerIdsFromList(Integer listId) {
        List<Object[]> retVal = null;
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where c.listid=? and c.comp_id.workflowState>=1 " +
                "order by c.comp_id.id,c.comp_id.workflowState desc";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{listId});
        }
        return retVal;
    }

    public List<Object[]> getContainerIds(Integer ctListId, Integer[] siteIds, Boolean siteLevel, String[] containerDefinitionNames,
                                EntryLoadRequest loadRequest, boolean ignoreLang, boolean stagingOnly, boolean ascendingOrder,
                                boolean orderByPage, Set<Integer> pagesID) {

        StringBuffer buff = new StringBuffer(1024);
        Map<String, Object> parameters = new HashMap<String, Object>();
        buff.append(" select distinct c.comp_id.id, c.comp_id.workflowState, c.comp_id.versionId, c.jahiaAclId, c.pageid from JahiaContainer c ");
        if (!ignoreLang || stagingOnly || loadRequest.isStaging()) {
            buff.append(", JahiaFieldsData f ");
        }
        buff.append(" where ");
        if (Boolean.TRUE.equals(siteLevel) && siteIds != null
                && siteIds.length > 0) {
            buff.append(" c.siteId IN (:siteIds) AND ");
        }

        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            buff.append(" c.ctndef.name IN (:containerDefinitionNames) AND ");
        } else if (ctListId != null && ctListId > 0) {
            parameters.put("ctListId", ctListId);
            buff.append(" c.listid= :ctListId AND ");
        }

        if (pagesID != null && !pagesID.isEmpty()) {
            buff.append(" c.pageid in (:pagesID) AND ");
        }

        appendContainerMultilangAndWorkflowParams(buff, parameters, loadRequest,
                ignoreLang, stagingOnly);
        buff.append(" order by ");
        if (orderByPage) {
            buff.append(" c.pageid,");
        }
        buff.append("c.comp_id.id");
        if (!ascendingOrder) {
            buff.append(" desc");
        }
        buff.append(", c.comp_id.workflowState ");

        Query query = this.getSession().createQuery(buff.toString());

        if (Boolean.TRUE.equals(siteLevel) && siteIds != null
                && siteIds.length > 0) {
            query.setParameterList("siteIds", siteIds);
        }
        if (containerDefinitionNames != null
                && containerDefinitionNames.length > 0) {
            query.setParameterList("containerDefinitionNames", containerDefinitionNames);
        }
        if (pagesID != null && !pagesID.isEmpty()) {
            query.setParameterList("pagesID", pagesID);
        }

        for (Object o : parameters.keySet()) {
            String name = (String) o;
            Object parameter = parameters.get(name);
            query.setParameter(name, parameter);
        }
        //query.setMaxResults(10000);
        return query.list();

    }

    public List<Object[]> getVersionedContainerIdsFromList(Integer listId, Integer version) {
        List<Object[]> retVal = null;
        String hql = "select distinct c.comp_id.id,c.comp_id.versionId,c.rank from JahiaContainer c " +
                "where c.listid=? and c.comp_id.workflowState<=1 and c.comp_id.versionId between 0 and ? " +
                "order by c.comp_id.id,c.comp_id.versionId desc";
        if (listId != null && version != null) {
            HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{listId, version});
        }
        return retVal;
    }

    public List<Integer> getDeletedContainerIdsFromList(Integer listId) {
        List<Integer> retVal = null;
        String hql = "select distinct c.comp_id.id,c.rank,c.comp_id.workflowState from JahiaContainer c " +
                "where c.listid=? and c.comp_id.workflowState=-1 " +
                "order by c.rank,c.comp_id.workflowState desc,c.comp_id.id";
        if (listId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{listId});
            retVal = fillContainerIdList(list);
        }
        return retVal;
    }

    public List<JahiaContainer> loadAllInactiveVersionedContainers(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState<=0 ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, new Object[]{containerId});
    }

    public JahiaContainer loadDeletedContainer(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState=-1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainer> loadDeletedContainer(List<Integer> ids) {
        StringBuffer queryString = new StringBuffer(1024);
        queryString.append("from JahiaContainer c ");
        queryString.append(" left join fetch c.ctndef ctndef ");
        queryString.append(" where c.comp_id.id in (");
        Iterator<Integer> it = ids.iterator();
        Integer id;
        while (it.hasNext()) {
            id = (Integer) it.next();
            queryString.append(String.valueOf(id));
            if (it.hasNext()) {
                queryString.append(",");
            }
        }
        queryString.append(")  and c.comp_id.workflowState=-1");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString.toString());
    }

    public List<JahiaContainer> loadPublishedContainer(Integer containerId, int batchSize) {
        StringBuffer queryString = new StringBuffer(1024);
        queryString.append("select c from JahiaContainer c ");
        queryString.append(" left join fetch c.ctndef ctndef ");
        queryString.append(" where c.comp_id.id between ? and ?  and c.comp_id.workflowState=1");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString.toString(),
                new Object[]{(containerId - batchSize),
                        (containerId + batchSize)});
    }

    public JahiaContainer loadPublishedContainer(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainer> loadPublishedContainer(List<Integer> ids) {
        StringBuffer queryString = new StringBuffer(1024);
        queryString.append("from JahiaContainer c ");
        queryString.append(" left join fetch c.ctndef ctndef ");
        queryString.append(" where ");
        Iterator<Integer> it = ids.iterator();
        Integer id;
        while (it.hasNext()) {
            id = (Integer) it.next();
            queryString.append("c.comp_id.id =").append(String.valueOf(id));
            if (it.hasNext()) {
                queryString.append(" or ");
            }
        }
        queryString.append(" and c.comp_id.workflowState=1");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString.toString());
    }

    public JahiaContainer loadStagedContainer(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainer> loadStagingContainer(Integer containerId, EntryLoadRequest request, int batchSize) {
        StringBuffer queryString = new StringBuffer(1024);
        queryString.append("select c from JahiaContainer c ");
        queryString.append(" left join fetch c.ctndef ctndef ");
        queryString.append(" where c.comp_id.id between ? and ? and c.comp_id.workflowState>=1 ORDER BY c.comp_id.workflowState DESC");

        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString.toString(),
                new Object[]{(containerId - batchSize),
                        (containerId + batchSize)});
        List<JahiaContainer> filteredList = new ArrayList<JahiaContainer>();
        Set<Integer> alreadyAddedCtns = new HashSet<Integer>();
        if (!list.isEmpty()) {
            for (JahiaContainer jahiaContainer : list) {
                if (!request.isWithDeleted() &&
                        ((jahiaContainer.getComp_id().getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE)
                                || (jahiaContainer.getComp_id().getVersionId() == EntryLoadRequest.DELETED_WORKFLOW_STATE))) {
                    continue;
                }
                if (!alreadyAddedCtns.contains(jahiaContainer.getCtnId())) {
                    filteredList.add(jahiaContainer);
                    alreadyAddedCtns.add(jahiaContainer.getCtnId());
                }
            }
        }
        return filteredList;
    }

    public List<JahiaContainer> loadStagedContainers(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, containerId);
    }

    public List<JahiaContainer> loadStagingContainer(List<Integer> ids, EntryLoadRequest request) {
        StringBuffer queryString = new StringBuffer(1024);
        queryString.append("select c from JahiaContainer c ");
        queryString.append(" left join fetch c.ctndef ctndef ");
        queryString.append(" where ");
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            Integer id = it.next();
            queryString.append("c.comp_id.id=").append(String.valueOf(id));
            if (it.hasNext()) {
                queryString.append(" or ");
            }
        }
        queryString.append(" and c.comp_id.workflowState>=1 ORDER BY c.comp_id.workflowState DESC");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString.toString());
        List<JahiaContainer> filteredList = new ArrayList<JahiaContainer>();
        Set<Integer> alreadyAddedCtns = new HashSet<Integer>();
        if (!list.isEmpty()) {
            for (JahiaContainer jahiaContainer : list) {
                if (!request.isWithDeleted() &&
                        ((jahiaContainer.getComp_id().getWorkflowState()
                                == EntryLoadRequest.DELETED_WORKFLOW_STATE)
                                || (jahiaContainer.getComp_id().getVersionId()
                                == EntryLoadRequest.DELETED_WORKFLOW_STATE))) {
                    continue;
                }
                if (!alreadyAddedCtns.contains(jahiaContainer.getCtnId())) {
                    filteredList.add(jahiaContainer);
                    alreadyAddedCtns.add(jahiaContainer.getCtnId());
                }
            }
        }
        return filteredList;
    }

    public JahiaContainer loadContainer(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        return getJahiaContainerFromList(list);
    }

    public JahiaContainer loadStagingContainer(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>=1 order by c.comp_id.workflowState DESC";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        return getJahiaContainerFromList(list);
    }

    public JahiaContainer loadStagingContainer(Integer containerId, EntryLoadRequest request) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>=1 order by c.comp_id.workflowState DESC";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, containerId);
        if (!list.isEmpty()) {
            final JahiaContainer jahiaContainer = (JahiaContainer) list.get(0);
            if (!request.isWithDeleted() &&
                    ((jahiaContainer.getComp_id().getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE)
                            || (jahiaContainer.getComp_id().getVersionId() == EntryLoadRequest.DELETED_WORKFLOW_STATE))) {
                return null;
            }
        }
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainer> loadStagingContainers(Integer containerId) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, containerId);
    }

    public JahiaContainer loadVersionedContainer(Integer containerId, Integer version) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState<=1 " +
                "and c.comp_id.versionId <= ? order by c.comp_id.versionId desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, new Object[]{containerId, version});
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainer> loadVersionedContainer(List<Integer> ids, EntryLoadRequest request) {
        List<JahiaContainer> result = new ArrayList<JahiaContainer>();
        JahiaContainer container;
        for (Integer id : ids) {
            container = loadVersionedContainer(id, request);
            if (container != null) {
                result.add(container);
            }
        }
        return result;
    }

    public JahiaContainer loadVersionedContainer(Integer containerId, EntryLoadRequest request) {
        String queryString = "from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState<=1 " +
                "and c.comp_id.versionId <= ? order by c.comp_id.versionId desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaContainer> list = template.find(queryString, new Object[]{containerId, (request.getVersionID())});
        if (!list.isEmpty()) {
            final JahiaContainer jahiaContainer = (JahiaContainer) list.get(0);
            if ((jahiaContainer.getComp_id().getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE)
                    && !request.isWithDeleted()) {
                return null;
            }
        }
        return getJahiaContainerFromList(list);
    }

    public int getMaxRankingValue() {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        List<Integer> list = hibernateTemplate.find("select max(c.rank) from JahiaContainer c");
        if (!list.isEmpty() && list.get(0) != null) {
            return (list.get(0));
        } else {
            return 0;
        }
    }

    public void save(JahiaContainer container) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (container.getComp_id().getId() != null) {
            hibernateTemplate.merge(container);
        } else {
            synchronized (this) {
                // List list = hibernateTemplate.find("select max(c.comp_id.id) from JahiaContainer c");
                // container.getComp_id().setId((((Integer) list.get(0)) + 1));
                container.getComp_id().setId(getNextInteger(container));
                hibernateTemplate.merge(container);
            }
        }
        hibernateTemplate.flush();
    }

    public void mergeVersion(JahiaContainer container) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(container);
    }

    public void saveNewVersion(JahiaContainer container) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(container);
    }


    public void update(JahiaContainer container) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.update(container);
    }

    private JahiaContainer getJahiaContainerFromList(final List<JahiaContainer> list) {
        return (!list.isEmpty() ? list.get(0) : null);
    }

    public void deleteProperties(Integer ctnId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerProperty p where p.comp_id.containerId=?", ctnId));
    }

    public void saveProperties(Integer ctnID, Integer siteID, Map<Object, Object> properties) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCheckWriteOperations(false);
        if (properties != null) {
            properties = new HashMap<Object, Object>(properties);
            if (!properties.isEmpty()) {
                List<JahiaContainerProperty> list = template
                        .find("from JahiaContainerProperty p where p.comp_id.containerId=?",
                                ctnID);
                for (JahiaContainerProperty jahiaContainerProperty : list) {
                    String name = jahiaContainerProperty.getComp_id().getName();
                    if (!properties.containsKey(name)) {
                        template.delete(jahiaContainerProperty);
                    } else {
                        jahiaContainerProperty.setValue((String) properties
                                .remove(name));
                        template.save(jahiaContainerProperty);
                    }
                }
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    template.saveOrUpdate(new JahiaContainerProperty(
                            new JahiaContainerPropertyPK(ctnID, (String) entry
                                    .getKey()), siteID, (String) entry
                            .getValue()));
                }
                template.flush();
            }
        }

    }

    public int getNBContainer(int containerId) {
        List<Long> nbPages = getHibernateTemplate().find("select count(c.comp_id.id) from JahiaContainer c where c.comp_id.id=?", (containerId));
        return nbPages.get(0).intValue();
    }

    public Map<Serializable, Integer> deleteAllContainersFromSite(Integer siteID) {
        String queryString = "from JahiaContainer c where c.siteId=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List list = template.find(queryString, siteID);
        Map<Serializable, Integer> map = new HashMap<Serializable, Integer>(list.size());
        for (Object aList : list) {
            JahiaContainer data = (JahiaContainer) aList;
            map.put(new ContentContainerKey(data.getComp_id().getId()), data.getJahiaAclId());
            deleteProperties(data.getComp_id().getId());
        }
        template.deleteAll(list);
        return map;
    }

    public Integer getParentContainerListId(Integer ctnId) {
        String hql = "select distinct c.listid from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState>=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List list = template.find(hql, new Object[]{ctnId});
        if (!list.isEmpty()) {
            return (Integer) list.iterator().next();
        }
        return null;
    }

    public Integer getDeletedParentContainerListId(Integer ctnId) {
        String hql = "select distinct c.listid from JahiaContainer c where c.comp_id.id=? and c.comp_id.workflowState=-1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List list = template.find(hql, new Object[]{ctnId});
        if (!list.isEmpty()) {
            return (Integer) list.iterator().next();
        }
        return null;
    }

    public List<Integer> findContainerIdByPropertyNameAndValue(String name, String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.containerId from JahiaContainerProperty p " +
                "where p.comp_id.name=? and p.value=?", new Object[]{name, value});
    }

    public List<Object[]> getContainerPropertiesByName(String name) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.containerId,p.comp_id.name, p.value " +
                             "from JahiaContainerProperty p " +
                             "where p.comp_id.name=?", name);
    }

    public void deleteContainerProperty(Integer ctnId, String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerProperty p where p.comp_id.name=? " +
                                         "and p.comp_id.containerId=?", new Object[]{name, ctnId}));
    }




    public List<Object[]> getSortedContainerIds(Integer ctListId, Integer[] siteIds, Boolean siteLevel,
                                      List<Integer> ctnDefIDs, List<Integer> fieldDefIDs,
                                      boolean sortByMetadata,
                                      EntryLoadRequest loadRequest, boolean ignoreLang, boolean stagingOnly,
                                      boolean ascendingOrder, BitSet ctnIdsBitSet, int dbMaxResult) {

        StringBuffer buff = new StringBuffer(1024);
        Map parameters = new HashMap();

        if (sortByMetadata) {
            parameters.put("contentType", ContentContainerKey.CONTAINER_TYPE);
        }

        buff.append(" SELECT f.comp_id.id, f.comp_id.workflowState, f.comp_id.versionId, f.metadataOwnerId, f.containerId FROM JahiaFieldsData f ");
        boolean addSiteParam = false;
        boolean addContainerDefinitionNameParam = false;
        boolean addContainerListIdParam = false;
        if (siteLevel != null && siteLevel) {
            if (siteIds != null && siteIds.length > 0) {
                addSiteParam = true;
            }
            if (ctnDefIDs != null && !ctnDefIDs.isEmpty()) {
                buff.append(", JahiaContainer c ");
                addContainerDefinitionNameParam = true;
            }
        } else {
            if (ctnDefIDs != null && !ctnDefIDs.isEmpty()) {
                buff.append(", JahiaContainer c ");
                addContainerDefinitionNameParam = true;
            } else if (ctListId != null && ctListId > 0) {
                buff.append(", JahiaContainer c ");
                parameters.put("ctListId", ctListId);
                addContainerListIdParam = true;
            }
        }
        List ctnIDs = null;
        if (ctnIdsBitSet != null
                && ctnIdsBitSet.length() > 0
                && ctnIdsBitSet.cardinality() <= org.jahia.settings.SettingsBean.getInstance()
                .getDBMaxElementsForInClause()) {
            ctnIDs = new ArrayList();
            for (int index = ctnIdsBitSet.nextSetBit(0); index >= 0; index = ctnIdsBitSet
                    .nextSetBit(index + 1)) {
                ctnIDs.add((index));
            }
        }
        buff.append(" WHERE (");
        if (!fieldDefIDs.isEmpty()) {
            buff.append(" f.fieldDefinition.id IN (:fieldDefIds) ");
        }
        buff.append(" ) ");
        if (sortByMetadata) {
            buff.append(" AND f.metadataOwnerType= :contentType ");
        }
        if (addContainerDefinitionNameParam) {
            buff.append(" AND c.ctndef.id IN (:ctnDefIds) ");
        }
        if (addContainerListIdParam) {
            buff.append(" AND c.listid= :ctListId ");
        }
        if (addSiteParam) {
            buff.append(" AND c.siteId IN (:siteIds) ");
        }
        if (sortByMetadata) {
            buff.append(" AND c.comp_id.id=f.metadataOwnerId ");
            if (ctnIDs != null) {
                buff.append(" AND f.metadataOwnerId IN (:ctnIds) ");
            }
        } else {
            buff.append(" AND c.comp_id.id=f.containerId ");
            if (ctnIDs != null) {
                buff.append(" AND f.containerId IN (:ctnIds) ");
            }
        }
        appendFieldMultilangAndWorkflowParams(buff, parameters, loadRequest,
                ignoreLang, stagingOnly);
        buff.append(" ORDER BY f.value ");
        if (!ascendingOrder) {
            buff.append(" DESC ");
        }
        Query query = this.getSession().createQuery(buff.toString());
        query.setParameterList("fieldDefIds", fieldDefIDs);
        if (ctnIDs != null) {
            query.setParameterList("ctnIds", ctnIDs);
        }
        if (addContainerDefinitionNameParam) {
            query.setParameterList("ctnDefIds", ctnDefIDs);
        }
        if (addSiteParam) {
            query.setParameterList("siteIds", siteIds);
        }
        for (Object o : parameters.keySet()) {
            String name = (String) o;
            Object parameter = parameters.get(name);
            query.setParameter(name, parameter);
        }
        if (dbMaxResult > 0) {
            query.setMaxResults(dbMaxResult);
        }
        return query.list();
    }

    public List getSortedContainerIdsByChildPage(Integer ctListId, Integer[] siteIds, Boolean siteLevel,
                                                 List ctnDefIDs, List fieldDefIDs, List pageFieldDefIDs,
                                                 EntryLoadRequest loadRequest, boolean ignoreLang, boolean stagingOnly,
                                                 boolean ascendingOrder, BitSet ctnIdsBitSet, int dbMaxResult) {

        StringBuffer buff = new StringBuffer(1024);
        Map parameters = new HashMap();

        parameters.put("contentType", ContentPageKey.PAGE_TYPE);

        buff.append(" SELECT f.comp_id.id, f.comp_id.workflowState, f.comp_id.versionId, pageF.containerId FROM JahiaFieldsData pageF, JahiaFieldsData f ");
        boolean addSiteParam = false;
        boolean addContainerDefinitionNameParam = false;
        boolean addContainerListIdParam = false;
        if (siteLevel != null && siteLevel) {
            if (siteIds != null && siteIds.length > 0) {
                addSiteParam = true;
            }
            if (ctnDefIDs != null && !ctnDefIDs.isEmpty()) {
                buff.append(", JahiaContainer c ");
                addContainerDefinitionNameParam = true;
            }
        } else {
            if (ctnDefIDs != null && !ctnDefIDs.isEmpty()) {
                buff.append(", JahiaContainer c ");
                addContainerDefinitionNameParam = true;
            } else if (ctListId != null && ctListId > 0) {
                buff.append(", JahiaContainer c ");
                parameters.put("ctListId", ctListId);
                addContainerListIdParam = true;
            }
        }
        List ctnIDs = null;
        if (ctnIdsBitSet != null
                && ctnIdsBitSet.length() > 0
                && ctnIdsBitSet.cardinality() <= org.jahia.settings.SettingsBean.getInstance()
                .getDBMaxElementsForInClause()) {
            ctnIDs = new ArrayList();
            for (int index = ctnIdsBitSet.nextSetBit(0); index >= 0; index = ctnIdsBitSet
                    .nextSetBit(index + 1)) {
                ctnIDs.add((index));
            }
        }
        buff.append(" WHERE (");
        if (!fieldDefIDs.isEmpty()) {
            buff.append(" f.fieldDefinition.id IN (:fieldDefIds) ");
        }
        buff.append(") AND (");
        if (!pageFieldDefIDs.isEmpty()) {
            buff.append(" pageF.fieldDefinition.id IN (:pageFieldDefIds) ");
        }
        buff.append(")");

        buff.append(" AND f.metadataOwnerType= :contentType ");

        buff.append(" AND pageF.containerId = c.comp_id.id ");

        buff.append(" AND f.metadataOwnerId = pageF.value ");

        if (ctnIDs != null) {
            buff.append(" AND f.containerId IN (:ctnIds) ");
        }
        if (addContainerDefinitionNameParam) {
            buff.append(" AND c.ctndef.id IN (:ctnDefIds) ");
        }
        if (addContainerListIdParam) {
            buff.append(" AND c.listid= :ctListId ");
        }
        if (addSiteParam) {
            buff.append(" AND c.siteId IN (:siteIds) ");
        }

        appendFieldMultilangAndWorkflowParams(buff, parameters, loadRequest,
                ignoreLang, stagingOnly);

        buff.append(" ORDER BY f.value ");
        if (!ascendingOrder) {
            buff.append(" DESC ");
        }

        Query query = this.getSession().createQuery(buff.toString());
        query.setParameterList("fieldDefIds", fieldDefIDs);
        query.setParameterList("pageFieldDefIds", pageFieldDefIDs);
        if (ctnIDs != null) {
            query.setParameterList("ctnIds", ctnIDs);
        }
        if (addContainerDefinitionNameParam) {
            query.setParameterList("ctnDefIds", ctnDefIDs);
        }
        if (addSiteParam) {
            query.setParameterList("siteIds", siteIds);
        }

        for (Object o : parameters.keySet()) {
            String name = (String) o;
            Object parameter = parameters.get(name);
            query.setParameter(name, parameter);
        }
        if (dbMaxResult > 0) {
            query.setMaxResults(dbMaxResult);
        }
        return query.list();

    }


    /**
     * @param query
     * @param params
     * @param entryLoadRequest
     * @param ignoreLang
     * @param stagingOnly
     */
    private void appendFieldMultilangAndWorkflowParams(StringBuffer query,
                                                       Map params,
                                                       EntryLoadRequest entryLoadRequest,
                                                       boolean ignoreLang,
                                                       boolean stagingOnly) {

        if (entryLoadRequest.isCurrent()) {
            query.append(" AND f.comp_id.workflowState = ");
            query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else if (entryLoadRequest.isStaging()) {
            query.append(" AND f.comp_id.workflowState > ");
            if (stagingOnly) {
                query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
            } else {
                query.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
            }
        } else {
            query.append(" AND f.comp_id.versionId = ");
            query.append(entryLoadRequest.getVersionID());
        }
        if (!ignoreLang) {
            String languageCode = entryLoadRequest.getFirstLocale(true).
                    toString();
            query.append(" AND (");
            query.append("f.comp_id.languageCode = :languageCode OR f.comp_id.languageCode= :sharedLanguageCode ");
            params.put("languageCode", languageCode);
            params.put("sharedLanguageCode", ContentField.SHARED_LANGUAGE);
            query.append(" ) ");
        }
    }

    private void appendContainerMultilangAndWorkflowParams(StringBuffer query,
                                                           Map params, EntryLoadRequest entryLoadRequest,
                                                           boolean ignoreLang, boolean stagingOnly) {
        if (stagingOnly || entryLoadRequest.isStaging() || !ignoreLang) {
            query.append(" c.comp_id.id=f.containerId AND (");
        }
        if (stagingOnly || entryLoadRequest.isStaging()) {
            query.append(" (c.comp_id.workflowState > ");
            query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
            query.append(" or f.comp_id.workflowState > ");
            query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
            query.append(")");
        } else if (entryLoadRequest.isCurrent()) {
            query.append(" c.comp_id.workflowState = ");
            query.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else {
            query.append(" c.comp_id.versionId = ");
            query.append(entryLoadRequest.getVersionID());
        }
        if (!ignoreLang) {
            query.append("AND (f.comp_id.languageCode = :languageCode OR f.comp_id.languageCode= :sharedLanguageCode) ");
            params.put("languageCode", entryLoadRequest.getFirstLocale(true)
                    .toString());
            params.put("sharedLanguageCode", ContentField.SHARED_LANGUAGE);
        }
        if (stagingOnly || entryLoadRequest.isStaging() || !ignoreLang) {
            query.append(")");
        }
    }

    public <E> List<E> executeQuery(String queryString, Map parameters) {
        Query query = this.getSession().createQuery(queryString);
        for (Object o : parameters.keySet()) {
            String name = (String) o;
            Object parameter = parameters.get(name);
            if (parameter instanceof Collection) {
                query.setParameterList(name, (Collection) parameter);
            } else if (parameter instanceof Object[]) {
                query.setParameterList(name, (Object[]) parameter);
            } else {
                query.setParameter(name, parameter);
            }
        }
        return query.list();
    }

    public List<Integer> getContainerIDsOnPagesHavingAcls(Set pageIDs, Set aclIDs) {
        Query query = this.getSession().createQuery(
                "select distinct c.comp_id.id from JahiaContainer c where c.pageid in (:pageIDs) and c.jahiaAclId in (:aclIDs) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1");

        query.setParameterList("pageIDs", pageIDs);
        query.setParameterList("aclIDs", aclIDs);

        return query.list();
    }

    public List<Integer> getContainerIDsHavingAcls(Set aclIDs) {
        Query query = this.getSession().createQuery(
                "select distinct c.comp_id.id from JahiaContainer c where c.jahiaAclId in (:aclIDs) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1");

        query.setParameterList("aclIDs", aclIDs);

        return query.list();
    }

    /**
     * Returns a map of acl ids for the given list of ctnIds
     *
     * @param ctnIds
     * @return
     */
    public Map<Integer, Integer> getContainerACLIDs(List ctnIds) {
        Session session = this.getSession();
        if (!session.isOpen()) {
            session = this.getSession(true);
        }
        Criteria criteria = session.createCriteria(JahiaContainer.class);
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.property("comp_id.id"));
        projectionList.add(Projections.property("jahiaAclId"));
        criteria.setProjection(projectionList);
        Property propCrit = Property.forName("comp_id.id");
        criteria.add(propCrit.in(ctnIds));
        List result = criteria.list();
        Map<Integer, Integer> acls = new HashMap<Integer, Integer>();
        Iterator it = result.iterator();
        Object[] row;
        while (it.hasNext()) {
            row = (Object[]) it.next();
            acls.put((Integer)row[0], (Integer)row[1]);
        }
        return acls;
    }

    public Map<String, String> getVersions(int site) {
        final HibernateTemplate template = getHibernateTemplate();
        List items = template.find("select uuid.comp_id.name, uuid.value, version.comp_id.name, version.value " +
                "from JahiaContainerProperty uuid, JahiaContainerProperty version " +
                "where uuid.siteId=? and uuid.comp_id.containerId=version.comp_id.containerId and uuid.comp_id.name='originalUuid' " +
                "and version.comp_id.name='lastImportedVersion'", site);
        Map<String, String> results = new HashMap();
        for (Object item : items) {
            Object[] o = (Object[]) item;
            results.put((String)o[1], (String)o[3]);
        }
        return results;
    }

}

