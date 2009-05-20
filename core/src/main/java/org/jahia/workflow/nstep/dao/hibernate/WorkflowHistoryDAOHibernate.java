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
 package org.jahia.workflow.nstep.dao.hibernate;

import org.jahia.hibernate.dao.AbstractGeneratorDAO;
import org.jahia.workflow.nstep.model.WorkflowHistoryEntry;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 déc. 2004
 * Time: 17:58:45
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowHistoryDAOHibernate extends AbstractGeneratorDAO
        implements org.jahia.workflow.nstep.dao.WorkflowHistoryDAO {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowHistoryDAO ---------------------

    public org.jahia.workflow.nstep.model.WorkflowHistoryEntry getWorkflowHistory(Long historyId) {
        return (WorkflowHistoryEntry) getHibernateTemplate().get(WorkflowHistoryEntry.class, historyId);
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByAuthor(String author) {
        return getHibernateTemplate().find(
                "from WorkflowHistoryEntry w where w.author='" + author + "' order by w.date desc");
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey) {
        return getHibernateTemplate().find(
                "from WorkflowHistoryEntry w where w.objectKey='" + objectKey + "' order by w.date desc");
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByProcess(String process) {
        return getHibernateTemplate().find(
                "from WorkflowHistoryEntry w where w.process='" + process + "' order by w.date desc");
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByUser(String user) {
        return getHibernateTemplate().find("from WorkflowHistoryEntry w where w.user='" + user +
                                           "' order by w.date desc");
    }

    public void removeWorkflowHistory(Long historyId) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getWorkflowHistory(historyId));
    }

    public void removeWorkflowHistory(String objectKey) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.deleteAll(getHibernateTemplate().find("from WorkflowHistoryEntry where objectKey=?",objectKey));
    }

    public void saveWorkflowHistory(org.jahia.workflow.nstep.model.WorkflowHistoryEntry history) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (history.getId() == null) {
            history.setId(getNextLong(history));
        }
        hibernateTemplate.save(history);
        if (logger.isDebugEnabled()) {
            logger.debug("userId set to: " + history.getId());
        }
    }
}

