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
//
package org.jahia.services.htmleditors;

import java.util.Iterator;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

/**
 * Html Editors Service
 *
 * @author Khue Nguyen
 */
public abstract class HtmlEditorsService extends JahiaService
{
    protected String configFileName = "htmleditors_config.xml";

    /**
     * Reload configuration file from disk
     *
     * @throws JahiaException
     */
    public abstract void reloadConfigurationFile() throws JahiaException;

	/**
	 * Returns an Iterator of all Html Editors registered in the System
	 *
	 * @return all Html Editors registered in the system
     * @throws JahiaException
	 */
    public abstract Iterator getEditors() throws JahiaException;

    /**
     * Returns an Iterator of all Html Editor CSSs a given site can view.
     *
     * @param siteID
     * @return all Html Editor CSSs a given site can view
     * @throws JahiaException
     */
    public abstract Iterator getCSSs(int siteID) throws JahiaException;

    /**
     * Returns an Iterator of all Html Editors a given site can view.
     *
     * @param siteID
     * @return all Html Editors a given site can view
     * @throws JahiaException
     */
    public abstract Iterator getEditors(int siteID) throws JahiaException;

    /**
     * Returns an Editor looking at it id
     *
     * @param id the Editor identifier
     * @return an Editor looking at it id
     * @throws JahiaException
     */
    public abstract HtmlEditor getEditor(String id) throws JahiaException;

    /**
     * Authorize the site to use the given Editor
     *
     * @param siteID
     * @param id the Editor identifier
     * @throws JahiaException
     */
    public abstract void authorizeSite(int siteID, String id) throws JahiaException;

    /**
     * unauthorize the site to use the given Editor
     *
     * @param siteID
     * @param id
     * @throws JahiaException
     */
    public abstract void unAuthorizeSite(int siteID, String id) throws JahiaException;

    /**
     * Returns true if the site has autorization to use the given Editor
     *
     * @param siteID
     * @param id the Editor identifier
     * @return true if the site has autorization to use the given Editor
     */
    public abstract boolean isSiteAutorized(int siteID, String id);

}

