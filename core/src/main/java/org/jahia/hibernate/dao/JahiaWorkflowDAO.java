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
