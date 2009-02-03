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
