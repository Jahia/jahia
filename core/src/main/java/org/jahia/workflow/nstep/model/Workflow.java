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

import java.util.List;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Describe a workflow and its steps.
 *
 * @hibernate.class table="jahia_nstep_workflow" lazy="false"
 */
public class Workflow extends org.jahia.workflow.nstep.model.BaseObject {

    private static final long serialVersionUID = -3487236325250456723L;
// ------------------------------ FIELDS ------------------------------

    private List<WorkflowStep> steps;
    private Long id;
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
     * @return The list of steps
     *
     * @hibernate.list cascade="all" lazy="false"
     * @hibernate.collection-index column="step_index" type="int"
     * @hibernate.collection-key column="workflow_id"
     * @hibernate.collection-one-to-many class="org.jahia.workflow.nstep.model.WorkflowStep"
     */
    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    public boolean isLastStep(String actionName) {
        WorkflowStep lastStep = (WorkflowStep) steps.get(steps.size()-1);
        return lastStep.getName().equals(actionName);
    }
}

