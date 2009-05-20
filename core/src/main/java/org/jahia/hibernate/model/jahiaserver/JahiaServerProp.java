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
 package org.jahia.hibernate.model.jahiaserver;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 oct. 2005
 * Time: 13:10:48
 *
 * @hibernate.class table="jahia_serverprops"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaServerProp {

    private JahiaServerPropPK comp_id;
    private String value;

    public JahiaServerProp(){

    }
    
    public JahiaServerProp(JahiaServerPropPK comp_id, String value){
        this.comp_id = comp_id;
        this.value = value;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public JahiaServerPropPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(JahiaServerPropPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="propvalue_serverprops"
     * length="250"
     * not-null="true"
     * @return
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaServerProp castOther = (JahiaServerProp) obj;
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
