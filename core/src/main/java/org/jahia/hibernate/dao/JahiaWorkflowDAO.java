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

import org.jahia.hibernate.model.JahiaWorkflow;
import org.springframework.orm.hibernate3.HibernateTemplate;
import java.util.List;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:27:23
 * To change this template use File | Settings | File Templates.
 */
public class JahiaWorkflowDAO extends AbstractGeneratorDAO {
    public void save(JahiaWorkflow workflow) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        synchronized(this) {
            if (hibernateTemplate.get(JahiaWorkflow.class, workflow.getObjectkey()) == null) {
                hibernateTemplate.save(workflow);
                hibernateTemplate.flush();
            } else {
                hibernateTemplate.merge(workflow);
                hibernateTemplate.flush();
            }
        }
    }

    public void update(JahiaWorkflow workflow) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(workflow);
        hibernateTemplate.flush();
    }

    public JahiaWorkflow findByPK(String objectKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaWorkflow) template.load(JahiaWorkflow.class,objectKey);
    }

    public void delete(String objectKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Object o = template.get(JahiaWorkflow.class, objectKey);
        if (o != null) {
            template.delete(o);
        }
    }

    public List getLinkedObjectForMain(String mainObjectKey) {
        String hql = "select w.objectKey from JahiaWorkflow w where w.mainObjectkey=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{mainObjectKey});

    }

    public void clearMainObject(String objectKey) {
        String hql = "from JahiaWorkflow w where w.mainObjectkey=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        template.setCheckWriteOperations(false);
        List l =  template.find(hql, new Object[]{objectKey});
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            JahiaWorkflow jahiaWorkflow = (JahiaWorkflow) iterator.next();
            jahiaWorkflow.setMainObjectkey(null);
            template.update(jahiaWorkflow);
        }

    }
}
