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
//
//  AppsShareBaseService
//
//  NK      31.04.2001
//
package org.jahia.services.shares;

import org.jahia.data.JahiaDOMObject;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaApplicationShareManager;
import org.jahia.services.sites.JahiaSite;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Application Share Service
 *
 * @author Khue ng
 */
public class AppsShareBaseService extends AppsShareService {
    private static AppsShareBaseService m_Instance = null;
    private JahiaApplicationShareManager shareManager;

    public void setShareManager(JahiaApplicationShareManager shareManager) {
        this.shareManager = shareManager;
    }

    //--------------------------------------------------------------------------
    protected AppsShareBaseService () throws JahiaException {
    }


    //--------------------------------------------------------------------------
    public static synchronized AppsShareBaseService getInstance () throws JahiaException {

        if (m_Instance == null) {
            m_Instance = new AppsShareBaseService ();
        }
        return m_Instance;
    }

    public void start() {}

    public void stop() {}

    //--------------------------------------------------------------------------
    /**
     * return an Iterator of sites' key having access to a gived app
     *
     * @return Enumeration, an Iterator of sites' keys
     */
    public Iterator getSites (ApplicationBean app) throws JahiaException {

        List vec = new ArrayList();
        if (app == null) {
            return vec.iterator ();
        }
        return shareManager.getSitesIdForApplicationID (app.getID ());
    }


    //--------------------------------------------------------------------------
    /**
     * add a share between a site and an application
     *
     * @param site  the site
     * @param app the app
     */
    public void addShare (JahiaSite site, ApplicationBean app) throws JahiaException {
        shareManager.addShare (app.getID (), site.getID ());

    }


    //--------------------------------------------------------------------------
    /**
     * remove a share between a site and an application
     *
     * @param site the site
     * @param app the app
     */
    public void removeShare (JahiaSite site, ApplicationBean app) throws JahiaException {
        shareManager.removeShare (app.getID (), site.getID ());

    }


    //--------------------------------------------------------------------------
    /**
     * remove all share referencing a site
     *
     * @param site the site
     */
    public void removeShares (JahiaSite site) throws JahiaException {
        return;
    }


    //--------------------------------------------------------------------------
    /**
     * remove all share referencing an app
     *
     * @param app the app
     */
    public void removeShares (ApplicationBean app) throws JahiaException {
        shareManager.removeSharesByApplication (app.getID ());

    }


    //--------------------------------------------------------------------------
    /**
     * Return a share between a site and an application
     *
     * @param site the site
     * @param app the app
     */
    public AppShare getShare (JahiaSite site, ApplicationBean app) throws JahiaException {
        return shareManager.getShare (app.getID (), site.getID ());

    }


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of application shares content
     *
     * @param siteID the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     */
    public JahiaDOMObject getApplicationSharesAsDOM (int siteID)
            throws JahiaException {
        return null;
    }


}

