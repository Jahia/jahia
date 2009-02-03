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
