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
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_workflow" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaWorkflow implements Serializable {

    /**
     * identifier field
     */
    private String objectkey;

    /**
     * nullable persistent field
     */
    private Integer mode;

    /**
     * nullable persistent field
     */
    private String externalname;

    /**
     * nullable persistent field
     */
    private String externalprocess;

    /**
     * nullable persistent field
     */
    private String mainObjectkey;

    /**
     * nullable persistent field
     */
    private Integer state;

    /**
     * full constructor
     */
    public JahiaWorkflow(String objectkey, Integer mode, String externalname, String externalprocess) {
        this.objectkey = objectkey;
        this.mode = mode;
        this.externalname = externalname;
        this.externalprocess = externalprocess;
    }

    /**
     * default constructor
     */
    public JahiaWorkflow() {
    }

    /**
     * minimal constructor
     */
    public JahiaWorkflow(String objectkey) {
        this.objectkey = objectkey;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.String"
     * column="OBJECTKEY"
     */
    public String getObjectkey() {
        return this.objectkey;
    }

    public void setObjectkey(String objectkey) {
        this.objectkey = objectkey;
    }

    /**
     * @hibernate.property column="MODEVALUE"
     * length="11"
     */
    public Integer getMode() {
        return this.mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    /**
     * @hibernate.property column="EXTERNALNAME"
     * length="255"
     */
    public String getExternalname() {
        return this.externalname;
    }

    public void setExternalname(String externalname) {
        this.externalname = externalname;
    }

    /**
     * @hibernate.property column="EXTERNALPROCESS"
     * length="255"
     */
    public String getExternalprocess() {
        return this.externalprocess;
    }

    public void setExternalprocess(String externalprocess) {
        this.externalprocess = externalprocess;
    }

//    /**
//     * @hibernate.property column="STATE"
//     */


    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * @hibernate.property column="MAINOBJECTKEY"
     * length="255"
     */
    public String getMainObjectkey() {
        return this.mainObjectkey;
    }

    public void setMainObjectkey(String objectkey) {
        this.mainObjectkey = objectkey;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("objectkey", getObjectkey())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaWorkflow castOther = (JahiaWorkflow) obj;
            return new EqualsBuilder()
                .append(this.getObjectkey(), castOther.getObjectkey())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getObjectkey())
                .toHashCode();
    }

}
