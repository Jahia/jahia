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
