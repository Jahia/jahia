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
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.hibernate.dao.JahiaAclDAO;
import org.jahia.hibernate.dao.JahiaApplicationDefinitionDAO;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAppDef;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 mars 2005
 * Time: 16:43:40
 * To change this template use File | Settings | File Templates.
 */
public class JahiaApplicationManager {
    private JahiaAclDAO aclDAO = null;
    private JahiaApplicationDefinitionDAO dao = null;
    private Log log = LogFactory.getLog(getClass());

    public void setJahiaAclDAO(JahiaAclDAO dao) {
        this.aclDAO = dao;
    }

    public void setJahiaApplicationDefinitionDAO(JahiaApplicationDefinitionDAO dao) {
        this.dao = dao;
    }

    public List<Integer> getWebSites() {
        return dao.getSiteIds();
    }

    public ApplicationBean getApplicationDefinition(int appID) {
        ApplicationBean applicationBean = null;
        try {
            JahiaAppDef appDef = dao.loadApplicationDefinition(new Integer(appID));
            if(appDef!=null)
            applicationBean = convertJahiaAppDefToApplicationBean(appDef);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Could not found application for id " + appID, e);
        }
        return applicationBean;
    }

    public ApplicationBean getApplicationDefinition(String context) {
        ApplicationBean applicationBean = null;
        try {
            JahiaAppDef appDef = dao.loadApplicationDefinition(context);
            applicationBean = convertJahiaAppDefToApplicationBean(appDef);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Could not found application for context " + context, e);
        }
        return applicationBean;
    }

    private ApplicationBean convertJahiaAppDefToApplicationBean(JahiaAppDef appDef) {
        ApplicationBean applicationBean = null;
        if(appDef!=null) {
        applicationBean = new ApplicationBean(appDef.getId().toString(),
                                              appDef.getName(), appDef.getContext(),
                                              appDef.getVisible() ==1,
                                              appDef.getDescription(),
                                              appDef.getType());
        }
        return applicationBean;
    }

    /**
     * Add application bean
     * @param app
     */
    public void addApplication(ApplicationBean app) {
        JahiaAppDef appDef = new JahiaAppDef();
        appDef.setContext(app.getContext());
        appDef.setName(app.getName());
        appDef.setDescription(app.getDescription());
        appDef.setType(app.getType());
        appDef.setVisible(app.isVisible()?1:0);
        dao.save(appDef);
        app.setID(appDef.getId().toString());
    }

    /**
     * Update application bean
     * @param app
     */
    public void updateApplication(ApplicationBean app) {
        try {
            JahiaAppDef appDef = dao.loadApplicationDefinition(new Integer(app.getID()));
            appDef.setContext(app.getContext());
            appDef.setDescription(app.getDescription());
            appDef.setName(app.getName());
            appDef.setType(app.getType());
            appDef.setVisible(app.isVisible()?1:0);
            dao.update(appDef);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Try to update a non existing object " + app, e);
        }
    }

    /**
     * Remove application
     * @param appID
     */
    public void removeApplication(int appID) {
        dao.delete(dao.loadApplicationDefinition(new Integer(appID)));
    }

    /**
     * Get Application bean depending on visibility
     * @param visible
     * @return
     */
    public List<ApplicationBean> getApplicationsList(boolean visible) {
        List<ApplicationBean> applicationBeanList = Collections.emptyList();
        List<JahiaAppDef> jahiaAppDefList = visible ? dao.getVisibleApplications() : dao.getAllApplications();

        if (!jahiaAppDefList.isEmpty()) {
            List<ApplicationBean> tempList = new FastArrayList(jahiaAppDefList.size());
            for (Iterator<JahiaAppDef> it = jahiaAppDefList.iterator(); it.hasNext();) {
                JahiaAppDef appDef = it.next();
                tempList.add(convertJahiaAppDefToApplicationBean(appDef));
            }
            ((FastArrayList)tempList).setFast(true);
            applicationBeanList = tempList;
        }
        return applicationBeanList;
    }
}
