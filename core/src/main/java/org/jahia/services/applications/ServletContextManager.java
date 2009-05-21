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
package org.jahia.services.applications;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.ServletBean;
import org.jahia.data.applications.WebAppContext;
import org.jahia.data.webapps.Security_Role;
import org.jahia.data.webapps.Servlet_Element;
import org.jahia.data.webapps.Web_App_Xml;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.settings.SettingsBean;
import org.springframework.web.context.ServletContextAware;


/**
 * Application context manager service.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.com">khue@jahia.com</a>
 * @version 1.0
 */
public class ServletContextManager implements ServletContextAware {

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ServletContextManager.class);

    private static final String REGISTRY_CACHE_NAME = "ApplicationContextCache";

    /** The web.xml file * */
    private static final String WEB_XML_FILE = "/WEB-INF/web.xml";

    /** Jahia-specific deployment-descriptor file */
    private static final String JAHIA_XML_FILE = "/WEB-INF/jahia.xml";

    /** the instance * */
    private static ServletContextManager instance = null;

    /** the cache of application context beans * */
    private Cache mRegistry;

    /** The Server ServerContext * */
    private ServletContext mContext;

    private boolean initialized = false;

    private CacheService cacheService;

    private SettingsBean settingsBean;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setServletContext(ServletContext servletContext) {
        mContext = servletContext;
        //To change body of implemented methods use File | Settings | File Templates.
    }//--------------------------------------------------------------------------
    /**
     * constructor
     */
    protected ServletContextManager () {
    }


    /**
     * return the singleton instance
     * @return instance of ServletContextManager
     */
    public static synchronized ServletContextManager getInstance () {
        if (instance == null) {
            instance = new ServletContextManager();
        }
        return instance;
    }

    //--------------------------------------------------------------------------
    /**
     * Services initializations
     *
     * @throws org.jahia.exceptions.JahiaInitializationException on error
     */
    public void start()
            throws JahiaInitializationException {
        if (!initialized) {
            mRegistry = cacheService.createCacheInstance(REGISTRY_CACHE_NAME);
            initialized = true;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Get an ApplicationContext for a given application id
     *
     * @param id , the application id
     *
     * @return the application context , null if not found
     * @throws org.jahia.exceptions.JahiaException on error
     */
    public WebAppContext getApplicationContext (int id)
            throws JahiaException {

        logger.debug ("Requested application : [" + id + "]");

        ApplicationBean appBean = ServicesRegistry.getInstance ()
                .getApplicationsManagerService ()
                .getApplication (id);
        if (appBean == null) {
            String errMsg = "Error getting application bean for application [" + id + "]";
            logger.debug (errMsg);

            throw new JahiaException (errMsg, errMsg,
                    JahiaException.ERROR_SEVERITY,
                    JahiaException.APPLICATION_ERROR);
        }

        return getApplicationContext (appBean);
    }

    //--------------------------------------------------------------------------
    /**
     * Get an ApplicationContext for a given context
     *
     * @return the application context , null if not found
     * @param appBean the application bean against which we want the wepapp context
     * @throws org.jahia.exceptions.JahiaException on error
     */
    public WebAppContext getApplicationContext (ApplicationBean appBean)
            throws JahiaException {

        logger.debug ("Requested for context : " + appBean.getContext());

        if (appBean != null && appBean.getContext() == null) {
            return null;
        }
        WebAppContext appContext = null;
        synchronized (mRegistry) {
            appContext = (WebAppContext) mRegistry.get (appBean.getContext());
            if (appContext == null) {
                // try to load from disk
                appContext = loadContextInfoFromDisk (appBean.getID(), appBean.getContext(),appBean.getFilename());
            }
            if (appContext == null) {
                // create a fake Application Context to avoid loading from disk the next time.
                appContext = new WebAppContext(appBean.getContext());
            }
            mRegistry.put (appBean.getContext(), appContext);
        }
        return appContext;
    }

    public void removeContextFromCache(String context) {
        mRegistry.remove(context);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns a ApplicationContext bean with the information loaded from the web.xml
     * file of a given context
     *
     * @param context , the context
     *
     * @return an ApplicationContext or null on error
     * @param applicationID  id of the application
     * @param filename the directory of the application to read from
     */
    private WebAppContext loadContextInfoFromDisk (int applicationID, String context, String filename) {

        ServletContext dispatchedContext = mContext.getContext (context);
        if (dispatchedContext == null) {
            logger.error ("Error getting dispatch context [" + context + "]");
            return null;
        }
        String path = dispatchedContext.getRealPath (WEB_XML_FILE);
        if(!(path.indexOf(context)>0) && !context.startsWith("/")){
            path = filename+WEB_XML_FILE;
        }
        logger.debug ("dispatched context real Path :  " + path);

        // extract data from the web.xml file
        WebAppContext appContext;
        Web_App_Xml webXmlDoc;
        try {
            webXmlDoc = new Web_App_Xml(path);
            webXmlDoc.extractDocumentData ();
        } catch (Exception e) {
            logger.error("Error during loading of web.xml file for application "+ context, e);
            return null;
        }

        appContext = new WebAppContext(context,
                webXmlDoc.getDisplayName (),
                webXmlDoc.getdesc (),
                new ArrayList(),
                webXmlDoc.getServletMappings (),
                new ArrayList(),
                webXmlDoc.getWelcomeFiles ());

        List servlets = webXmlDoc.getServlets ();
        Servlet_Element servlet;
        ServletBean servletBean;
        for (int i = 0; i < servlets.size (); i++) {
            servlet = (Servlet_Element) servlets.get (i);
            servletBean = new ServletBean (
                    applicationID,
                    servlet.getType (),
                    servlet.getDisplayName (),
                    servlet.getName (),
                    servlet.getSource (),
                    context,
                    servlet.getdesc ()
            );
            appContext.addServlet (servletBean);
        }

        List roles = webXmlDoc.getRoles ();
        Security_Role role;
        for (int i = 0; i < roles.size (); i++) {
            role = (Security_Role) roles.get (i);
            appContext.addRole (role.getName ());
        }

        return appContext;
    }

}
