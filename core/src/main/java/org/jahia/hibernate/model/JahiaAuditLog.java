/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.sql.Blob;

/**
 * @hibernate.class table="jahia_audit_log"
 */
public class JahiaAuditLog implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private Long time;

    /**
     * nullable persistent field
     */
    private String username;

    /**
     * nullable persistent field
     */
    private Integer objecttype;

    /**
     * nullable persistent field
     */
    private Integer objectid;

    /**
     * nullable persistent field
     */
    private Integer parenttype;

    /**
     * nullable persistent field
     */
    private Integer parentid;

    /**
     * nullable persistent field
     */
    private String operation;

    /**
     * nullable persistent field
     */
    private String site;

    /**
     * nullable persistent field
     */
    private String content;

    /**
     * nullable persistent field
     */
    private JahiaAuditLog parent;

    /**
     * nullable persistent field
     */
    private String eventType;

    /**
     * nullable persistent field
     */
    private Blob eventInformation;
    /**
     * full constructor
     */
    public JahiaAuditLog(Integer idJahiaAuditLog, Long timeJahiaAuditLog, String usernameJahiaAuditLog,
                         Integer objecttypeJahiaAuditLog, Integer objectidJahiaAuditLog,
                         Integer parenttypeJahiaAuditLog, Integer parentidJahiaAuditLog, String operationJahiaAuditLog,
                         String siteJahiaAuditLog, String contentJahiaAuditLog) {
        this.id = idJahiaAuditLog;
        this.time = timeJahiaAuditLog;
        this.username = usernameJahiaAuditLog;
        this.objecttype = objecttypeJahiaAuditLog;
        this.objectid = objectidJahiaAuditLog;
        this.parenttype = parenttypeJahiaAuditLog;
        this.parentid = parentidJahiaAuditLog;
        this.operation = operationJahiaAuditLog;
        this.site = siteJahiaAuditLog;
        this.content = contentJahiaAuditLog;
    }

    /**
     * default constructor
     */
    public JahiaAuditLog() {
    }

    /**
     * minimal constructor
     */
    public JahiaAuditLog(Integer idJahiaAuditLog) {
        this.id = idJahiaAuditLog;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_audit_log"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="time_jahia_audit_log"
     */
    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    /**
     * @hibernate.property column="username_jahia_audit_log"
     * length="50"
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @hibernate.property column="objecttype_jahia_audit_log"
     * length="11"
     */
    public Integer getObjecttype() {
        return this.objecttype;
    }

    public void setObjecttype(Integer objecttype) {
        this.objecttype = objecttype;
    }

    /**
     * @hibernate.property column="objectid_jahia_audit_log"
     * length="11"
     */
    public Integer getObjectid() {
        return this.objectid;
    }

    public void setObjectid(Integer objectid) {
        this.objectid = objectid;
    }

    /**
     * @hibernate.property column="parenttype_jahia_audit_log"
     * length="11"
     */
    public Integer getParenttype() {
        return this.parenttype;
    }

    public void setParenttype(Integer parenttype) {
        this.parenttype = parenttype;
    }

    /**
     * @hibernate.property column="parentid_jahia_audit_log"
     * length="11"
     */
    public Integer getParentid() {
        return this.parentid;
    }

    public void setParentid(Integer parentid) {
        this.parentid = parentid;
    }

    /**
     * @hibernate.property column="operation_jahia_audit_log"
     * length="50"
     */
    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @hibernate.property column="site_jahia_audit_log"
     * length="50"
     */
    public String getSite() {
        return this.site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    /**
     * @hibernate.property column="content_jahia_audit_log"
     * length="250"
     */
    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @hibernate.many-to-one not-null="false"
     * @hibernate.column name="parent_id_jahia_audit_log"
     */
    public JahiaAuditLog getParent() {
        return parent;
    }

    public void setParent(JahiaAuditLog parent) {
        this.parent = parent;
    }

    /**
     * @hibernate.property not-null="false"
     */
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @hibernate.property not-null="false" type="blob"
     */
    public Blob getEventInformation() {
        return eventInformation;
    }

    public void setEventInformation(Blob eventInformation) {
        this.eventInformation = eventInformation;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAuditLog castOther = (JahiaAuditLog) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

}
