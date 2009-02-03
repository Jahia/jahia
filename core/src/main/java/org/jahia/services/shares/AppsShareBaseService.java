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

