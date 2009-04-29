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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.hibernate.dao.JahiaResourceDAO;
import org.jahia.hibernate.model.JahiaResource;
import org.jahia.hibernate.model.JahiaResourcePK;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 avr. 2005
 * Time: 16:40:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaResourceManager {
    private JahiaResourceDAO dao = null;
    private Log log = LogFactory.getLog(JahiaResourceManager.class);
    public void setJahiaResourceDAO(JahiaResourceDAO dao) {
        this.dao = dao;
    }

    public JahiaResource getResource(String name, String languageCode) {
        JahiaResource jahiaResource = null;
        try {
            jahiaResource = dao.findByPK(new JahiaResourcePK(name, languageCode));
        } catch (Exception e) {
            log.debug("Could not find resource "+name+" in language code "+languageCode);
        }
        return jahiaResource;
    }

    public List<JahiaResource> getResources(String name) {
        try {
            return dao.findByName(name);
        } catch (Exception e) {
            log.debug("Could not find resource "+name);
        }
        return null;
    }

    public List<JahiaResource> searchResourcesByStartingNameInLanguage(final String name, final String language) {
        try {
            return dao.searchByStartingNameInLanguage(name,language);
        } catch (Exception e) {
            log.debug("Could not find resources starting by " +name+ " in language "+language);
        }
        return null;
    }

    public List<JahiaResource> searchResourcesByContainingStringInLanguage(final String name, final String language) {
        try {
            return dao.searchResourcesByContainingStringInLanguage(name, language);
        } catch (Exception e) {
            log.debug("Could not find resources containing " + name + " in language " + language);
        }
        return null;
    }

    public void deleteAllResourcesForName(String name) {
        dao.deleteAllResourcesForName(name);
    }

    public void updateResource(JahiaResource resource) {
        dao.update(resource);
    }

    public void saveResource(JahiaResource resource) {
        dao.save(resource);
    }

    public void removeResource(JahiaResource resource) {
        dao.delete(resource);
    }
}
