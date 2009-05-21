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
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaRetentionRuleDef;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:54:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRetentionRuleDefDAO extends AbstractGeneratorDAO {

    public List getRetentionRuleDefs() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaRetentionRuleDef");
    }

    public JahiaRetentionRuleDef findByPK(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaRetentionRuleDef) template.load(JahiaRetentionRuleDef.class, id);
    }

    public JahiaRetentionRuleDef findName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List retList = template.find("from JahiaRetentionRuleDef r where r.name=?",name);
        if ( !retList.isEmpty() ){
            return (JahiaRetentionRuleDef)retList.get(0);
        }
        return null;
    }

    public void delete(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaRetentionRuleDef h where h.id=?", id));
    }

    public void save(JahiaRetentionRuleDef retentionRuleDef) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (retentionRuleDef.getId() == null) {
            retentionRuleDef.setId(getNextInteger(retentionRuleDef));
        }
        template.merge(retentionRuleDef);
    }
}
