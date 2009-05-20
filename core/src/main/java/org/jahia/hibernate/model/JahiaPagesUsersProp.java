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
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 10:05:02
 */

/**
 * @hibernate.class table="jahia_pages_users_prop"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaPagesUsersProp implements Serializable {
    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaPagesUsersPropPK comp_id;

    /**
     * nullable persistent field
     */
    private String value;

    public JahiaPagesUsersProp() {
    }

    /**
     * full constructor
     */
    public JahiaPagesUsersProp(org.jahia.hibernate.model.JahiaPagesUsersPropPK comp_id, String propValue) {
        this.comp_id = comp_id;
        this.value = propValue;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public JahiaPagesUsersPropPK getComp_id() {
        return comp_id;
    }

    public void setComp_id(JahiaPagesUsersPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="prop_value"
     * length="255"
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        if (this.getComp_id() != null) {
            return getComp_id().getName();
        }
        return null;
    }

    public int getPageId() {
        if (this.getComp_id() != null) {
            Integer i = getComp_id().getPageId();
            if (i != null) {
                return i.intValue();
            }
        }
        return -1;
    }

    public String getPrincipalKey() {
        if (this.getComp_id() != null) {
            return this.getComp_id().getPrincipalKey();
        }
        return null;
    }

    public String getPrincipalType() {
        if (this.getComp_id() != null) {
            return this.getComp_id().getPrincipalType();
        }
        return null;
    }

    public String getPropType() {
        if (this.getComp_id() != null) {
            return this.getComp_id().getPropType();
        }
        return null;
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("comp_id=" + getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaPagesUsersProp castOther = (JahiaPagesUsersProp) obj;
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
