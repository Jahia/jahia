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
