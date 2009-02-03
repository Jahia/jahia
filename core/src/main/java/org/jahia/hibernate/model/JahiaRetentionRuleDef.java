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

import org.jahia.services.timebasedpublishing.BaseRetentionRuleDef;
import org.jahia.services.timebasedpublishing.RetentionRuleDef;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 13:01:41
 * To change this template use File | Settings | File Templates.
 *
 * @hibernate.class table="jahia_retruledef"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaRetentionRuleDef {

    private Integer id;
    private String name;
    private String title;
    private String ruleClassName;
    private String ruleHelperClassName;
    private String dateFormat;

    public JahiaRetentionRuleDef(){
    }

    /**
     *
     * @param id
     * @param title
     * @param ruleClassName
     */
    public JahiaRetentionRuleDef( Integer id,
                                 String title,
                                 String ruleClassName ){
        this.id = id;
        this.title = title;
        this.ruleClassName = ruleClassName;
    }

    /**
     * @hibernate.id generator-class="assigned" column="id_jahia_retruledef"
     *
     * @return
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="name_retruledef" unique="true"
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.property column="title_retruledef"
     * @return
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @hibernate.property column="ruleclass_retruledef"
     * @return
     */
    public String getRuleClassName() {
        return ruleClassName;
    }

    public void setRuleClassName(String ruleClassName) {
        this.ruleClassName = ruleClassName;
    }

    /**
     * @hibernate.property column="rulehelperclass_retruledef"
     * @return
     */
    public String getRuleHelperClassName() {
        return ruleHelperClassName;
    }

    public void setRuleHelperClassName(String ruleHelperClassName) {
        this.ruleHelperClassName = ruleHelperClassName;
    }

    /**
     * @hibernate.property column="dateformat_retruledef"
     * @return
     */
    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public RetentionRuleDef toRetentionRuleDef(){
        BaseRetentionRuleDef retentionRuleDef = new BaseRetentionRuleDef();
        retentionRuleDef.setId(this.getId());
        retentionRuleDef.setName(this.getName());
        retentionRuleDef.setRuleClassName(this.getRuleClassName());
        retentionRuleDef.setRuleHelperClassName(this.getRuleHelperClassName());
        retentionRuleDef.setTitle(this.getTitle());
        retentionRuleDef.setDateFormat(this.getDateFormat());
        return retentionRuleDef;
    }

}
