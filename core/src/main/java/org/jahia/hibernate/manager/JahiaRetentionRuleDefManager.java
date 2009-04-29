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
import org.jahia.hibernate.dao.JahiaRetentionRuleDefDAO;
import org.jahia.hibernate.model.JahiaRetentionRuleDef;
import org.jahia.services.timebasedpublishing.BaseRetentionRuleDef;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:42:48
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRetentionRuleDefManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaRetentionRuleDefDAO dao = null;
    private Log log = LogFactory.getLog(JahiaRetentionRuleDefManager.class);

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaRetentionRuleDefDAO(JahiaRetentionRuleDefDAO dao) {
        this.dao = dao;
    }

    public List getJahiaRetentionRuleDefs() {
        List list = dao.getRetentionRuleDefs();
        return fillList(list);
    }


// -------------------------- OTHER METHODS --------------------------

    private List fillList(List list) {
        FastArrayList retList = new FastArrayList(list.size());
        for (Iterator it = list.iterator(); it.hasNext();) {
            JahiaRetentionRuleDef jahiaRetentionRuleDef = (JahiaRetentionRuleDef) it.next();
            try {
                retList.add(jahiaRetentionRuleDef.toRetentionRuleDef());
            } catch (Exception e) {
                log.warn("Could not create retention rule ",e);
            }            
        }
        retList.setFast(true);
        return retList;
    }

    public BaseRetentionRuleDef getRetentionRuleDefById(int id) {
        if (id < 0) {
            return null;
        }
        JahiaRetentionRuleDef def = dao.findByPK(new Integer(id));
        if ( def != null ){
            return (BaseRetentionRuleDef)def.toRetentionRuleDef();
        }
        return null;
    }

    public BaseRetentionRuleDef getRetentionRuleDefByName(String name) {
        JahiaRetentionRuleDef def = dao.findName(name);
        if ( def != null ){
            return (BaseRetentionRuleDef)def.toRetentionRuleDef();
        }
        return null;
    }

    public void delete(int id) {
        dao.delete(new Integer(id));
    }

    public void save(BaseRetentionRuleDef retentionRuleDef) {
        JahiaRetentionRuleDef jahiaRetentionRuleDef = new JahiaRetentionRuleDef();
        jahiaRetentionRuleDef.setId(retentionRuleDef.getId());
        jahiaRetentionRuleDef.setName(retentionRuleDef.getName());
        jahiaRetentionRuleDef.setRuleClassName(retentionRuleDef.getRuleClassName());
        jahiaRetentionRuleDef.setRuleHelperClassName(retentionRuleDef.getRuleHelperClassName());
        jahiaRetentionRuleDef.setTitle(retentionRuleDef.getTitle());
        jahiaRetentionRuleDef.setDateFormat(retentionRuleDef.getDateFormat());
        dao.save(jahiaRetentionRuleDef);
        retentionRuleDef.setId(jahiaRetentionRuleDef.getId());
    }

}

