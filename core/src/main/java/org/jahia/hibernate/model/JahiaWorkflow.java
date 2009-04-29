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
