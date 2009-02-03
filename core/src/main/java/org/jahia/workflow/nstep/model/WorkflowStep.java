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

