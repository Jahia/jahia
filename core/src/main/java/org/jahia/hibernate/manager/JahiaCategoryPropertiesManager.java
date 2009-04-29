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

import org.jahia.hibernate.dao.JahiaCategoryPropertiesDAO;
import org.jahia.hibernate.model.JahiaCategoryProp;
import org.jahia.hibernate.model.JahiaCategoryPropPK;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 14:58:59
 * To change this template use File | Settings | File Templates.
 */
public class JahiaCategoryPropertiesManager {

    private JahiaCategoryPropertiesDAO dao = null;

    public void setJahiaCategoryPropertiesDAO(JahiaCategoryPropertiesDAO dao) {
        this.dao = dao;
    }

    public List<Integer> findCategoryIDsByPropNameAndValue(String propName, String propValue) {
        return dao.findCategoryIDsByPropNameAndValue(propName, propValue);
    }

    public void removeProperties(int categoryId) {
        dao.delete(new Integer(categoryId));
    }

    public Properties getProperties(int categoryId) {
        Properties properties = new Properties();
        List<JahiaCategoryProp> list = dao.getJahiaCategoryProperties(new Integer(categoryId));
        for (int i = 0; i < list.size(); i++) {
            JahiaCategoryProp jahiaCategoryProp = list.get(i);
            properties.setProperty(jahiaCategoryProp.getComp_id().getName(),jahiaCategoryProp.getValue());
        }
        return properties;
    }

    public void setProperties(int categoryId, Properties properties) {
        removeProperties(categoryId);
        Integer catId = new Integer(categoryId);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            dao.save(new JahiaCategoryProp(new JahiaCategoryPropPK(catId, (String) entry.getKey()),(String) entry.getValue()));
        }
    }
}
