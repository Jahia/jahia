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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_site_lang_list"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSiteLangList implements Serializable {

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
    private String code;

    /**
     * nullable persistent field
     */
    private Integer rank;

    /**
     * nullable persistent field
     */
    private Boolean activated;

    /**
     * nullable persistent field
     */
    private Boolean mandatory;

    /**
     * full constructor
     */
    public JahiaSiteLangList(Integer id, JahiaSite siteId, String code, Integer rank, Boolean activated,
                             Boolean mandatory) {
        this.id = id;
        this.site = siteId;
        this.code = code;
        this.rank = rank;
        this.activated = activated;
        this.mandatory = mandatory;
    }

    /**
     * default constructor
     */
    public JahiaSiteLangList() {
    }

    /**
     * minimal constructor
     */
    public JahiaSiteLangList(Integer id) {
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
     * @hibernate.property column="code"
     * length="255"
     */
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @hibernate.property column="rank"
     * length="11"
     */
    public Integer getRank() {
        return this.rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    /**
     * @hibernate.property column="activated"
     */
    public Boolean getActivated() {
        return this.activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    /**
     * @hibernate.property column="mandatory"
     */
    public Boolean getMandatory() {
        return this.mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSiteLangList castOther = (JahiaSiteLangList) obj;
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
