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
//  AppsShareService
//
//  NK      31.04.2001
//
package org.jahia.services.shares;

import org.jahia.data.JahiaDOMObject;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;

import java.util.Iterator;


/**
 * Application Share Service
 *
 * @author Khue ng
 */
public abstract class AppsShareService extends JahiaService {


    /**
     * return an Iterator of sites' key having access to a gived app
     *
     * @return Enumeration, an Iterator of sites' keys
     */
    public abstract Iterator<Integer> getSites (ApplicationBean app) throws JahiaException;


    /**
     * add a share between a site and an application
     *
     * @param JahiaSite,       the site
     * @param ApplicationBean, the app
     */
    public abstract void addShare (JahiaSite site, ApplicationBean app) throws JahiaException;

    /**
     * remove a share between a site and an application
     *
     * @param JahiaSite,       the site
     * @param ApplicationBean, the app
     */
    public abstract void removeShare (JahiaSite site, ApplicationBean app)
            throws JahiaException;

    /**
     * remove all share referencing a site
     *
     * @param JahiaSite, the site
     */
    public abstract void removeShares (JahiaSite site) throws JahiaException;

    /**
     * remove all share referencing an app
     *
     * @param ApplicationBean, the app
     */
    public abstract void removeShares (ApplicationBean app) throws JahiaException;


    //--------------------------------------------------------------------------
    /**
     * Return a share between a site and an application
     *
     * @param JahiaSite,       the site
     * @param ApplicationBean, the app
     */
    public abstract AppShare getShare (JahiaSite site, ApplicationBean app)
            throws JahiaException;


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of application shares content
     *
     * @param int the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     * @author NK
     */
    public abstract JahiaDOMObject getApplicationSharesAsDOM (int siteID)
            throws JahiaException;


}

