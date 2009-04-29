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
 * @hibernate.class table="jahia_site_lang_maps"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSiteLangMap implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private JahiaSite site;

    /**
     * nullable persistent field
     */
    private String fromLanguageCode;

    /**
     * nullable persistent field
     */
    private String toLanguageCode;

    /**
     * full constructor
     */
    public JahiaSiteLangMap(Integer id, JahiaSite siteId, String fromLangCode, String toLangCode) {
        this.id = id;
        this.site = siteId;
        this.fromLanguageCode = fromLangCode;
        this.toLanguageCode = toLangCode;
    }

    /**
     * default constructor
     */
    public JahiaSiteLangMap() {
    }

    /**
     * minimal constructor
     */
    public JahiaSiteLangMap(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.many-to-one update="true" insert="true" column="site_id" 
     */
    public org.jahia.hibernate.model.JahiaSite getSite() {
        return this.site;
    }

    public void setSite(org.jahia.hibernate.model.JahiaSite site) {
        this.site = site;
    }

    /**
     * @hibernate.property column="from_lang_code"
     * length="255"
     */
    public String getFromLanguageCode() {
        return this.fromLanguageCode;
    }

    public void setFromLanguageCode(String fromLanguageCode) {
        this.fromLanguageCode = fromLanguageCode;
    }

    /**
     * @hibernate.property column="to_lang_code"
     * length="255"
     */
    public String getToLanguageCode() {
        return this.toLanguageCode;
    }

    public void setToLanguageCode(String toLanguageCode) {
        this.toLanguageCode = toLanguageCode;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSiteLangMap castOther = (JahiaSiteLangMap) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

}
