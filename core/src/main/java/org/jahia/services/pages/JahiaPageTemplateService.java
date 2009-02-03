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

// 22.01.2001 NK added page definition properties, ACL


package org.jahia.services.pages;

import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import java.util.Iterator;
import java.util.List;


/**
 * This interface defines all the methods a page template service should
 * implement, so that it can be integrated into Jahia.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public abstract class JahiaPageTemplateService extends JahiaService {

    /**
     * Create a new page template.
     *
     * @param siteID    the site identification number
     * @param name        The page template name.
     * @param sourcePath  The page template source path.
     * @param isAvailable True is the page template is available in edition
     *                mode or false if it should be hidden.
     * @param pageType
     *@param image       Image path.
     * @param	parentAclID		The parent ACL id
 * @return Return a new page template instantiation.
     *
     * @throws JahiaException Throws this exception when any error occurred
     *                        in the page template creation process.
     */
    public abstract JahiaPageDefinition createPageTemplate(
            int siteID,
            String name,
            String sourcePath,
            boolean isAvailable,
            String pageType,
            String description,
            String image,
            int parentAclID)
            throws JahiaException;


    /**
     * Deletes the specified page template.
     *
     * @param templateID The page template ID.
     *
     * @throws JahiaException Throws this exception if any error occurred in the deletion process.
     * @throws JahiaException Return this exception if any failure occurred.
     */
    public abstract void deletePageTemplate (int templateID)
            throws JahiaException;


    /**
     * Gets all the page template IDs.
     *
     * @return Return a List of page template IDs
     *
     * @throws JahiaException Return this exception if any failure occurred.
     */
    public abstract List getAllPageTemplateIDs ()
            throws JahiaException;


    /**
     * Check if a page already has the same source path
     *
     * @param siteID    the site identification number
     * @param path      the full path with filename to the template file to check
     *
     * @return Return the reference on the page template having the same
     *         specified source path. Return null if no template exists,
     *         matching the source path.
     */
    public abstract JahiaPageDefinition getPageTemplateBySourcePath (int siteID, String path)
            throws JahiaException;


    /**
     * Try to find the specified page template.
     *
     * @param templateID The page template ID.
     *
     * @return Return a valid instance of a JahiaPageDefinition class. If the
     *         page template doesn't exist an exception is thrown.
     *
     * @throws JahiaException Throws this exception if any error occurred in the lookup process.
     */
    public abstract JahiaPageDefinition lookupPageTemplate (int templateID)
            throws JahiaException;


    /**
     * Try to find the specified page template.
     *
     * @param name      the name of the requested page template.
     * @param siteID    the site identification number
     *
     * @return Return a valid instance of a JahiaPageDefinition class. If the
     *         page template doesn't exist an exception is thrown.
     *
     * @throws JahiaException Throws this exception if any error occurred in the lookup process.
     */
    public abstract JahiaPageDefinition lookupPageTemplateByName (String name, int siteID)
            throws JahiaException;


    /**
     * Return a list of all the page templates depending of the site ID and
     * if there are available or not (according to the passed parameters).
     *
     * @param siteID        the site identification number
     * @param availableOnly Set true to get all the available template, or
     *                      false to the other ones.
     *
     * @return Return an Iterator holding all the page templates matching
     *         the site ID and the visibility requirements.
     */
    public abstract Iterator getPageTemplates (int siteID, boolean availableOnly)
            throws JahiaException;


    /**
     * Return a list of all the page templates depending of the site ID, the
     * user and if there are available or not (according to the passed parameters).
     *
     * @param user          A Jahia user having access to the page template.
     * @param siteID        the site identification number
     * @param availableOnly Set true to get all the available template, or
     *                      false to the other ones.
     *
     * @return Return an Iterator holding all the page templates matching
     *         the site ID and the visibility requirements.
     */
    public abstract Iterator getPageTemplates (JahiaUser user, int siteID,
                                                  boolean availableOnly)
            throws JahiaException;


    /**
     * Return the number of page templates in the database.
     *
     * @return Return the number of page templates.
     */
    public abstract int getNbPageTemplates ()
            throws JahiaException;


    /**
     * Return the number of page templates in the database for a given site.
     *
     * @param siteID    the site identification number
     *
     * @return Return the number of page templates.
     */
    public abstract int getNbPageTemplates (int siteID)
            throws JahiaException;



    /**
     * returns a DOM representation of all page def of a site
     *
     * @param siteID    the site identification number
     */
    public abstract JahiaDOMObject getPageDefsAsDOM (int siteID)
            throws JahiaException;


    /**
     * returns a DOM representation of all page def props of a site
     *
     * @param siteID    the site identification number
     */
    public abstract JahiaDOMObject getPageDefPropsAsDOM (int siteID)
            throws JahiaException;


    /**
     * Returns a List of all page templates' Acl ID of this site
     * Need this for site extraction
     *
     * @param siteID    the site identification number
     */
    public abstract List getAclIDs (int siteID)
            throws JahiaException;

    /**
     * Update page template in database and cache.
     * @param thePageTemplate JahiaPageDefinition
     * @throws JahiaException
     */
    public abstract void updatePageTemplate(JahiaPageDefinition thePageTemplate)
        throws JahiaException;

    // Patch ---------------------------------------------------
    // 30.01.2002 : NK patch for old databases containing templates without ACL
    // 				Do create ACL for them.
    public abstract void patchTemplateWithoutACL () throws JahiaException;

    /**
     * Save the page template and create a new Acl if the current is bull
     * @param thePageTemplate
     * @throws JahiaException
     */
    public abstract void createPageTemplateAcl(JahiaPageDefinition thePageTemplate)
        throws JahiaException;
    

}

