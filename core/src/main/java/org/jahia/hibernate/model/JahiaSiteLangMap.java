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
