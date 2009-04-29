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

