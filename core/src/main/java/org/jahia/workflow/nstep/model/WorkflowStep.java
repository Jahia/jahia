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
 package org.jahia.workflow.nstep.model;

import java.util.Set;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Represent a step in a workflow.
 *
 * @hibernate.class table="jahia_nstep_workflowstep" lazy="false"
 */
public class WorkflowStep extends BaseObject {

    private static final long serialVersionUID = 432751511246094455L;
// ------------------------------ FIELDS ------------------------------

    private Long id;
    private Set<WorkflowInstance> workflowInstances;
    private String name;

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return Returns the id.
     *
     * @hibernate.id column="id" generator-class="assigned" unsaved-value="null"
     *
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Returns the name
     *
     * @hibernate.property column="name" not-null="true"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return set of WorkflowInstances
     *
     * @hibernate.set lazy="true" inverse="true" cascade="all"
     * @hibernate.collection-key column="step"
     * @hibernate.collection-one-to-many class="org.jahia.workflow.nstep.model.WorkflowInstance"
     */
    public Set<WorkflowInstance> getWorkflowInstances() {
        return workflowInstances;
    }

    public void setWorkflowInstances(Set<WorkflowInstance> workflowInstances) {
        this.workflowInstances = workflowInstances;
    }
}

