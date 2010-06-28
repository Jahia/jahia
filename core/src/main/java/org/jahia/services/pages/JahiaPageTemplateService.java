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
// 22.01.2001 NK added page definition properties, ACL


package org.jahia.services.pages;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;


/**
 * This interface defines all the methods a page template service should
 * implement, so that it can be integrated into Jahia.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public abstract class JahiaPageTemplateService extends JahiaService {


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
     * Update page template in database and cache.
     * @param thePageTemplate JahiaPageDefinition
     * @throws JahiaException
     */
    public abstract void updatePageTemplate(JahiaPageDefinition thePageTemplate)
        throws JahiaException;

    // Patch ---------------------------------------------------
    // 30.01.2002 : NK patch for old databases containing templates without ACL
    // 				Do create ACL for them.


}

