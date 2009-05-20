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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * To change this template use File | Settings | File Templates.
 *
 * @hibernate.class table="jahia_indexingjobsserver"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaIndexJobServer implements Serializable {

    private JahiaIndexJobServerPK comp_id;
    private Long date = new Long(0);

    public JahiaIndexJobServer(){
    }

    public JahiaIndexJobServer(JahiaIndexJobServerPK comp_id, Long processedDate){
        this.comp_id = comp_id;
        this.date = processedDate;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public JahiaIndexJobServerPK getComp_id() {
        return comp_id;
    }

    public void setComp_id(JahiaIndexJobServerPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="indexing_date"
     * @return
     */
    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("comp_id", this.getComp_id())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaIndexJobServer castOther = (JahiaIndexJobServer) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getComp_id())
                .toHashCode();
    }

}
