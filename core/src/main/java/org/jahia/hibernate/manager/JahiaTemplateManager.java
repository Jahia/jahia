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

    public List getAllPageTemplateIDs() {
        return dao.getAllPageTemplateIDs();
    }

    public List getPageTemplateIDs(int siteId, boolean availableOnly) {
        return dao.getPageTemplateIDs(siteId, availableOnly);
    }

    public List getAllAclId(int siteID) {
        return dao.getAllAclId(new Integer(siteID));
    }
}

