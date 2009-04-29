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

