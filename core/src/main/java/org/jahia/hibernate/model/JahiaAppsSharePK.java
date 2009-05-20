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
 * @author Hibernate CodeGenerator
 */
public class JahiaAppsSharePK extends CachedPK implements Serializable {


    /**
     * identifier field
     */
    private JahiaSite site;
    private JahiaAppDef definition;

    /**
     * full constructor
     */
    public JahiaAppsSharePK(JahiaAppDef idApsAppid, JahiaSite idApsSiteid) {
        this.definition = idApsAppid;
        this.site = idApsSiteid;
    }

    /**
     * default constructor
     */
    public JahiaAppsSharePK() {
    }

    /**
     * @hibernate.many-to-one update="false"
     * insert="false"
     * @hibernate.column name="id_aps_appid"
     */
    public org.jahia.hibernate.model.JahiaAppDef getDefinition() {
        return this.definition;
    }

    public void setDefinition(org.jahia.hibernate.model.JahiaAppDef definition) {
        updated();
        this.definition = definition;
    }

    /**
     * @hibernate.many-to-one not-null="true"
     * @hibernate.column name="id_aps_siteid"
     */
    public JahiaSite getSite() {
        return this.site;
    }

    public void setSite(JahiaSite site) {
        updated();
        this.site = site;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("definition", getDefinition())
                .append("site", getSite())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAppsSharePK castOther = (JahiaAppsSharePK) obj;
            return new EqualsBuilder()
                .append(this.getDefinition(), castOther.getDefinition())
                .append(this.getSite(), castOther.getSite())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getDefinition())
                .append(getSite())
                .toHashCode();
    }

}
