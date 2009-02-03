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
package org.jahia.hibernate.manager;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.dao.JahiaCategoryDAO;
import org.jahia.hibernate.model.JahiaCategory;
import org.jahia.services.categories.Category;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 14:24:17
 * To change this template use File | Settings | File Templates.
 */
public class JahiaCategoryManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaCategoryDAO dao = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaCategoryDAO(JahiaCategoryDAO dao) {
        this.dao = dao;
    }

// -------------------------- OTHER METHODS --------------------------

    public void createCategory(JahiaCategory categoryBean) throws JahiaException {
        dao.save(categoryBean);
    }

    public Category findCategoryByKey(String categoryKey) {
        JahiaCategory categoryByKey = null;
        try {
            categoryByKey = dao.findCategoryByKey(categoryKey);
        } catch (ObjectRetrievalFailureException e) {
        }
        if(categoryByKey != null)
            return new Category(categoryByKey);
        return null;
    }

    public List<Category> searchCategoryStartingByKey(String categoryKey) {
        final List<Category> result = new ArrayList<Category>();
        try {
            final List<JahiaCategory> jahiaCategories = dao.searchCategoryStartingByKey(categoryKey);
            if (jahiaCategories != null) {
                for (int i = 0; i < jahiaCategories.size(); i++) {
                    final JahiaCategory jahiaCat = (JahiaCategory) jahiaCategories.get(i);
                    final Category cat = getCategory(jahiaCat.getId());
                    if (cat != null) result.add(cat);
                }
            }
        } catch (ObjectRetrievalFailureException e) {
            // Ignore
        }
        return result;
    }

    public Category getCategory(int categoryID) {
        JahiaCategory categoryById = null;
        try {
            categoryById = dao.findCategoryById(new Integer(categoryID));
        } catch (ObjectRetrievalFailureException e) {
        }
        if(categoryById != null)
            return new Category(categoryById);
        return null;
    }

    public void removeCategory(JahiaCategory categoryBean) {
        dao.delete(categoryBean);
    }
}

