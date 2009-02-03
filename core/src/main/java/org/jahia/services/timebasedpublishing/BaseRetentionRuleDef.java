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

 package org.jahia.services.timebasedpublishing;

import org.jahia.hibernate.manager.JahiaRetentionRuleManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaRetentionRuleDef;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 13:01:41
 * To change this template use File | Settings | File Templates.
 *
 */
public class BaseRetentionRuleDef implements RetentionRuleDef, Serializable {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (BaseRetentionRuleDef.class);

    protected Integer id;
    private String name;
    private String title;
    private String ruleClassName;
    private String ruleHelperClassName;
    private String dateFormat;

    /**
     *
     * @param id
     * @param title
     * @param ruleClassName
     */
    public BaseRetentionRuleDef( Integer id,
                                 String title,
                                 String ruleClassName ){
        this.id = id;
        this.title = title;
        this.ruleClassName = ruleClassName;
    }

    public BaseRetentionRuleDef() {
        
    }

    /**
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     */
    public String getRuleClassName() {
        return ruleClassName;
    }

    public void setRuleClassName(String ruleClassName) {
        this.ruleClassName = ruleClassName;
    }

    public String getRuleHelperClassName() {
        return ruleHelperClassName;
    }

    public void setRuleHelperClassName(String ruleHelperClassName) {
        this.ruleHelperClassName = ruleHelperClassName;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public RetentionRule getRule(Integer id){
        return getRuleManager().getRetentionRuleById(id.intValue());
    }

    public RetentionRule createRule(){
        RetentionRule rule = null;
        try {
            Class c = Class.forName(this.getRuleClassName());
            rule = (RetentionRule)c.newInstance();
            rule.setRetentionRuleDef(this);
        } catch ( IllegalAccessException ile ){
            logger.debug("Wrong rule class",ile);
        } catch ( InstantiationException inse ){
            logger.debug("Wrong rule class",inse);
        } catch ( ClassNotFoundException cnfe ){
            logger.debug("Wrong rule class",cnfe);
        }
        return rule;
    }

    public RetentionRule saveRule(RetentionRule rule)
    throws Exception {
        BaseRetentionRule baseRetRule = (BaseRetentionRule)rule;

        getRuleManager().save(baseRetRule);
        rule = baseRetRule.getJahiaRetentionRule().getRetentionRule();
        return rule;
    }

    public void deleteRule(RetentionRule rule){
        getRuleManager().delete(rule.getId().intValue());
    }

    public RetentionRuleDefHelper getHelper(){
        if ( this.getRuleHelperClassName() != null ){
            try {
                Class c = Class.forName(this.getRuleHelperClassName());
                return (RetentionRuleDefHelper) c.newInstance();
            } catch ( ClassNotFoundException cnfe ){
                logger.debug("Class not found Exception", cnfe);
            } catch ( IllegalAccessException iae ){
                logger.debug("Illegal Access Exception", iae);
            } catch ( InstantiationException ins ){
                logger.debug("Instanciation Access Exception", ins);
            }
        }
        return null;
    }

    public String getBundleKey() {
        return "org.jahia.engines.timebasedpublishing.baseretentionrule.label";
    }

    public JahiaRetentionRuleDef getJahiaRetentionRuleDef(){
        JahiaRetentionRuleDef ruleDef = new JahiaRetentionRuleDef(this.getId(),this.getTitle(),this.getRuleClassName());
        ruleDef.setName(this.getName());
        ruleDef.setDateFormat(this.getDateFormat());
        ruleDef.setRuleHelperClassName(this.getRuleHelperClassName());
        return ruleDef;
    }

    protected JahiaRetentionRuleManager getRuleManager(){
        try {
            return  (JahiaRetentionRuleManager)
                    SpringContextSingleton.getInstance().getContext().getBean(JahiaRetentionRuleManager.class.getName());
        } catch ( Exception t ){
            logger.debug(t);
        }
        return null;
    }

}
