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
import org.jahia.hibernate.dao.JahiaApplicationDefinitionDAO;
import org.jahia.hibernate.dao.JahiaApplicationShareDAO;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.model.JahiaAppDef;
import org.jahia.hibernate.model.JahiaAppsShare;
import org.jahia.hibernate.model.JahiaAppsSharePK;
import org.jahia.hibernate.model.JahiaSite;
import org.jahia.services.shares.AppShare;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 mars 2005
 * Time: 15:37:21
 * To change this template use File | Settings | File Templates.
 */
public class JahiaApplicationShareManager {

    private JahiaApplicationShareDAO dao = null;
    private JahiaSiteDAO siteDAO = null;
    private JahiaApplicationDefinitionDAO definitionDAO = null;
    private Log log = LogFactory.getLog(getClass());

    public void setJahiaApplicationShareDAO(JahiaApplicationShareDAO dao) {
        this.dao = dao;
    }
    public void setJahiaSiteDAO(JahiaSiteDAO dao) {
        this.siteDAO = dao;
    }
    public void setJahiaApplicationDefinitionDAO(JahiaApplicationDefinitionDAO dao) {
        this.definitionDAO = dao;
    }
    public Iterator getSitesIdForApplicationID(int applicationId) {
        List list = null;
        Iterator iterator = null;
        try {
            list = dao.getSitesIdForApplicationID(new Integer(applicationId));
            iterator = list.iterator();
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Could not find sites for this application",e);
            iterator = new ArrayList(1).iterator();
        }
        return iterator;  //To change body of created methods use File | Settings | File Templates.
    }

    public void addShare(int applicationId, int siteId) {
        JahiaSite jahiaSite = siteDAO.findById(new Integer(siteId));
        JahiaAppDef appDef = definitionDAO.loadApplicationDefinition(new Integer(applicationId));
        dao.save(new JahiaAppsShare(new JahiaAppsSharePK(appDef, jahiaSite)));
    }

    public void removeShare(int applicationId, int siteId) {
        JahiaSite jahiaSite = siteDAO.findById(new Integer(siteId));
        JahiaAppDef appDef = definitionDAO.loadApplicationDefinition(new Integer(applicationId));
        try {
            final JahiaAppsShare byPk = dao.findByPk(new JahiaAppsSharePK(appDef, jahiaSite));
            if(byPk!=null) {
                dao.delete(byPk);
            }
        } catch(ObjectRetrievalFailureException e) {
        }
    }

    public void removeSharesByApplication(int id) {
        dao.deleteByApplicationId(new Integer(id));
    }

    public AppShare getShare(int applicationId, int siteId) {
        JahiaSite jahiaSite = siteDAO.findById(new Integer(siteId));
        JahiaAppDef appDef = definitionDAO.loadApplicationDefinition(new Integer(applicationId));
        AppShare appShare = null;
        try {
            final JahiaAppsShare byPk = dao.findByPk(new JahiaAppsSharePK(appDef, jahiaSite));
            if(byPk!=null) {
                appShare = new AppShare(applicationId,siteId);
            }
        } catch(ObjectRetrievalFailureException e) {
        }
        return appShare;
    }
}
