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

import org.jahia.hibernate.model.JahiaCategoryProp;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 15:04:47
 * To change this template use File | Settings | File Templates.
 */
public class JahiaCategoryPropertiesDAO extends AbstractGeneratorDAO {
// -------------------------- OTHER METHODS --------------------------

    public void delete(Integer categoryId) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaCategoryProp c where c.comp_id.id=?",
                                      categoryId));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public List<Integer> findCategoryIDsByPropNameAndValue(String propName, String propValue) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Integer> list = new ArrayList<Integer>(1);
        if (propName != null && propValue != null) {
            list = template.find("select distinct c.comp_id.id from JahiaCategoryProp c " +
                                 "where c.comp_id.name like ? and c.value like ?",
                                 new Object[]{propName, propValue});
        }
        return list;
    }

    public List<JahiaCategoryProp> getJahiaCategoryProperties(Integer categoryId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<JahiaCategoryProp> list = new ArrayList<JahiaCategoryProp>(1);
        if (categoryId != null) {
            list = template.find("from JahiaCategoryProp c where c.comp_id.id=?",
                                 categoryId);
        }
        return list;
    }

    public void save(JahiaCategoryProp jahiaCategoryProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(jahiaCategoryProp);
    }
}

