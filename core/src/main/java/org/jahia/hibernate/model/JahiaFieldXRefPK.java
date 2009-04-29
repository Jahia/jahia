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

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 juil. 2007
 * Time: 17:11:59
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldXRefPK extends CachedPK implements Serializable {
    private int fieldId;
    private String language;
    private Integer workflow;
    private String target;



    
    public JahiaFieldXRefPK() {
    }

    public JahiaFieldXRefPK(int fieldId, String language, Integer workflow, String target) {
        this.fieldId = fieldId;
        this.language = language;
        this.workflow = workflow;
        this.target = target;
    }


    /**
     * @hibernate.property
     */
    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * @hibernate.property length="10"
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @hibernate.property
     */
    public Integer getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Integer workflow) {
        this.workflow = workflow;
    }

    /**
     * @hibernate.property length="255"
     */
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JahiaFieldXRefPK that = (JahiaFieldXRefPK) o;

        if (fieldId != that.fieldId) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (workflow != null ? !workflow.equals(that.workflow) : that.workflow != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = fieldId;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (workflow != null ? workflow.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("fieldId", getFieldId())
                .append("language", getLanguage())
                .append("workflow", getWorkflow())
                .append("filepath", getTarget())
                .toString();
    }
}
