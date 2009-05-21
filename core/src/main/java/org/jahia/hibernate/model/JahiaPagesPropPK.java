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
