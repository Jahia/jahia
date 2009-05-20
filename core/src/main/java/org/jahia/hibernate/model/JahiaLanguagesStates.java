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
package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_languages_states" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaLanguagesStates implements Serializable {

    private JahiaLanguagesStatesPK comp_id;

    private Integer workflowState;
    private Integer siteID;

    public JahiaLanguagesStates(JahiaLanguagesStatesPK comp_id, Integer workflowState, Integer siteID) {
        this.comp_id = comp_id;
        this.workflowState = workflowState;
        this.siteID = siteID;
    }

    public JahiaLanguagesStates() {
    }

    /**
     * @hibernate.id generator-class="assigned"
     * @return composite key with object key and language
     */
    public JahiaLanguagesStatesPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(JahiaLanguagesStatesPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="workflow_state"
     * @return the workflow state value
     */
    public Integer getWorkflowState() {
        return workflowState;
    }

    public void setWorkflowState(Integer workflowState) {
        this.workflowState = workflowState;
    }

    /**
     * @hibernate.property column="siteid"
     * @return the site ID
     */
    public Integer getSiteID() {
        return siteID;
    }

    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLanguagesStates castOther = (JahiaLanguagesStates) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }


}
