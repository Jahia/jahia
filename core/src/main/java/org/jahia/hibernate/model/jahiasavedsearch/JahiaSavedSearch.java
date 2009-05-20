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
 package org.jahia.hibernate.model.jahiasavedsearch;

import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaSite;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_savedsearch" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSavedSearch implements Serializable, Cloneable {


    /**
     * identifier field
     */
    private Integer id;

    private String title;

    private String descr;

    private String search;

    private Long creationDate = new Long(0);

    private String ownerKey;

    private String searchViewHandlerClass;

    private JahiaSite jahiaSite;

    private JahiaAcl jahiaAcl;

    /**
     *
     */
    public JahiaSavedSearch(){
    }
    
    /**
     * full constructor
     */
    public JahiaSavedSearch( Integer id,
                             String title,
                             String descr,
                             String search,
                             Long creationDate,
                             String ownerKey,
                             String searchViewHandlerClass,
                             JahiaSite jahiaSite,
                             JahiaAcl jahiaAcl) {
        this.id = id;
        this.title = title;
        this.descr = descr;
        this.search = search;
        this.creationDate = creationDate;
        this.ownerKey = ownerKey;
        this.searchViewHandlerClass = searchViewHandlerClass;
        this.jahiaSite = jahiaSite;
        this.jahiaAcl = jahiaAcl;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_savedsearch"
     *
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="title_jahia_savedsearch" type="string"
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @hibernate.property column="descr_jahia_savedsearch" type="string" length="250"
     */
    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    /**
     * @hibernate.property not-null="true" column="search_jahia_savedsearch" type="text" length="16777216"
     */
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * @hibernate.property column="creationdate_jahia_savedsearch"
     * @return
     */
    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @hibernate.property column="owner_jahia_savedsearch"
     */
    public String getOwnerKey() {
        return this.ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    /**
     * @hibernate.property column="class_jahia_savedsearch"
     */
    public String getSearchViewHandlerClass() {
        return searchViewHandlerClass;
    }

    public void setSearchViewHandlerClass(String searchViewHandlerClass) {
        this.searchViewHandlerClass = searchViewHandlerClass;
    }

    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="jahiaid_jahia_savedsearch"
     */
    public JahiaSite getJahiaSite() {
        return this.jahiaSite;
    }

    public void setJahiaSite(JahiaSite jahiaSite) {
        this.jahiaSite = jahiaSite;
    }

    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="rights_jahia_search"
     */
    public JahiaAcl getJahiaAcl() {
        return jahiaAcl;
    }

    public void setJahiaAcl(JahiaAcl jahiaAcl) {
        this.jahiaAcl = jahiaAcl;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSavedSearch castOther = (JahiaSavedSearch) obj;
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

    public Object clone() {
        final JahiaSavedSearch search = new JahiaSavedSearch();
        search.setId(getId());
        search.setTitle(getTitle());
        search.setDescr(getDescr());
        search.setSearch(getSearch());
        search.setCreationDate(getCreationDate());
        search.setOwnerKey(getOwnerKey());
        search.setSearchViewHandlerClass(this.getSearchViewHandlerClass());
        search.setJahiaSite(getJahiaSite());
        search.setJahiaAcl(getJahiaAcl());
        return search;
    }

}
