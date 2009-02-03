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

