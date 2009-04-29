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
package org.jahia.hibernate.model.indexingjob;

import org.jahia.hibernate.model.CachedPK;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaIndexJobServerPK extends CachedPK implements Serializable {

    /**
     * identifier field
     */
    private String indexingJobId;

    /**
     * identifier field
     */
    private String serverId;

    /**
     * full constructor
     */
    public JahiaIndexJobServerPK(String indexingJobId, String serverId) {
        this.indexingJobId = indexingJobId;
        this.serverId = serverId;
   }

    /**
     * default constructor
     */
    public JahiaIndexJobServerPK() {
    }

    /**
     * @hibernate.property column="serverid"
     * length="200"
     */
    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String serverId) {
        updated();
        this.serverId = serverId;
    }

    /**
     * @hibernate.property column="indexingjobid"
     * length="50"
     */
    public String getIndexingJobId() {
        return this.indexingJobId;
    }

    public void setIndexingJobId(String indexingJobId) {
        updated();
        this.indexingJobId = indexingJobId;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("indexingJobId", getIndexingJobId())
                .append("serverId", getServerId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaIndexJobServerPK castOther = (JahiaIndexJobServerPK) obj;
            return new EqualsBuilder()
                .append(this.getIndexingJobId(), castOther.getIndexingJobId())
                .append(this.getServerId(), castOther.getServerId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIndexingJobId())
                .append(getServerId())
                .toHashCode();
    }

}
