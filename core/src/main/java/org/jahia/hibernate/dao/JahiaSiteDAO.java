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

 package org.jahia.hibernate.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.jahia.content.ContentDefinitionKey;
import org.jahia.content.CrossReferenceManager;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaSite;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.dao.WorkflowHistoryDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.util.*;

/**
 * DAO pattern.
 * No $plugin.mergedir/hibernate-dao-ALL-class-comments.txt found.
 * No $plugin.mergedir/hibernate-dao-jahia_sites-class-comments.txt found.
 */
public class JahiaSiteDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(JahiaSiteDAO.class);

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<JahiaSite> getSites() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<JahiaSite> retval = template.find("from JahiaSite");
        return retval;
    }

    public int getNbSites() {
        Long retval = null;
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        retval = (Long) template.find("select count(*) from JahiaSite").get(0);
        return retval.intValue();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Finder:
     *
     * @param active The search criteria.
     */
    public List<JahiaSite> findByActive(Integer active) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return active == null ? template
                .find("from JahiaSite where active IS NULL") : template.find(
                "from JahiaSite where active = ?", active);
        }

    /**
     * Finder:
     *
     * @param defaultpageid The search criteria.
     */
    public List<JahiaSite> findByDefaultpageid(Integer defaultpageid) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return defaultpageid == null ? template
                .find("from JahiaSite where defaultpageid IS NULL") : template
                .find("from JahiaSite where defaultpageid = ?", defaultpageid);
    }

    /**
     * Finder:
     *
     * @param defaulttemplateid The search criteria.
     */
    public List<JahiaSite> findByDefaulttemplateid(Integer defaulttemplateid) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return defaulttemplateid == null ? template
                .find("from JahiaSite where defaulttemplateid IS NULL")
                : template.find("from JahiaSite where defaulttemplateid = ?",
                        defaulttemplateid);
        }

    /**
     * Finder:
     *
     * @param id The search criteria.
     */
    public JahiaSite findById(java.lang.Integer id) {
        JahiaSite retval = null;
        if (id == null) {
            throw new RuntimeException("Error: Cannot use null in query for unique column");
        }
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            retval = (JahiaSite) template.get(JahiaSite.class, id);
            if (retval == null) {
                throw new ObjectRetrievalFailureException(JahiaSite.class, id);
            }

        return retval;
    }

    /**
     * Finder:
     *
     * @param key The search criteria.
     */
    public JahiaSite findByKey(String key) {
        JahiaSite retval = null;
        if (key == null) {
            throw new RuntimeException("Error: Cannot use null in query for unique column");
        }
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
        List<JahiaSite> objects = template.find(
                "from JahiaSite where key = ?", key);
            if (objects.size() == 1) {
                retval = (JahiaSite) objects.get(0);
            } else if (objects.size() > 1) {
            throw new RuntimeException(
                    "Error: multiple values returned for unique column:"
                            + objects);
            } else {
                throw new ObjectRetrievalFailureException(JahiaSite.class, key);
            }

        return retval;
    }

    /**
     * Finder:
     *
     * @param servername The search criteria.
     */
    public JahiaSite findByServername(String servername) {
        JahiaSite retval = null;
        if (servername == null) {
            throw new RuntimeException("Error: Cannot use null in query for unique column");
        }
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
        List<JahiaSite> objects = template
                .find("from JahiaSite where servername = ?",
                        servername);
            if (objects.size() == 1) {
                retval = (JahiaSite) objects.get(0);
            } else if (objects.size() > 1) {
            throw new RuntimeException(
                    "Error: multiple values returned for unique column:"
                            + objects);
            } else {
            throw new ObjectRetrievalFailureException(JahiaSite.class,
                    servername);
        }

        return retval;
    }

    /**
     * Finder:
     *
     * @param title The search criteria.
     */
    public List<JahiaSite> findByTitle(String title) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return title == null ? template
                .find("from JahiaSite where title IS NULL") : template.find(
                "from JahiaSite where title = ?", title);
        }

    /**
     * Finder:
     *
     * @param tplDeploymode The search criteria.
     */
    public List<JahiaSite> findByTplDeploymode(Integer tplDeploymode) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return tplDeploymode == null ? template
                .find("from JahiaSite where tplDeploymode IS NULL") : template
                .find("from JahiaSite where tplDeploymode = ?", tplDeploymode);
        }

    /**
     * Finder:
     *
     * @param webappsDeploymode The search criteria.
     */
    public List<JahiaSite> findByWebappsDeploymode(Integer webappsDeploymode) throws HibernateException {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return webappsDeploymode == null ? template
                .find("from JahiaSite where webappsDeploymode IS NULL")
                : template.find("from JahiaSite where webappsDeploymode = ?",
                        webappsDeploymode);
        }

    public void remove(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        Map<Serializable, Integer> acls = new HashMap<Serializable, Integer>(1024);
        List<ContentDefinitionKey> definitions = new ArrayList<ContentDefinitionKey>(1024);
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        JahiaAclDAO aclDAO = (JahiaAclDAO) context.getBean("jahiaAclDAO");
        JahiaAclNamesDAO aclNamesDAO = (JahiaAclNamesDAO) context.getBean("jahiaAclNamesDAO");
        JahiaBigTextDataDAO bigTextDataDAO = (JahiaBigTextDataDAO) context.getBean("jahiaBigTextDataDAO");
        JahiaContainerDAO containerDAO = (JahiaContainerDAO) context.getBean("jahiaContainerDAO");
        JahiaContainerDefinitionDAO containerDefinitionDAO = (JahiaContainerDefinitionDAO) context.getBean("jahiaContainerDefinitionDAO");
        JahiaContainerListDAO containerListDAO = (JahiaContainerListDAO) context.getBean("jahiaContainerListDAO");
        JahiaFieldsDataDAO fieldsDataDAO = (JahiaFieldsDataDAO) context.getBean("jahiaFieldsDataDAO");
        JahiaFieldsDefinitionDAO fieldsDefinitionDAO = (JahiaFieldsDefinitionDAO) context.getBean("jahiaFieldsDefinitionDAO");
        JahiaObjectDAO objectDAO = (JahiaObjectDAO) context.getBean("jahiaObjectDAO");
        JahiaPagesDAO pagesDAO = (JahiaPagesDAO) context.getBean("jahiaPagesDAO");
        JahiaPagesDefinitionDAO pagesDefinitionDAO = (JahiaPagesDefinitionDAO) context.getBean("jahiaPagesDefinitionDAO");
        JahiaSiteLanguageMappingDAO languageMappingDAO = (JahiaSiteLanguageMappingDAO) context.getBean("jahiaSiteLanguageMappingDAO");
        JahiaSiteLanguageListDAO languageListDAO = (JahiaSiteLanguageListDAO) context.getBean("jahiaSiteLanguageListDAO");
        JahiaSitePropertyDAO sitePropertyDAO = (JahiaSitePropertyDAO) context.getBean("jahiaSitePropertyDAO");
        JahiaUserDAO userDAO = (JahiaUserDAO) context.getBean("jahiaUserDAO");
        JahiaGroupDAO groupDAO = (JahiaGroupDAO) context.getBean("jahiaGroupDAO");
        JahiaGroupAccessDAO groupAccessDAO = (JahiaGroupAccessDAO) context.getBean("jahiaGroupAccessDAO");
        JahiaSavedSearchDAO savedSearchDAO = (JahiaSavedSearchDAO) context.getBean("jahiaSavedSearchDAO");
        JahiaWorkflowDAO jahiaWorkflowDAO = (JahiaWorkflowDAO) context.getBean("jahiaWorkflowDAO");
        JahiaLanguagesStatesDAO jahiaLanguagesStatesDAO = (JahiaLanguagesStatesDAO) context.getBean("jahiaLanguagesStatesDAO");
        JahiaFieldXRefDAO jahiaFieldXRefDAO = (JahiaFieldXRefDAO) context.getBean("jahiaFieldXRefDAO");

        WorkflowInstanceDAO workflowInstanceDAO = (WorkflowInstanceDAO) context.getBean("nstepWorkflowInstanceDAO");
        WorkflowHistoryDAO workflowHistoryDAO = (WorkflowHistoryDAO) context.getBean("nstepWorkflowHistoryDAO");

        // flush all audit log entries for site
        ((JahiaAuditLogDAO) context.getBean("jahiaAuditLogDAO"))
                .flushSiteLogs(findById(siteID).getKey());        
        
        // We delete All fields
        Map<Serializable, Integer> map = fieldsDataDAO.deleteAllFieldsFromSite(siteID);
        acls.putAll(map);
        List<? extends ContentDefinitionKey> list = fieldsDefinitionDAO.deleteAllDefinitionsFromSite(siteID);
        definitions.addAll(list);
        // We delete all containers
        map = containerDAO.deleteAllContainersFromSite(siteID);
        acls.putAll(map);
        // We delete all containerLists
        map = containerListDAO.deleteAllListsFromSite(siteID);
        acls.putAll(map);
        list = containerDefinitionDAO.deleteAllDefinitionsFromSite(siteID);
        definitions.addAll(list);
        // We delete all pages
        map = pagesDAO.deleteAllPagesFromSite(siteID);
        acls.putAll(map);
        list = pagesDefinitionDAO.deleteAllTemplatesFromSite(siteID);
        definitions.addAll(list);
        // We delete all links
        CrossReferenceManager instance = CrossReferenceManager.getInstance();
        for (Iterator<?> iterator = acls.keySet().iterator(); iterator.hasNext(); ) {
            Object o = iterator.next();
            if (o instanceof ObjectKey) {
                ObjectKey objectKey = (ObjectKey) o;
                try {
                    instance.removeAllObjectXRefs(objectKey);
                    jahiaWorkflowDAO.delete(objectKey.toString());
                    workflowHistoryDAO.removeWorkflowHistory(objectKey.toString());
                    workflowInstanceDAO.removeWorkflowInstance(objectKey.toString());

                    for (String s : groupDAO.searchGroupNameInJahiaGrp("workflowrole_" + objectKey.toString()+"_%", null)) {
                        groupDAO.delete(s);
                        groupAccessDAO.delete(s);
                    }
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        for (ObjectKey definitionKey : definitions) {
            try {
                instance.removeAllObjectXRefs(definitionKey);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        // We delete all bigtext
        bigTextDataDAO.deleteAllFromSite(siteID);
        // We delete all objects
        objectDAO.deleteAllFromSite(siteID);

        jahiaFieldXRefDAO.deleteFromSite(siteID);

        jahiaLanguagesStatesDAO.clearEntriesForSite(siteID);
        // We delete all langs (list maps)
        languageListDAO.deleteAllFromSite(siteID);
        languageMappingDAO.deleteAllFromSite(siteID);
        // We delete all site properties
        sitePropertyDAO.deleteAllFromSite(siteID);
        // We delete all saved search
        map = savedSearchDAO.deleteAllFromSite(siteID);
        acls.putAll(map);

        // We delete all Aclnames
        acls.putAll(aclNamesDAO.removeBySiteID(siteID));
        // We delete all Acls
        List<Integer> collection = new ArrayList<Integer>(acls.values());
        Collections.sort(collection);
        for (int i = collection.size() - 1; i >= 0; i--) {
            Integer aclID = collection.get(i);
            try {
                aclDAO.removeAcl(aclID);
            } catch (Exception e) {
                ;
            }
        }
        // We delete all groups
        for (String groupKey : groupDAO.getGroupKeys(siteID)) {
            groupAccessDAO.delete(groupKey);
        }

        for (String groupKey : groupDAO.deleteAllFromSite(siteID)) {
            aclDAO.removeGroupAclEntries(groupKey);            
        }
        // We delete all users
        for (String userKey : userDAO.deleteAllFromSite(siteID)) {
            aclDAO.removeUserAclEntries(userKey);
        }
        template.delete(findById(siteID));
    }

    /**
     * Insert the object into the database.
     *
     * @param obj The object to save.
     *
     * @return The primary key of the newly inserted object.
     */
    public JahiaSite save(JahiaSite obj) {
        if (obj.getId() == null) {
            obj.setId(getNextInteger(obj));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(obj);
        if (log.isDebugEnabled()) {
            log.debug("Saved JahiaSite id set to " + obj.getId());
        }
        return obj;
    }

    public JahiaSite update(JahiaSite obj) {
        if (obj.getId().intValue() > 0) {
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.merge(obj);
        }
        return obj;
    }

    public JahiaSite getDefaultSite() {
        JahiaSite retval = null;
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<JahiaSite> objects = template.find("from JahiaSite where defaultSite = ?",
                Boolean.TRUE);
        if (objects.size() == 1) {
            retval = objects.get(0);
        }
        return retval;
    }

    public void setDefaultSite(JahiaSite site) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        for (JahiaSite jahiaSite : getSites()) {
            if(site != null && jahiaSite.getId().equals(site.getId())) {
                jahiaSite.setDefaultSite(Boolean.TRUE);
            } else {
                jahiaSite.setDefaultSite(Boolean.FALSE);
            }
            template.merge(jahiaSite);
        }
    }
}