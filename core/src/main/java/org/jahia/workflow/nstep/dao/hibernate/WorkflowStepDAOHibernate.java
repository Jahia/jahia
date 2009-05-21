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

