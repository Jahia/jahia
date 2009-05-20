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
        applicationBean = new ApplicationBean(appDef.getId().intValue(),
                                              appDef.getName(), appDef.getContext(),
                                              appDef.getVisible().intValue(),
                                              appDef.getShared().intValue() == 1,
                                              appDef.getJahiaAcl().getId().intValue(),
                                              appDef.getFilename(), appDef.getDescription(),
                                              appDef.getType());
        }
        return applicationBean;
    }

    public void addApplication(ApplicationBean app) {
        JahiaAcl jahiaAcl = aclDAO.findLazyAclById(new Integer(app.getRights()));
        JahiaAppDef appDef = new JahiaAppDef();
        appDef.setContext(app.getContext());
        appDef.setDescription(app.getdesc());
        appDef.setFilename(app.getFilename());
        appDef.setJahiaAcl(jahiaAcl);
        appDef.setName(app.getName());
        appDef.setShared(new Integer(app.isShared() ? 1 : 0));
        appDef.setType(app.getType());
        appDef.setVisible(new Integer(app.getVisibleStatus()));
        dao.save(appDef);
        app.setID(appDef.getId().intValue());
    }

    public void updateApplication(ApplicationBean app) {
        try {
            JahiaAppDef appDef = dao.loadApplicationDefinition(new Integer(app.getID()));
            JahiaAcl jahiaAcl = aclDAO.findLazyAclById(new Integer(app.getRights()));
            appDef.setContext(app.getContext());
            appDef.setDescription(app.getdesc());
            appDef.setFilename(app.getFilename());
            appDef.setJahiaAcl(jahiaAcl);
            appDef.setName(app.getName());
            appDef.setShared(new Integer(app.isShared() ? 1 : 0));
            appDef.setType(app.getType());
            appDef.setVisible(new Integer(app.getVisibleStatus()));
            dao.update(appDef);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Try to update a non existing object " + app, e);
        }
    }

    public void removeApplication(int appID) {
        dao.delete(dao.loadApplicationDefinition(new Integer(appID)));
    }

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
