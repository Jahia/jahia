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
