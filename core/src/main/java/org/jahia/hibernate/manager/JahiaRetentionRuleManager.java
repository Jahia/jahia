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

