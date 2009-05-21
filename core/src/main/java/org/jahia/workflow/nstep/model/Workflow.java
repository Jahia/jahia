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

