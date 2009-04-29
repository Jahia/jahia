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

