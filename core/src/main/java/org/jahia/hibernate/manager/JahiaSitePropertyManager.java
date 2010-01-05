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
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.jahia.hibernate.dao.JahiaSitePropertyDAO;
import org.jahia.hibernate.model.JahiaSiteProp;
import org.jahia.hibernate.model.JahiaSitePropPK;
import org.jahia.services.sites.JahiaSite;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 23 déc. 2004
 * Time: 14:34:33
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSitePropertyManager {

    private JahiaSitePropertyDAO dao = null;

    public void setJahiaSitePropertyDAO(JahiaSitePropertyDAO dao) {
        this.dao = dao;
    }

    public void save(JahiaSite site, Properties updateProps) {
        Enumeration<?> enumeration = updateProps.propertyNames();
        while (enumeration.hasMoreElements()) {
            String s = (String)enumeration.nextElement();
            String v = updateProps.getProperty(s);
            saveProperty(site, s, v);
        }
    }
    
    public void save(JahiaSite id, String name, String value) {
        saveProperty(id, name, value);
    }

    private void saveProperty(JahiaSite id, String name, String value) {
        JahiaSiteProp siteProp;
        try {
            siteProp = dao.getSitePropByKey(id.getID(), name);
            if (!value.equals(siteProp.getValue())) {
                siteProp.setValue(value);
                dao.update(siteProp);
            }
        } catch (ObjectRetrievalFailureException e) {
            siteProp = new JahiaSiteProp(new JahiaSitePropPK(new Integer(id.getID()), name),
                                         value);
            dao.save(siteProp);
        }
    }

    public void save(JahiaSite site) {
        Properties properties = site.getSettings();
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String s = (String) enumeration.nextElement();
            String v = properties.getProperty(s);
            saveProperty(site, s, v);
        }
    }

    public void remove(JahiaSite id) {
        Properties properties = id.getSettings();
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String s = (String) enumeration.nextElement();
            try {
                dao.remove(id.getID(), s);
            } catch (ObjectRetrievalFailureException e) {
            }
        }
    }

    public void remove(JahiaSite id, List<String> propertiesToBeRemoved) {
        for (String s : propertiesToBeRemoved) {
            try {
                dao.remove(id.getID(), s);
            } catch (ObjectRetrievalFailureException e) {
            }
        }
    }

    public List<JahiaSiteProp> getProperties(Integer id) {
        return dao.getSitePropById(id);
    }

    public String getProperty(JahiaSite id, String name) {
        try {
            if(id!=null) {
                final JahiaSiteProp sitePropByKey = dao.getSitePropByKey(id.getID(),name);
                return sitePropByKey.getValue();
            }
            return null;
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public void remove(JahiaSite id, String mixLanguagesActive) {
        try {
            dao.remove(id.getID(), mixLanguagesActive);
        } catch (ObjectRetrievalFailureException e) {
        }
    }
}
