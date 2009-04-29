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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.content.ObjectKey;
import org.jahia.hibernate.dao.JahiaObjectDAO;
import org.jahia.hibernate.dao.JahiaRetentionRuleDAO;
import org.jahia.hibernate.model.JahiaRetentionRule;
import org.jahia.services.timebasedpublishing.BaseRetentionRule;
import org.jahia.services.timebasedpublishing.RetentionRule;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:42:48
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRetentionRuleManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaRetentionRuleDAO dao = null;
    private JahiaObjectDAO jahiaObjectDAO = null;

    private JahiaObjectManager jahiaObjectManager = null;

    private Log log = LogFactory.getLog(JahiaRetentionRuleManager.class);

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaRetentionRuleDAO(JahiaRetentionRuleDAO dao) {
        this.dao = dao;
    }

    public JahiaObjectDAO getJahiaObjectDAO() {
        return jahiaObjectDAO;
    }

    public void setJahiaObjectDAO(JahiaObjectDAO jahiaObjectDAO) {
        this.jahiaObjectDAO = jahiaObjectDAO;
    }

    public JahiaObjectManager getJahiaObjectManager() {
        return jahiaObjectManager;
    }

    public void setJahiaObjectManager(JahiaObjectManager jahiaObjectManager) {
        this.jahiaObjectManager = jahiaObjectManager;
    }

    public List getJahiaRetentionRules() {
        List list = dao.getRetentionRules();
        return fillList(list);
    }

// -------------------------- OTHER METHODS --------------------------

    private List fillList(List list) {
        FastArrayList retList = new FastArrayList(list.size());
        for (Iterator it = list.iterator(); it.hasNext();) {
            JahiaRetentionRule jahiaRetentionRule = (JahiaRetentionRule) it.next();
            try {
                retList.add(jahiaRetentionRule.getRetentionRule());
            } catch (Exception e) {
                log.warn("Could not create retention rule ",e);
            }            
        }
        retList.setFast(true);
        return retList;
    }

    public RetentionRule getRetentionRuleById(int id) {
        if (id < 0) {
            return null;
        }
        JahiaRetentionRule jahiaRetentionRule = dao.findByPK(new Integer(id));
        if ( jahiaRetentionRule != null ){
            try {
                return jahiaRetentionRule.getRetentionRule();
            } catch (Exception e) {
                log.warn("Could not create retention rule ",e);
            }
        }
        return null;
    }

    public RetentionRule getRetentionRuleByObjectKey(ObjectKey objectKey) {
        JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(objectKey);
        if ( jahiaObjectDelegate == null ){
            return null;
        }
        return jahiaObjectDelegate.getRule();
    }

    public JahiaRetentionRule getDetachedJahiaRetentionRuleById(int id) {
        if (id < 0) {
            return null;
        }
        JahiaRetentionRule jahiaRetentionRule = dao.findByPK(new Integer(id));
        if ( jahiaRetentionRule != null ){
            try {
                jahiaRetentionRule.getRetentionRuleDef();
                return jahiaRetentionRule;
            } catch (Exception e) {
                log.warn("Could not create retention rule ",e);
            }
        }
        return null;
    }

    public void delete(int id) {
        dao.delete(new Integer(id));
    }

    public void save(BaseRetentionRule retentionRule) {
        try {
            JahiaRetentionRule jahiaRetentionRule = retentionRule.getJahiaRetentionRule();
            dao.save(jahiaRetentionRule);
            retentionRule.setId(jahiaRetentionRule.getId());
        } catch (Exception e) {
            log.debug("Error saving retention rule ",e);
        }
    }

}

