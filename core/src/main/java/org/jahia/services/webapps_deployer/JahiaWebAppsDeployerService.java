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
//
//  JahiaWebAppsDeployerService
//
//  NK      12.01.2001
//
//


package org.jahia.services.webapps_deployer;


import org.apache.log4j.Logger;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.webapps.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.utils.JahiaTools;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Web Application Deployment service API
 *
 * @author Khue ng
 * @version 1.0
 */
public abstract class JahiaWebAppsDeployerService extends JahiaService {

    /**
     * Factory bean for instantiating and initializing instances of concrete
     * {@link JahiaWebAppsDeployerService} implementations.
     * 
     * @author Sergiy Shyrkov
     */
    public static class WebAppsDeployerServiceFactory extends MethodInvokingFactoryBean {

        @Override
        public Object invoke() throws InvocationTargetException,
                IllegalAccessException {

            JahiaWebAppsDeployerService instance = (JahiaWebAppsDeployerService) super
                    .invoke();
            instance.setSettingsBean(org.jahia.settings.SettingsBean.getInstance());

            return instance;
        }

    }

    private static Logger logger = Logger
            .getLogger(JahiaWebAppsDeployerService.class);
    
    /** The Server Type * */
    protected String m_ServerType = "";

    /** The Server Home Disk Path * */
    protected String m_ServerHomeDiskPath = "";

    /** The full path to the WebApp Root Folder * */
    protected static String m_WebAppRootPath = "";

    /** The full path to the New WebApp Folder * */
    protected static String m_NewWebAppPath = "";

    /** The full path to jahia folder * */
    protected static String m_JahiaHomeDiskPath = "";

    /** The Shared Components Path * */
    private static String m_SharedComponentsPath = "";


    /** The right * */
    protected int m_AppRight = 1;            // FIXME , where to get it ?

    /** Is the web Apps visible by default to all user * */
    protected boolean m_VisibleStatus = true;       // FIXME , where to get it ?

    /** a temporary folder * */
    protected static String m_TempFolderDiskPath = "";

    /** temp folder prefix * */
    protected static String m_TempFolderPrefix = "todelete_";

    /** WEB-INF folder * */
    protected static final String m_WEB_INF = "WEB-INF";

    /** Meta-Inf folder * */
    protected static final String m_META_INF = "Meta-Inf";


    /**
     * The Map of web apps packages.
     * The entry key is the path to an archive file ( war, ear )
     * or an unziped directory
     * The value Object is a List of JahiaWebAppsPackage
     * containing a list of JahiaWebAppDef ( web components informations bean )
     */
    protected static Map<String, JahiaWebAppsPackage> m_WebAppsPackage = new HashMap<String, JahiaWebAppsPackage>();


    /** The web.xml file in case of .war file * */
    private static final String WEB_XML_FILE = "WEB-INF/web.xml";


    /**
     * Services initializations
     *
     */
    public void start()
            throws JahiaInitializationException {

        m_ServerType = settingsBean.getServer ();
        m_ServerHomeDiskPath = settingsBean.getServerHomeDiskPath ();
        m_WebAppRootPath = settingsBean.getJahiaWebAppsDiskPath ();
        m_NewWebAppPath = settingsBean.getJahiaNewWebAppsDiskPath ();
        m_JahiaHomeDiskPath = settingsBean.getJahiaHomeDiskPath ();

        logger.debug (" jahiaHomeDiskPath= " + m_JahiaHomeDiskPath);

        // create the temporary folder
        File f = new File (m_NewWebAppPath);
        File parent = f.getParentFile ();
        if (parent != null) {
            File tmpFolder = new File (parent.getAbsolutePath () + File.separator + "tmp");
            tmpFolder.mkdirs ();
            if (tmpFolder == null || !tmpFolder.isDirectory ()) {
                String errMsg = " cannot create a temporaty folder ";
                logger.error (errMsg);
                throw new JahiaInitializationException (errMsg);
            }
            m_TempFolderDiskPath = tmpFolder.getAbsolutePath ();
        }
    } // end init

//    public void initPortletListener() {
//        PortletContextManager.getManager().addPortletRegistryListener(new PortletRegistryListener() {
//            public void portletApplicationRegistered(PortletRegistryEvent portletRegistryEvent) {
//                try {
//                    registerWebApps(portletRegistryEvent.getApplicationId());
//                } catch (JahiaException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//
//            public void portletApplicationRemoved(PortletRegistryEvent portletRegistryEvent) {
//            }
//        }) ;
//        PortletContextManager.getManager().getRegisteredPortletApplications();
//    }

    /************************************************************************
     * Abstract Methods , need different implementations depending of the
     * type of server ( Tomcat, orion , ... )
     *
     *
     *
     ***********************************************************************/


    /**
     * Hot Deploy a .ear or .war file.
     * Hot Deploy a web component on the specifiy server, active them to
     * be immediately accessible in Jahia
     * If it's a .ear file, the context is the application context
     * If it's a .war file, the context is the web application context
     *
     * @param (JahiaSite) the site
     * @param (String)    context , the context
     * @param (String)    filePath , the full path to the ear file
     *
     * @return (boolean) true if successfull
     */
    public abstract boolean deploy(String context, String filePath)
            throws JahiaException;


    /**
     * Undeploy a web application. Delete the web component from disk.
     *
     * @param app the application bean object
     *
     * @return (boolean) true if successfull
     */
    public abstract boolean undeploy (ApplicationBean app) throws JahiaException;


    /**
     * A Simple way to pass a List of .ear or .war files
     * to be deployed or to be added in the Map of packages
     *
     * @param files    files, a List of .ear application files
     */
    public abstract boolean deploy(List files);

    //-------------------------------------------------------------------------
    /**
     * Return true if deployment functionallities are available or not
     * If they are not available ( due to some init initialization or missing resources),
     * deploy services always return false.
     */
    public abstract boolean canDeploy ();


    /**
     * ***********************************************************************
     * Default Implementations
     * ***********************************************************************
     */

    public static String getNewWebAppsPath () {

        return m_NewWebAppPath;

    }


    public static String getWebAppsRootPath () {

        return m_WebAppRootPath;

    }

    public static String getJahiaHomeDiskPath () {

        return m_JahiaHomeDiskPath;

    }

    public static String getSharedComponentsPath () {

        return m_SharedComponentsPath;

    }


    public void registerWebApps(String context) throws JahiaException {
        // check if an application with same context and servletsrc already exists or not
        if (!context.startsWith("/"))
            context = "/" + context;

        //System.out.println("registerWebApps app context " + appContext + " not used");
//        int parentAclID = 0;
//
//        // Create a new ACL.
//        JahiaBaseACL acl = new JahiaBaseACL();
//        try {
//            acl.create(parentAclID);
//            JahiaUser guest = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(JahiaUserManagerProvider.GUEST_USERNAME);
//            acl.setUserEntry(guest, new JahiaAclEntry(1,0));
//        } catch (ACLNotFoundException ex) {
//            throw new JahiaException("Could not create the page def.",
//                    "The parent ACL ID [" + parentAclID + "] could not be found," +
//                            " while trying to create a new page def.",
//                    JahiaException.TEMPLATE_ERROR, JahiaException.ERROR_SEVERITY, ex);
//        }

        // save definition in db
        ApplicationBean theWebApp = new ApplicationBean(
                "", // id
                context.substring(1),
                context,
                m_VisibleStatus,
                "",
                "portlet"
        );

        ServicesRegistry.getInstance().getApplicationsManagerService()
                .addDefinition(theWebApp);

        //System.out.println("registerWebApps()" + webAppDef.getName() );
    }


    /**
     * Register Web Apps Definition in Jahia
     *
     * @param appContext the context root for the web apps
     * @param filename the package file ( .war or .ear needed for undeploying under orion
     * @param webApps the List of JahiaWebAppDef objects
     */
    public void registerWebApps(String appContext, String filename,
                                List<JahiaWebAppDef> webApps) throws JahiaException {

        int size = webApps.size ();

        //System.out.println("registerWebApps started");

        for (int i = 0; i < size; i++) {

            JahiaWebAppDef webAppDef = webApps.get (i);

            // check if an application with same context and servletsrc already exists or not
            String context = appContext;
            if(!context.startsWith("/"))
                context = "/"+appContext;
            ApplicationBean theWebApp = ServicesRegistry.getInstance ().
                    getApplicationsManagerService ().
                    getApplicationByContext(context);

            if (theWebApp == null) {

                //System.out.println("registerWebApps app context " + appContext + " not used");
//                int parentAclID = 0;
//
//                // Create a new ACL.
//                JahiaBaseACL acl = new JahiaBaseACL ();
//                try {
//                    acl.create (parentAclID);
//                    JahiaUser guest = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(JahiaUserManagerProvider.GUEST_USERNAME);
//                    acl.setUserEntry(guest, new JahiaAclEntry(1,0));
//                } catch (ACLNotFoundException ex) {
//                    throw new JahiaException ("Could not create the page def.",
//                            "The parent ACL ID [" + parentAclID + "] could not be found," +
//                                    " while trying to create a new page def.",
//                            JahiaException.TEMPLATE_ERROR, JahiaException.ERROR_SEVERITY, ex);
//                }

                // save definition in db
                theWebApp = new ApplicationBean (
                        "", // id
                        webAppDef.getName (),
                        context,
                        m_VisibleStatus,
                        webAppDef.getdesc (),
                        webAppDef.getType()
                );

                ServicesRegistry.getInstance ().getApplicationsManagerService ()
                        .addDefinition (theWebApp);

                //System.out.println("registerWebApps()" + webAppDef.getName() );

            }
        }
    }

    /**
     * Try to scan web component deployment descriptor files
     * ( META-INF\applications.xml  or WEB-INF\web.xml file ).
     * A JahiaWebAppsPackage object is returned that holds
     * informations extracted from those files
     *
     * @param path full path to a file or directory
     *
     * @return a  JahiaWebAppsPackage object or null
     */
    public JahiaWebAppsPackage loadWebAppInfo (String path) throws JahiaException{

        synchronized (m_WebAppsPackage) {

            JahiaWebAppsPackage pack = null;

            // check for case sensitive
            if (!JahiaTools.checkFileNameCaseSensitive (path)) {
                return null;
            }

            File f = new File (path);

            try {
                // wait while the file is still modified
                long fLength = f.length ();
                Thread.sleep (500);
                while (fLength != f.length ()) {
                    fLength = f.length ();
                    Thread.sleep (500);
                }
            } catch (Exception tr) {
                return null;
            }

            if (f != null && (f.isFile() || f.isDirectory())) {
                if (f.isDirectory()) {
                    pack = loadWebAppInfoFromDirectory(path);
                } else {
                    pack = loadWebAppInfoFromFile(path);
                }
            }
            return pack;
        }
    }


    /**
     * read the informations contained in a web.xml file
     *
     * @param path , the full path to a directory
     *
     * @return a JahiaWebAppsPackage or null
     */
    protected JahiaWebAppsPackage loadWebAppInfoFromDirectory (String path)
            throws JahiaException {
        return loadWebAppInfoFromDirectory(path, path + File.separator + WEB_XML_FILE,
                path + File.separator +"WEB-INF" + File.separator + "portlet.xml");
    }

    protected JahiaWebAppsPackage loadWebAppInfoFromDirectory (String path, String webxml, String portletxml)
            throws JahiaException {

        Web_App_Xml doc = null;
        JahiaWebAppsPackage pack = null;

        // extract data from the web.xml file
        doc = new Web_App_Xml (webxml);

        String type= "servlet";
        // let's test for presence of portlet.xml file
        File portletXMLFile = new File ( portletxml);
        if (portletXMLFile.exists()) {
            type="portlet";
        }
        File jahiaXMLFile = new File (path + File.separator + "WEB-INF" + File.separator + "jahia.xml");
        if("servlet".equals(type) && ! jahiaXMLFile.exists()) {
                throw new JahiaException("JahiaWebAppsWarPackage",
                                         "Cannot find file WEB-INF/jahia.xml in web application directory",
                                         JahiaException.SERVICE_ERROR,
                                         JahiaException.ERROR_SEVERITY);
            }
        doc.extractDocumentData ();

        // Actually the Context Root for the web application is the current directory
        File tmpFile = new File (path);


        String contextRoot = tmpFile.getName ();
        int endIndex = contextRoot.lastIndexOf(".");
        if(endIndex>0)
        contextRoot = contextRoot.substring(0,endIndex);
        // build the list of the Web Apps Definition
        if(!contextRoot.startsWith("/"))
            contextRoot = "/"+contextRoot;

        pack = new JahiaWebAppsPackage (contextRoot);

        List<Servlet_Element> servlets = doc.getServlets ();
        int size = servlets.size ();

        Servlet_Element servlet = null;

        JahiaWebAppDef webAppDef = new JahiaWebAppDef(contextRoot.substring(1), contextRoot, type);
        webAppDef.addRoles (doc.getRoles ());

        for (int i = 0; i < size; i++) {

            servlet = (Servlet_Element) servlets.get (i);
            webAppDef.addServlet (servlet);
        }

        pack.addWebAppDef (webAppDef);

        if (pack.getWebApps ().size () > 0) {

            pack.setFileName (tmpFile.getName ());
            pack.setFilePath (path);
            if(tmpFile.isDirectory()) {
                pack.setType(JahiaWebAppsPackage.DIR);
            }
        } else {
            return null;
        }

        return pack;
    }


    /**
     * Load Web App info from a file
     *
     * @param path , the full path to a file
     *
     * @return a JahiaWebAppsPackage or null
     */
    protected JahiaWebAppsPackage loadWebAppInfoFromFile (String path) throws JahiaException {

        File fileItem = new File (path);

        JahiaWebAppsPackage pack = null;

        if (fileItem != null && fileItem.getName ().endsWith (".war")) {

            JahiaWebAppsWarPackage warPackage = loadWebAppInfoFromWar (
                    fileItem.getAbsolutePath ());

            if (warPackage != null && warPackage.getWebApps ().size () > 0) {

                pack = new JahiaWebAppsPackage (warPackage.getContextRoot ());
                pack.addWebAppDef (warPackage.getWebApps ());
                pack.setFileName (fileItem.getName ());
                pack.setFilePath (path);
            }

        } else if (fileItem != null && fileItem.getName ().endsWith (".ear")) {

            JahiaWebAppsEarPackage earPackage = loadWebAppInfoFromEar (
                    fileItem.getAbsolutePath ());

            if (earPackage != null && earPackage.getWebApps ().size () > 0) {

                pack = new JahiaWebAppsPackage (earPackage.getContextRoot ());
                pack.addWebAppDef (earPackage.getWebApps ());
                pack.setFileName (fileItem.getName ());
                pack.setFilePath (path);
            }
        }
        return pack;
    }


    /**
     * read the informations contained in a war file
     *
     * @param path , the full path to the file
     *
     * @return a JahiaWebAppsWarPackage or null
     */
    protected JahiaWebAppsWarPackage loadWebAppInfoFromWar (String path) throws JahiaException {

        File fileItem = new File (path);

        if (fileItem != null && fileItem.getName ().endsWith (".war")) {

            // Create a war file Handler
            JahiaWebAppsWarHandler wah = null;

            try {
                wah = new JahiaWebAppsWarHandler (path);
                JahiaWebAppsWarPackage warPackage = wah.getWebAppsPackage ();
                return warPackage;
            } finally {

                if (wah != null) {
                    wah.closeArchiveFile ();
                }
            }

        }
        return null;
    }


    /**
     * read the informations contained in a ear file
     *
     * @param path , the full path to the file
     *
     * @return a JahiaWebAppsEarPackage or null
     */
    protected JahiaWebAppsEarPackage loadWebAppInfoFromEar (String path) throws JahiaException {

        File fileItem = new File (path);

        if (fileItem != null && fileItem.getName ().endsWith (".ear")) {

            // Create a Ear Handler
            JahiaEarFileHandler earh = null;

            try {

                earh = new JahiaEarFileHandler (path);

                JahiaWebAppsEarPackage earPackage = new JahiaWebAppsEarPackage ("/"+
                        JahiaTools.removeFileExtension (fileItem.getName (), ".ear"));

                // Get WebComponents ( list of war files )
                List<Web_Component> webComponents = earh.getWebComponents ();

                int size = webComponents.size ();

                Web_Component webComp = null;
                String webURI = null;     // name of the war file
                JahiaWebAppsWarPackage warPackage = null;

                for (int i = 0; i < size; i++) {

                    webComp = (Web_Component) webComponents.get (i);
                    webURI = webComp.getWebURI ();

                    if (webURI != null && (webURI.length () > 0)) {

                        //logger.debug("start extracting entry " + webURI);

                        // extract the war file
                        earh.extractEntry (webURI, m_TempFolderDiskPath);

                        // get the extracted war file
                        File warFile = new File (
                                m_TempFolderDiskPath + File.separator + webURI);

                        // load info from war
                        warPackage = loadWebAppInfoFromWar (warFile.getAbsolutePath ());
                        if (warPackage != null && (warPackage.getWebApps ().size () > 0)) {
                            earPackage.addWebAppDefs ((List<JahiaWebAppDef>) warPackage.getWebApps ());
                        }

                        warFile.delete ();
                    }
                }

                return earPackage;

            } catch (JahiaException e) {

                String errMsg = "Failed handling webApps file: " + e.getMessage();
                logger.error (errMsg, e);
                if (earh != null) {
                    earh.closeArchiveFile ();
                }

                throw new JahiaException (
                        "JahiaWebAppsDeployerBaseService::loadWebAppInfoFromEar()",
                        "JahiaWebAppsDeployerBaseService: " + errMsg,
                        JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, e);
            } finally {

                // Important to close the JarFile object !!!!
                // cannot delete it otherwise
                if (earh != null) {
                    earh.closeArchiveFile ();
                }
            }

        }
        return null;
    }

    /**
     * delete a package reference in the Map of detected packages
     * and delete it physically from disk.
     *
     * @param (JahiaSite) the site in which the webapp package belong to
     * @param (String)    full path to a file or directory
     *
     * @return true if successful
     */
    public boolean deletePackage(String path) {


        synchronized (m_WebAppsPackage) {

            File tmpFile = new File (path);
            StringBuffer filename = new StringBuffer (tmpFile.getName ());

            //System.out.println(" deletePackage key is =" + filename );

            if (tmpFile != null && tmpFile.isFile ()) {
                if (tmpFile.delete ()) {
                    m_WebAppsPackage.remove (filename.toString ());
                    return true;
                } else {
                    return false;
                }
            } else if (tmpFile != null && tmpFile.isDirectory ()) {

                StringBuffer buff = new StringBuffer (m_TempFolderDiskPath);
                buff.append (File.separator);
                buff.append (m_TempFolderPrefix);
                buff.append (filename.toString ());
                buff.append (JahiaTools.getUniqueDirName ());
                File tmpFolder = new File (buff.toString ());
                //System.out.println(" try to move to " + tmpFolder.getAbsolutePath() );
                if (tmpFile.renameTo (tmpFolder)) {
                    m_WebAppsPackage.remove (filename.toString ());
                    // delete folder
                    JahiaTools.deleteFile (tmpFolder);
                    return true;
                }

            } else {
                m_WebAppsPackage.remove (filename.toString ());
                return true;
            }
            return false;
        }
    }


    /**
     * add a new file or directory only if it is recognized
     * as a valid component package.
     * And only if it is not registered yet.
     *
     * @param path    full path to a file or directory
     */
    public void addNewFile(String path) throws JahiaException {


        synchronized (m_WebAppsPackage) {

            File tmpFile = new File (path);
            StringBuffer filename = new StringBuffer (tmpFile.getName ());
            if (tmpFile != null) {

                JahiaWebAppsPackage pack = null;

                if (m_WebAppsPackage.get (filename.toString ()) == null) {

                    pack = loadWebAppInfo (path);
                    if (pack != null) {
                        m_WebAppsPackage.remove (filename.toString ());
                        m_WebAppsPackage.put (filename.toString (), pack);
                    }
                }
            }
        }
    }


    /**
     * return an Enumerations of the keys of the Map of package
     *
     * @return (Iterator) the Iterator of all keys
     */
    public Iterator<String> getWebAppsPackageKeys () {

        synchronized (m_WebAppsPackage) {
            return m_WebAppsPackage.keySet().iterator();
        }

    }

    /**
     * return an Enumerations of the keys of the Map of package for a gived site
     *
     * @return (Iterator) the Iterator of all keys
     */
    public Iterator<String> getWebAppsPackageKeys (String siteKey) {

        Iterator<String> enumPackages = null;
        List<String> result = new ArrayList<String>();
        synchronized (m_WebAppsPackage) {

            enumPackages = m_WebAppsPackage.keySet().iterator();
            String name = null;
            String siteIdent = siteKey + "_";
            while (enumPackages.hasNext ()) {
                name = enumPackages.next ();
                if (name.startsWith (siteIdent)) {
                    result.add (siteIdent);
                }
            }
            return result.iterator();
        }
    }


    /**
     * return an Iterator of the web apps package Map
     *
     * @return (Iterator) the Iterator of the packages
     */
    public Iterator<Entry<String, JahiaWebAppsPackage>> getWebAppsPackages () {

        return m_WebAppsPackage.entrySet().iterator();
    }


    /**
     * return an Iterator of the web apps package Map
     *
     * @return (Iterator) the Iterator of the packages
     */
    public Iterator<JahiaWebAppsPackage> getWebAppsPackages (String siteKey) {

        List<JahiaWebAppsPackage> result = new ArrayList<JahiaWebAppsPackage>();
        synchronized (m_WebAppsPackage) {

            Iterator<String> enumPackages = m_WebAppsPackage.keySet().iterator();
            String name = null;
            String siteIdent = siteKey + "_";
            while (enumPackages.hasNext ()) {
                name = enumPackages.next ();
                if (name.startsWith (siteIdent)) {
                    result.add (m_WebAppsPackage.get (name));
                }
            }
            return result.iterator();
        }
    }


    /**
     * return a web app package in the Map m_WebAppsPackage
     * looking at the key
     *
     * @param theKey the key
     *
     * @return (Object) a webapps package or null
     */
    public Object getWebAppsPackage (String theKey) {

        synchronized (m_WebAppsPackage) {
            return m_WebAppsPackage.get (theKey);
        }
    }


    /**
     * scan a directory for web apps package.
     * Add them to the Map of WebAppsPackage.
     *
     * @param path full path to a directory
     *
     */
    public void scanDirectory (String path)
            throws JahiaException {

        synchronized (m_WebAppsPackage) {

            JahiaWebAppsPackage pack = null;

            File dir = new File (path);
            if (dir != null && dir.isDirectory ()) {

                File[] files = dir.listFiles ();
                int size = files.length;

                for (int i = 0; i < size; i++) {

                    if (files[i].canWrite ()) {

                        pack = loadWebAppInfo (files[i].getAbsolutePath ());
                        if (pack != null) {
                            //System.out.println("added package in Map " + files[i].getAbsolutePath() );
                            // remove old map
                            m_WebAppsPackage.remove (files[i].getName ());
                            // set the new map
                            m_WebAppsPackage.put (files[i].getName (), pack);
                        }
                    }
                }
            }
        }
    }




    //-------------------------------------------------------------------------
    /**
     * Search and remove the Meta-Inf folder
     *
     * @param parentPath, the path of the parent Folder
     *
     * @return (boolean) true if successfull
     */
    protected boolean removeMetaInfFolder (String parentPath) {

        if (parentPath == null) {
            return false;
        }

        File f = new File (parentPath);
        File[] files = f.listFiles ();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName ().equalsIgnoreCase (m_META_INF)) {
                try {
                    return JahiaTools.deleteFile (files[i], false);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        }

        return false;
    }


} // end JahiaWebAppsDeployerService
