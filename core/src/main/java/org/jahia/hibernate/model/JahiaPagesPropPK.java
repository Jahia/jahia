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
import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaPagesPropPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private Integer pageId;

    /**
     * identifier field
     */
    private String name;

    /**
     * identifier field
     */
    private String languageCode;

    /**
     * full constructor
     */
    public JahiaPagesPropPK(Integer pageId, String propName, String languageCode) {
        this.pageId = pageId;
        this.name = propName;
        this.languageCode = languageCode;
    }

    /**
     * default constructor
     */
    public JahiaPagesPropPK() {
    }

    /**
     * @hibernate.property column="page_id"
     * length="11"
     */
    public Integer getPageId() {
        return this.pageId;
    }

    public void setPageId(Integer pageId) {
        updated();
        this.pageId = pageId;
    }

    /**
     * @hibernate.property column="prop_name"
     * length="150"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        updated();
        this.name = name;
    }

    /**
     * @hibernate.property column="language_code"
     * length="100"
     */
    public String getLanguageCode() {
        return this.languageCode;
    }

    public void setLanguageCode(String languageCode) {
        updated();
        this.languageCode = languageCode;
    }

    public String effectiveToString() {
        return new StringBuffer(getClass().getName())
                .append("pageId="+getPageId())
                .append("name="+getName())
                .append("languageCode="+getLanguageCode())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaPagesPropPK castOther = (JahiaPagesPropPK) obj;
            return new EqualsBuilder()
                .append(this.getPageId(), castOther.getPageId())
                .append(this.getName(), castOther.getName())
                .append(this.getLanguageCode(), castOther.getLanguageCode())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getPageId())
                .append(getName())
                .append(getLanguageCode())
                .toHashCode();
    }

}
