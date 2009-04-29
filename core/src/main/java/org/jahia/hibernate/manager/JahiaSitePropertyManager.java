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
    private JahiaSiteManager jahiaSiteManager = null;

    public void setJahiaSitePropertyDAO(JahiaSitePropertyDAO dao) {
        this.dao = dao;
    }

    public void setJahiaSiteManager(JahiaSiteManager siteManager) {
        this.jahiaSiteManager = siteManager;
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
            siteProp = dao.getSitePropByKey(jahiaSiteManager.getModelSiteById(id.getID()), name);
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
        final org.jahia.hibernate.model.JahiaSite modelSiteById = jahiaSiteManager.getModelSiteById(id.getID());
        Properties properties = id.getSettings();
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String s = (String) enumeration.nextElement();
            try {
                dao.remove(modelSiteById, s);
            } catch (ObjectRetrievalFailureException e) {
            }
        }
    }

    public void remove(JahiaSite id, List<String> propertiesToBeRemoved) {
        final org.jahia.hibernate.model.JahiaSite modelSiteById = jahiaSiteManager.getModelSiteById(id.getID());
        for (String s : propertiesToBeRemoved) {
            try {
                dao.remove(modelSiteById, s);
            } catch (ObjectRetrievalFailureException e) {
            }
        }
    }

    public List<JahiaSiteProp> getProperties(Integer id) {
        return dao.getSitePropById(id);
    }

    public String getProperty(JahiaSite id, String name) {
        try {
            org.jahia.hibernate.model.JahiaSite modelSiteById = jahiaSiteManager.getModelSiteById(id.getID());
            if(modelSiteById!=null) {
                final JahiaSiteProp sitePropByKey = dao.getSitePropByKey(modelSiteById,
                                                                         name);
                return sitePropByKey.getValue();
            }
            return null;
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public void remove(JahiaSite id, String mixLanguagesActive) {
        final org.jahia.hibernate.model.JahiaSite modelSiteById = jahiaSiteManager.getModelSiteById(id.getID());
        try {
            dao.remove(modelSiteById, mixLanguagesActive);
        } catch (ObjectRetrievalFailureException e) {
        }
    }
}
