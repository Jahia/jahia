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
import java.io.Serializable;
import java.util.Map;

/**
 * @hibernate.class table="jahia_pages_def"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaPagesDef implements Serializable {

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private JahiaSite site;

    /**
     * nullable persistent field
     */
    private String name;

    /**
     * nullable persistent field
     */
    private String sourcePath;

    /**
     * nullable persistent field
     */
    private Boolean visible;

    /**
     * nullable persistent field
     */
    private Integer browsable;

    /**
     * nullable persistent field
     */
    private String warningMessage;

    /**
     * nullable persistent field
     */
    private String image;

    private String pageType;

    /**
     * persistent field
     */
    private Map properties;

    /**
     * full constructor
     */
    public JahiaPagesDef(Integer idJahiaPagesDef, JahiaSite jahiaidJahiaPagesDef, String nameJahiaPagesDef,
                         String sourcepathJahiaPagesDef, Boolean visibleJahiaPagesDef, Integer browsableJahiaPagesDef,
                         String warningMsgJahiaPagesDef, String imgJahiaPagesDef, Map jahiaPagesDefProps) {
        this.id = idJahiaPagesDef;
        this.site = jahiaidJahiaPagesDef;
        this.name = nameJahiaPagesDef;
        this.sourcePath = sourcepathJahiaPagesDef;
        this.visible = visibleJahiaPagesDef;
        this.browsable = browsableJahiaPagesDef;
        this.warningMessage = warningMsgJahiaPagesDef;
        this.image = imgJahiaPagesDef;
        this.properties = jahiaPagesDefProps;
    }

    /**
     * default constructor
     */
    public JahiaPagesDef() {
    }

    /**
     * minimal constructor
     */
    public JahiaPagesDef(Integer idJahiaPagesDef, Map jahiaPagesDefProps) {
        this.id = idJahiaPagesDef;
        this.properties = jahiaPagesDefProps;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_pages_def"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.many-to-one cascade="none"
     * @hibernate.column name="jahiaid_jahia_pages_def"
     */
    public JahiaSite getSite() {
        return this.site;
    }

    public void setSite(JahiaSite site) {
        this.site = site;
    }

    /**
     * @hibernate.property column="name_jahia_pages_def"
     * length="250"
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.property column="sourcepath_jahia_pages_def"
     * length="250"
     */
    public String getSourcePath() {
        return this.sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * @hibernate.property column="visible_jahia_pages_def"
     * length="11"
     */
    public Boolean getVisible() {
        return this.visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    /**
     * @hibernate.property column="browsable_jahia_pages_def"
     * length="11"
     */
    public Integer getBrowsable() {
        return this.browsable;
    }

    public void setBrowsable(Integer browsable) {
        this.browsable = browsable;
    }

    /**
     * @hibernate.property column="warning_msg_jahia_pages_def"
     * length="250"
     */
    public String getWarningMessage() {
        return this.warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    /**
     * @hibernate.property column="img_jahia_pages_def"
     * length="150"
     */
    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @hibernate.property column="pagetype_jahia_pages_def"
     * length="150"
     */
    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    /**
     * @hibernate.map lazy="false" inverse="true" cascade="delete" table="jahia_pages_def_prop"
     * @hibernate.collection-key column="id_jahia_pages_def_prop" type="int"
     * @hibernate.collection-index column="name_pages_def_prop" type="string"
     * @hibernate.collection-element column="value_pages_def_prop" type="string"
     * @hibernate.collection-cache usage="nonstrict-read-write"
     * @return
     */
    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }
    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("id="+getId())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaPagesDef castOther = (JahiaPagesDef) obj;
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
