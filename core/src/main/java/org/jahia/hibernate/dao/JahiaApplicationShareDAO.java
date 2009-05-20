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

import org.jahia.hibernate.model.JahiaAppsShare;
import org.jahia.hibernate.model.JahiaAppsSharePK;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 mars 2005
 * Time: 15:39:17
 * To change this template use File | Settings | File Templates.
 */
public class JahiaApplicationShareDAO extends AbstractGeneratorDAO {
    public List getSitesIdForApplicationID(Integer applicationId) {
        List retList = null;
        if(applicationId!=null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            retList = template.find("select distinct a.comp_id.site.id from JahiaAppsShare a " +
                                                  "where a.comp_id.definition.id=?",new Object[]{applicationId});
            if(!(retList.size()>0)) {
                throw new ObjectRetrievalFailureException("Couldnot find site id for application id",applicationId);
            }
        }
        return retList;
    }

    public JahiaAppsShare findByPk(JahiaAppsSharePK jahiaAppsSharePK) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaAppsShare) template.load(JahiaAppsShare.class,jahiaAppsSharePK);
    }

    public void save(JahiaAppsShare jahiaAppsShare) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(jahiaAppsShare);
    }

    public void delete(JahiaAppsShare share) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(share);
    }

    public void deleteByApplicationId(Integer applicationId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaAppsShare a where a.comp_id.definition.id=?",
                                      applicationId));
    }
}
