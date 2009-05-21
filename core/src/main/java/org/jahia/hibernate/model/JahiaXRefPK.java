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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 juil. 2007
 * Time: 17:11:59
 * To change this template use File | Settings | File Templates.
 */
public class JahiaXRefPK extends CachedPK implements Serializable {
    private Integer pageId;
    private Integer refId;
    private Integer refType;


    public JahiaXRefPK() {
    }

    public JahiaXRefPK(Integer pageId, Integer refId, Integer refType) {
        this.pageId = pageId;
        this.refId = refId;
        this.refType = refType;
    }

    /**
     * @hibernate.property column="page_id"
     * length="11"
     */
    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    /**
     * @hibernate.property column="ref_id"
     * length="11"
     */
    public Integer getRefId() {
        return refId;
    }

    public void setRefId(Integer refId) {
        this.refId = refId;
    }

    /**
     * @hibernate.property column="ref_type"
     * length="11"
     */
    public Integer getRefType() {
        return refType;
    }

    public void setRefType(Integer refType) {
        this.refType = refType;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("pid", getPageId())
                .append("ref", getRefId())
                .append("type", getRefType())
                .toString();
    }

    public boolean equals(Object obj) {
                if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaXRefPK castOther = (JahiaXRefPK) obj;
            return new EqualsBuilder()
                .append(this.getPageId(), castOther.getPageId())
                .append(this.getRefId(), castOther.getRefId())
                .append(this.getRefType(), castOther.getRefType())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getPageId())
                .append(getRefId())
                .append(getRefType())
                .toHashCode();
    }
}
