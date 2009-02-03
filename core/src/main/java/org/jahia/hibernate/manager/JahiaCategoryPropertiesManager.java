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
