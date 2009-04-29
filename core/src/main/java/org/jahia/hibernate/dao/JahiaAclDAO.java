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
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.services.acl.ACLInfo;
import org.jahia.settings.SettingsBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * DAO service for managing Jahia ACL entries.
 * 
 * @author Rincevent
 */
public class JahiaAclDAO extends AbstractGeneratorDAO {
    private Log log = LogFactory.getLog(JahiaAclDAO.class);
    
    private final static String ACLS_ON_PAGE_QUERY_WITH_NONCONTAINER_FIELDS_MYSQL = "from JahiaAcl a where a.parentId in (:aclIDs) and (a.id in (select distinct l.jahiaAclId from JahiaContainerList l where l.pageid = (:pageId) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct c.jahiaAclId from JahiaContainer c where c.pageid = (:pageId) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1) or a.id in (select distinct p.value from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid = (:pageId) and p.comp_id.name like 'view_field_acl_%' and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct f.jahiaAclId from JahiaFieldsData f where f.pageId = (:pageId) and f.comp_id.workflowState >= 1 and f.comp_id.versionId != -1))";
    private final static String ACLS_ON_PAGE_QUERY_MYSQL = "from JahiaAcl a where a.parentId in (:aclIDs) and (a.id in (select distinct l.jahiaAclId from JahiaContainerList l where l.pageid = (:pageId) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct c.jahiaAclId from JahiaContainer c where c.pageid = (:pageId) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1) or a.id in (select distinct p.value from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid = (:pageId) and p.comp_id.name like 'view_field_acl_%' and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1))";

    private final static String ACLS_ON_PAGE_QUERY_WITH_NONCONTAINER_FIELDS = "from JahiaAcl a where a.parentId in (:aclIDs) and (a.id in (select distinct l.jahiaAclId from JahiaContainerList l where l.pageid = (:pageId) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct c.jahiaAclId from JahiaContainer c where c.pageid = (:pageId) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1) or cast(a.id as string) in (select distinct p.value from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid = (:pageId) and p.comp_id.name like 'view_field_acl_%' and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct f.jahiaAclId from JahiaFieldsData f where f.pageId = (:pageId) and f.comp_id.workflowState >= 1 and f.comp_id.versionId != -1))";
    private final static String ACLS_ON_PAGE_QUERY = "from JahiaAcl a where a.parentId in (:aclIDs) and (a.id in (select distinct l.jahiaAclId from JahiaContainerList l where l.pageid = (:pageId) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1) or a.id in (select distinct c.jahiaAclId from JahiaContainer c where c.pageid = (:pageId) and c.comp_id.workflowState >= 1 and c.comp_id.versionId != -1) or cast(a.id as string) in (select distinct p.value from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid = (:pageId) and p.comp_id.name like 'view_field_acl_%' and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1))";


    private static boolean implicitDataConversion = true;
    private static Boolean isMySQLDB = null;


    public List<JahiaAcl> getAcls() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return findByCriteria(template, DetachedCriteria.forClass(
                JahiaAcl.class).setFetchMode("entries", FetchMode.JOIN));
    }
    
    public List<JahiaAcl> getChildAcls(Integer parentId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return findByCriteria(template, DetachedCriteria.forClass(
                JahiaAcl.class).add(Restrictions.eq("parentId", parentId)));
    }    
    
    public List<JahiaAcl> getChildAclsOnPage(List<Integer> parentAclIds, int pageId) {
        List<JahiaAcl> result = null;
        SettingsBean settings = org.jahia.settings.SettingsBean.getInstance();
        final HibernateTemplate template = getHibernateTemplate();
        String hql = settings.areDeprecatedNonContainerFieldsUsed() ? ACLS_ON_PAGE_QUERY_WITH_NONCONTAINER_FIELDS_MYSQL
                : ACLS_ON_PAGE_QUERY_MYSQL;
        try {
            if (implicitDataConversion) {
                if (isMySQLDB == null) {
                    isMySQLDB = new Boolean(getSession().connection()
                            .getMetaData().getDatabaseProductName()
                            .toLowerCase().indexOf("mysql") >= 0);
                }
                if (!isMySQLDB.booleanValue()) {
                    hql = settings.areDeprecatedNonContainerFieldsUsed() ? ACLS_ON_PAGE_QUERY_WITH_NONCONTAINER_FIELDS
                            : ACLS_ON_PAGE_QUERY;
                }
            }
        } catch (SQLException e) {
        }
        try {
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            template.setCacheQueries(true);
            int maxElementsForInClause = settings.getDBMaxElementsForInClause();

            if (parentAclIds.size() > maxElementsForInClause) {
                for (int fromIndex = 0; fromIndex <= parentAclIds.size(); fromIndex += maxElementsForInClause) {
                    int toIndex = fromIndex + maxElementsForInClause;
                    if (toIndex >= parentAclIds.size()) {
                        toIndex = parentAclIds.size();
                    }

                    if (fromIndex == 0) {
                        result = template.findByNamedParam(hql, new String[] {
                                "pageId", "aclIDs" }, new Object[] {
                                new Integer(pageId),
                                parentAclIds.subList(fromIndex, toIndex) });
                    } else {
                        result.addAll(template.findByNamedParam(hql,
                                new String[] { "pageId", "aclIDs" },
                                new Object[] {
                                    new Integer(pageId),
                                    parentAclIds.subList(fromIndex,
                                        toIndex) }));
                    }
                }
            } else {
                result = template.findByNamedParam(hql, new String[] {
                        "pageId", "aclIDs" }, new Object[] {
                        new Integer(pageId), parentAclIds });
            }
        } catch (HibernateException e) {
            if(implicitDataConversion) {
                implicitDataConversion=false;
                result = getChildAclsOnPage(parentAclIds, pageId);
            } else {
                logger.error("Error during getChildsAclsOnPage",e);
            }
        }

        return result;
    }        

    public JahiaAcl findAclById(Integer id) {
        JahiaAcl acl = null;
        if (id != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            acl = (JahiaAcl) findByCriteriaUnique(template, DetachedCriteria
                    .forClass(JahiaAcl.class).add(Restrictions.idEq(id))
                    .setFetchMode("entries", FetchMode.JOIN));
            if (acl == null) {
                throw new ObjectRetrievalFailureException(JahiaAcl.class, id);
            }
        }
        return acl;
    }

    public JahiaAcl findLazyAclById(Integer id) {
        JahiaAcl acl = null;
        if (id != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            acl = (JahiaAcl) template.load(JahiaAcl.class, id);
            if (acl == null) {
                throw new ObjectRetrievalFailureException(JahiaAcl.class, id);
            }
        }
        return acl;
    }

    public void removeAcl(Integer id) {
        JahiaAcl acl = null;
        try {
            acl = findAclById(id);            
        } catch (ObjectRetrievalFailureException e) {
            logger.info("ACL with ID: " + id + " not found. Skipping deleting.");
        }
        if (acl != null) {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaAcl a where a.parentId=?",id));
            hibernateTemplate.delete(acl);
            hibernateTemplate.flush();
        }
    }

    public void removeGroupAclEntries(String name) {
        List list = findByTargetName(name, ACLInfo.GROUP_TYPE_ENTRY);
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(list);
    }

    public void removeUserAclEntries(String name) {
        List list = findByTargetName(name, ACLInfo.USER_TYPE_ENTRY);
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(list);
    }

    public boolean isGroupUsedInAclEntries(String groupName) {
        List list = findByTargetName(groupName, ACLInfo.GROUP_TYPE_ENTRY);
        return list.size()>0;
    }

    public void saveAcl(JahiaAcl acl) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        try {
            if (acl.getId() == null) {
                acl.setId(getNextInteger(acl));
                hibernateTemplate.save(acl);
                updateEntries(acl, hibernateTemplate);
            } else {
                updateAcl(acl);
            }
            hibernateTemplate.flush();
        } catch (HibernateException e) {
            log.warn("Exception during save of acl " + acl, e);
        }
    }

    public void updateAcl(JahiaAcl acl) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        try {
            updateEntries(acl, hibernateTemplate);
            hibernateTemplate.merge(acl);
            hibernateTemplate.flush();
        } catch (HibernateException e) {
            log.warn("Exception during save of acl " + acl, e);
        }
    }

    private List findByTargetName(String name, int type) {
        String hql = "from JahiaAclEntry a where a.comp_id.target = ? and a.comp_id.type=" + type;
        List ret = null;
        if (name != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            ret = template.find(hql, name);
        }
        return ret;
    }

    public Collection<String> findAllTarget(int type) {
        String hql = "select distinct a.comp_id.target from JahiaAclEntry a where a.comp_id.type=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Integer(type));
    }

    private void saveOrUpdateEntry(final Map userEntries, String key, HibernateTemplate hibernateTemplate)
            throws HibernateException {
        JahiaAclEntry jahiaAclEntry = (JahiaAclEntry) userEntries.get(key);
        hibernateTemplate.save(jahiaAclEntry);
    }

    private void updateEntries(JahiaAcl acl, HibernateTemplate hibernateTemplate) throws HibernateException {
        final Map userEntries = acl.getUserEntries();
        hibernateTemplate.setCheckWriteOperations(false);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaAclEntry a where a.comp_id.id=?",acl));
        if (userEntries != null) {
            Set entries = userEntries.keySet();
            Iterator iterator = entries.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                saveOrUpdateEntry(userEntries, key, hibernateTemplate);
            }
        }
        final Map groupEntries = acl.getGroupEntries();

        if (groupEntries != null) {
            Set entries = acl.getGroupEntries().keySet();
            Iterator iterator = entries.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                saveOrUpdateEntry(groupEntries, key, hibernateTemplate);
            }
        }
    }
}