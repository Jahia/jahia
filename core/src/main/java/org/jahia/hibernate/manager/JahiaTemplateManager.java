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

import org.jahia.hibernate.dao.JahiaPagesDefinitionDAO;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.model.JahiaPagesDef;
import org.jahia.services.pages.JahiaPageDefinition;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 3 mars 2005
 * Time: 10:24:03
 * To change this template use File | Settings | File Templates.
 */
public class JahiaTemplateManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaPagesDefinitionDAO dao = null;
    private JahiaSiteDAO siteDAO = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaPagesDefinitionDAO(JahiaPagesDefinitionDAO dao) {
        this.dao = dao;
    }

    public void setJahiaSiteDAO(JahiaSiteDAO dao) {
        this.siteDAO = dao;
    }

// -------------------------- OTHER METHODS --------------------------

    public synchronized boolean insertPageTemplate(JahiaPageDefinition template) {
        JahiaPagesDef def = new JahiaPagesDef();
        def.setImage(template.getImage());
        def.setSite(siteDAO.findById(new Integer(template.getJahiaID())));
        def.setName(template.getName());
        def.setProperties(template.getProperties());
        def.setSourcePath(JahiaPageDefinition.DEF_SOURCE_PATH);
        def.setVisible(Boolean.valueOf(template.isAvailable()));
        def.setPageType(template.getPageType());
        dao.save(def);
        template.setID(def.getId().intValue());
        return true;
    }

    public synchronized JahiaPageDefinition loadPageTemplate(int templateID) {
        return convert(dao.loadPageTemplate(new Integer(templateID)));
    }

    public synchronized JahiaPageDefinition loadPageTemplate(String templateName, int siteId) {
        return convert(dao.loadPageTemplate(templateName, new Integer(siteId)));
    }

    private JahiaPageDefinition convert(JahiaPagesDef def) {
        JahiaPageDefinition definition = null;
        if (def != null) {
            definition = new JahiaPageDefinition(def.getId().intValue(), def
                    .getSite().getId().intValue(), def.getName(), def
                    .getSourcePath(), def.getVisible().booleanValue(), def
                    .getImage(), def.getPageType());
            definition.setProperties(def.getProperties());
        }

        return definition;
    }

    public synchronized void updatePageTemplate(JahiaPageDefinition template) {
        JahiaPagesDef def = new JahiaPagesDef();
        if (template.getID() > 0) {
            def.setId(new Integer(template.getID()));
        }
        def.setImage(template.getImage());
        def.setSite(siteDAO.findById(new Integer(template.getJahiaID())));
        def.setName(template.getName());
        def.setProperties(template.getProperties());
        def.setSourcePath(JahiaPageDefinition.DEF_SOURCE_PATH);
        def.setVisible(Boolean.valueOf(template.isAvailable()));
        def.setPageType(template.getPageType());
            dao.save(def);
            template.setID(def.getId().intValue());

    }

    public synchronized void deletePageTemplate(int defID) {
        dao.delete(new Integer(defID));
    }

    public synchronized int getPageTemplateIDMatchingSourcePath(int siteID, String path) {
        final Integer pageTemplateIDMatchingSourcePath = dao.getPageTemplateIDMatchingSourcePath(new Integer(siteID),
                                                                                                 path);
        int ret = -1;
        if (pageTemplateIDMatchingSourcePath != null) {
            ret = pageTemplateIDMatchingSourcePath.intValue();
        }
        return ret;
    }

    public int getNbPageTemplates() {
        Integer nbPage = dao.getNbPageTemplates();
        int ret = -1;
        if (nbPage != null) {
            ret = nbPage.intValue();
        }
        return ret;
    }

    public int getNbPageTemplates(int siteID) {
        Integer nbPage = dao.getNbPageTemplates(new Integer(siteID));
        int ret = -1;
        if (nbPage != null) {
            ret = nbPage.intValue();
        }
        return ret;
    }

    public List<Integer> getAllPageTemplateIDs() {
        return dao.getAllPageTemplateIDs();
    }

    public List<Integer> getPageTemplateIDs(int siteId, boolean availableOnly) {
        return dao.getPageTemplateIDs(siteId, availableOnly);
    }

    public List<Integer> getAllAclId(int siteID) {
        return dao.getAllAclId(new Integer(siteID));
    }
}

