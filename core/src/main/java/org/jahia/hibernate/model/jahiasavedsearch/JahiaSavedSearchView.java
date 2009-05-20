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

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Data object for the saved search view settings.
 * @author hollis
 *
 * @hibernate.class table="jahia_savedsearchview"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaSavedSearchView implements Serializable, Cloneable {

    private JahiaSavedSearchViewPK comp_id;

    private String setting;

    private String viewName;

    public JahiaSavedSearchView(){

    }

    public JahiaSavedSearchView(JahiaSavedSearchViewPK comp_id, String setting){
        this.comp_id = comp_id;
        this.setting = setting;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public JahiaSavedSearchViewPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(JahiaSavedSearchViewPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property not-null="true" column="setting_savedsearchview" type="text" length="16777216"
     */
    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    /**
     * @hibernate.property not-null="true" column="name_savedsearchview" length="100"
     */
    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSavedSearchView castOther = (JahiaSavedSearchView) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }

}
