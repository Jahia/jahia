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
package org.jahia.services.htmleditors;

import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;


/**
 * Html Editors Service Default Implementation.
 *
 * @author Khue Nguyen
 */
public class HtmlEditorsBaseService extends HtmlEditorsService
{
    static private HtmlEditorsBaseService instance = null;
    private HtmlEditorsFactory htmlEditorsFactory = null;
    private String htmlEditorsConfigFile = "";

    protected HtmlEditorsBaseService() {
    }

     public static HtmlEditorsBaseService getInstance() {
         if (instance == null) {
             instance = new HtmlEditorsBaseService();
         }
         return instance;
     }

     public void start()
     throws JahiaInitializationException {

         StringBuffer buff = new StringBuffer(settingsBean.getJahiaEtcDiskPath());
         buff.append(File.separator);
         buff.append("htmleditors");
         buff.append(File.separator);
         buff.append(this.configFileName);
         this.htmlEditorsConfigFile = buff.toString();

         try {
             htmlEditorsFactory =
                 new JahiaHtmlEditorsFactory( this.htmlEditorsConfigFile );
         } catch ( Exception t ){
             throw new JahiaInitializationException("Error init Html Editor Service",t);
         }
     }

    public void stop() {}

     /**
      * Reload configuration file from disk
      *
      * @throws JahiaException
      */
     public void reloadConfigurationFile() throws JahiaException{
         htmlEditorsFactory =
                 new JahiaHtmlEditorsFactory( this.htmlEditorsConfigFile );
     }

	/**
	 * Returns an Iterator of all Html Editors registered in the System
	 *
	 * @return all Html Editors registered in the system
     * @throws JahiaException
	 */
    public Iterator getEditors()
    throws JahiaException
    {
        return htmlEditorsFactory.getEditors().iterator();
    }

    /**
     * Returns an Iterator of all Html Editors a given site can view.
     *
     * @param siteID
     * @return all Html Editors a given site can view
     * @throws JahiaException
     */
    public Iterator getEditors(int siteID)
    throws JahiaException
    {
        List res = new ArrayList();
        List editors = htmlEditorsFactory.getEditors();
        int size = editors.size();
        HtmlEditor htmlEditor = null;
        for ( int i=0 ; i<size ; i++ ){
            htmlEditor = (HtmlEditor)editors.get(i);
            if ( htmlEditor.isSiteAuthorized(siteID) ){
                res.add(htmlEditor);
            }
        }
        return res.iterator();
    }

    /**
     * Returns an Iterator of all Html Editor CSSs a given site can view.
     *
     * @param siteID
     * @return all Html Editor CSSs a given site can view
     * @throws JahiaException
     */
    public Iterator getCSSs(int siteID)
    throws JahiaException
    {
        JahiaSite site = ServicesRegistry.getInstance()
                       .getJahiaSitesService().getSite(siteID);
        List res = new ArrayList();

        if ( site == null ){
            return res.iterator();
        }

        List cssList = htmlEditorsFactory.getCSSs();
        int size = cssList.size();
        HtmlEditorCSS css = null;
        for ( int i=0 ; i<size ; i++ ){
            css = (HtmlEditorCSS)cssList.get(i);
            if ( css.isShared() || css.isSiteAllowed(site.getSiteKey()) ){
                res.add(css);
            }
        }
        return res.iterator();
    }

    /**
     * Returns an Editor looking at it id
     *
     * @param id the Editor identifier
     * @return an Editor looking at it id
     * @throws JahiaException
     */
    public HtmlEditor getEditor(String id)
    throws JahiaException
    {
        return htmlEditorsFactory.getEditor(id);
    }

    /**
     * Authorize the site to use the given Editor
     *
     * @param siteID
     * @param id the Editor identifier
     * @throws JahiaException
     * @todo : not implemented yet
     */
    public void authorizeSite(int siteID, String id)
    throws JahiaException
    {
        // TODO
    }

    /**
     * unauthorize the site to use the given Editor
     *
     * @param siteID
     * @param id
     * @throws JahiaException
     */
    public void unAuthorizeSite(int siteID, String id)
    throws JahiaException
    {
        // TODO
    }

    /**
     * Returns true if the site has autorization to use the given Editor
     *
     * @param siteID
     * @param id the Editor identifier
     * @return true if the site has autorization to use the given Editor
     * @todo : actually return true
     */
    public boolean isSiteAutorized(int siteID, String id)
    {
        return true;
    }

}

