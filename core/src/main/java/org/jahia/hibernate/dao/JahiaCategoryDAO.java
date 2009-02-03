/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.jahia.hibernate.model.JahiaCategory;
import org.jahia.utils.comparator.NumericStringComparator;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 14:32:35
 * To change this template use File | Settings | File Templates.
 */
public class JahiaCategoryDAO extends AbstractGeneratorDAO {

    public JahiaCategory findCategoryByKey(String categoryKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaCategory category = null;
        if (categoryKey != null) {
            List<JahiaCategory> list = template.find("from JahiaCategory c where c.key=?", categoryKey);
            if(list.size()>0) {
                category = (JahiaCategory) list.get(0);
            } else {
                throw new ObjectRetrievalFailureException(JahiaCategory.class,categoryKey);
            }
        }
        return category;
    }

    public List<JahiaCategory> searchCategoryStartingByKey(final String categoryKey) {
        final List<JahiaCategory> values = getSession().createCriteria(JahiaCategory.class).
                add(Expression.ilike("key", categoryKey, MatchMode.START)).list();
        final TreeSet<JahiaCategory> tmp = new TreeSet<JahiaCategory>(new NumericStringComparator());
        tmp.addAll(values);
        final List<JahiaCategory> result = new ArrayList<JahiaCategory>(values.size());
        final Iterator<JahiaCategory> tmpIterator = tmp.iterator();
        while (tmpIterator.hasNext()) {
            result.add(tmpIterator.next());
        }
        return result;
    }

    public JahiaCategory findCategoryById(Integer categoryId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaCategory) template.load(JahiaCategory.class,categoryId);
    }

    public void save(JahiaCategory jahiaCategory) {
        if (jahiaCategory.getID() == null) {
            jahiaCategory.setID(getNextInteger(jahiaCategory));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(jahiaCategory);
    }

    public void delete(JahiaCategory categoryById) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(categoryById);
    }
}
