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
