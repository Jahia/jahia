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

import java.util.List;

import org.jahia.hibernate.dao.AbstractGeneratorDAO;
import org.jahia.workflow.nstep.dao.WorkflowDAO;
import org.jahia.workflow.nstep.model.Workflow;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * This class interacts with Spring and Hibernate to save and
 * retrieve User objects.
 *
 * @author Matt Raible
 */
public class WorkflowDAOHibernate extends AbstractGeneratorDAO implements WorkflowDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Workflow> getWorkflows() {
        return getHibernateTemplate().find("from Workflow");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowDAO ---------------------

    public Workflow getWorkflowById(Long workflowId) {
        Workflow workflow = (Workflow) getHibernateTemplate().get(
                Workflow.class, workflowId);
        if (workflow == null) {
            throw new ObjectRetrievalFailureException(Workflow.class, workflowId);
        }
        return workflow;
    }

    public Workflow getWorkflowByName(String workflowName) {
        List<Workflow> list = getHibernateTemplate().find("from Workflow wf where wf.name='" + workflowName + "'");
        Workflow workflow = null;
        if (list.size() > 0) {
            workflow = (Workflow) list.get(0);
        }
        if (workflow == null) {
            throw new ObjectRetrievalFailureException(Workflow.class, workflowName);
        }
        return workflow;
    }

    public void removeWorkflow(Long workflowId) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getWorkflowById(workflowId));
    }

    public void saveWorkflow(Workflow workflow) {
        if (workflow.getId() == null) {
            workflow.setId(getNextLong(workflow));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(workflow);

        if (logger.isDebugEnabled()) {
            logger.debug("userId set to: " + workflow.getId());
        }
    }
}

