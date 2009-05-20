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
import org.jahia.workflow.nstep.model.WorkflowStep;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * This class interacts with Spring and Hibernate to save and
 * retrieve User objects.
 *
 * @author Matt Raible
 */
public class WorkflowStepDAOHibernate extends AbstractGeneratorDAO
        implements org.jahia.workflow.nstep.dao.WorkflowStepDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<WorkflowStep> getWorkflowSteps() {
        return getHibernateTemplate().find("from WorkflowStep");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowStepDAO ---------------------

    public org.jahia.workflow.nstep.model.WorkflowStep getWorkflowStep(Long workflowId) {
        WorkflowStep workflow = (WorkflowStep) getHibernateTemplate().get(WorkflowStep.class, workflowId);
        if (workflow == null) {
            throw new ObjectRetrievalFailureException(org.jahia.workflow.nstep.model.WorkflowStep.class, workflowId);
        }
        return workflow;
    }

    public void removeWorkflowStep(Long workflowId) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getWorkflowStep(workflowId));
    }

    public void saveWorkflowStep(org.jahia.workflow.nstep.model.WorkflowStep workflow) {
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

