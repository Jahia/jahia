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

