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
