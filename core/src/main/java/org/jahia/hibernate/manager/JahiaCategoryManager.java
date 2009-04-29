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

