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

