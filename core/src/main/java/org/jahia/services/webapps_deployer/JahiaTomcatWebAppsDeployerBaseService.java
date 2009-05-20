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
//
//  JahiaTomcatWebAppsDeployerBaseService
//
//  NK      12.01.2001
//
//


package org.jahia.services.webapps_deployer;

import org.apache.commons.io.FileUtils;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.webapps.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.webapps_deployer.tomcat.TomcatWebAppsDeployer;
import org.jahia.services.webapps_deployer.tomcat.Tomcat_Users_Xml;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.keygenerator.JahiaKeyGen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This Service Register new Application Definition,
 * deploy webApps packaged in a .war or .ear file.
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaTomcatWebAppsDeployerBaseService extends
        JahiaWebAppsDeployerService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaTomcatWebAppsDeployerBaseService.class);

    private static JahiaTomcatWebAppsDeployerBaseService m_Instance = null;

    /**
     * The tomcat tomcat-users.xml file *
     */
    private String m_TomcatUsersXmlFilePath;

    /**
     * The Tomcat User needed to use Tomcat Management Application API * The Tomcat user name *
     * The Tomcat user name *
     */

    /** The Tomcat user name * */
    private static String m_TomcatUserName = "Jahia";

    /**
     * The Tomcat user password *
     */
    private static String m_TomcatUserPassword = "";

    /**
     * The Tomcat user roles *
     */
    private static String m_TomcatUserRoles = "manager";

    /**
     * the HostHttpPath needed to call Tomcat Manager Servlet Application servlet *
     */
    private static String m_JahiaWebAppsDeployerBaseURL = "";

    /**
     * Tomcat initialization error *
     */
    private boolean m_TomcatInitErr = false;

    private String jetspeedDeploymentDirectory;

    /**
     * Constructor
     */
    protected JahiaTomcatWebAppsDeployerBaseService() {

        logger.debug("Starting the Jahia Tomcat WebApps Deployer Base Service");

    }

    /**
     * Use this method to get an instance of this class
     */
    public static synchronized JahiaTomcatWebAppsDeployerBaseService
    getInstance() {

        if (m_Instance == null) {
            m_Instance = new JahiaTomcatWebAppsDeployerBaseService();
        }
        return m_Instance;
    }

    //-------------------------------------------------------------------------
    /**
     * Initialize with Tomcat configuration and disk paths
     */
    public void start()
            throws JahiaInitializationException {

        super.start();

        m_JahiaWebAppsDeployerBaseURL = settingsBean.
                getJahiaWebAppsDeployerBaseURL();

        // add a user with the manager role to tomcat-users.xml
        //System.out.println(" m_ServerHomeDiskPath is " + m_ServerHomeDiskPath );
        m_TomcatUsersXmlFilePath =
                m_ServerHomeDiskPath + "conf" + File.separator +
                        "tomcat-users.xml";

        File tomcatUsersXmlFile = new File(m_TomcatUsersXmlFilePath);
        if (tomcatUsersXmlFile == null || !tomcatUsersXmlFile.isFile()
                || !tomcatUsersXmlFile.canWrite()) {
            logger.debug(
                    "WARNING: " + tomcatUsersXmlFile.getAbsolutePath() + " file cannot be accessed or doesn't exist, application deployment might not work! ");
            //throw new JahiaInitializationException ( errMsg );
            m_TomcatInitErr = true;
        } else {

            if (!addManagerUser(m_TomcatUsersXmlFilePath)) {
                logger.debug(
                        "WARNING: cannot create the tomcat manager user for web apps, application deployment might not work");
                m_TomcatInitErr = true;

            }
        }

        jetspeedDeploymentDirectory = settingsBean.getJetspeedDeploymentDirectory();

    }

    public void stop() {
    }

    //-------------------------------------------------------------------------
    /**
     * Hot Deploy a .ear or .war file.
     * Hot Deploy a web component on the specifiy server, active them to
     * be immediately accessible in Jahia
     * If it's a .ear file, the context is the application context
     * If it's a .war file, the context is the web application context
     *
     * @param context  the context
     * @param filePath the full path to the ear file
     * @return (boolean) true if successfull
     */
    public boolean deploy(String context, String filePath)
            throws JahiaException {

        if (!canDeploy()) {
            return false;
        }

        boolean success = false;
        if (filePath.endsWith(".ear")) {
            success = handleEarFile(filePath);
        } else if (filePath.endsWith(".war")) {
            if (isPortletWarFile(filePath)) {
                success = deployPortletWarFile(context, filePath);
            } else {
                success = deployWarFile(context, filePath);
            }
        } else {
            File tmpFile = new File(filePath);
            if (tmpFile != null && tmpFile.isDirectory()) {
                success = deployDirectory(context, filePath);
            }
        }

        if (success) {
            // deletePackage( filePath);
            return true;
        } else {
            return false;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Undeploy a web application. Delete the web component from disk.
     *
     * @param app the application bean object
     * @return true if successfull
     */

    public boolean undeploy(ApplicationBean app)
            throws JahiaException {

        if (!canDeploy()) {
            return false;
        }

        if (app != null) {

            // call tomcat undeploy url
            undeployWebApp(app.getContext());

            // try to delete physically the directory on disk
            JahiaTools.deleteFile(new File(m_WebAppRootPath + app.getContext()));

            // delete the jsp cache folder
            /*
                         StringBuffer path = new StringBuffer( System.getProperties().getProperty("user.dir") );
                         path.append(File.separator);
                         path.append("work");
                         path.append(File.separator);
                         path.append("localhost");
                         path.append(app.getContext());

                         JahiaTools.deleteFile( new File( path.toString()) );
             */

            return true;
        }

        return false;
    }

    //-------------------------------------------------------------------------
    /**
     * Deploy a single .war web component file
     *
     * @param webContext the web context
     * @param filePath   the full path to the war file
     */
    protected boolean deployWarFile(String webContext,
                                    String filePath)
            throws JahiaException {

        List webApps = new ArrayList();
        StringBuffer webContextDiskPath = new StringBuffer(m_WebAppRootPath);
        webContextDiskPath.append(webContext);

        //StringBuffer webContextBuff = new StringBuffer(site.getSiteKey());
        StringBuffer webContextBuff = new StringBuffer(webContext);
        webApps = handleWarFile(webContextDiskPath.toString(), filePath, true);
        // Activate the Web App in Tomcat
        activateWebApp(webContextBuff.toString(), webContextDiskPath.toString());

        // register Web Apps in Jahia
        File f = new File(filePath);
        registerWebApps(webContextBuff.toString(), f.getName(), webApps);

        // move the war to the context
        File warFile = new File(filePath);
        warFile.delete();

        /*
                 webContextDiskPath.append(File.separator);
                 webContextDiskPath.append(warFile.getName());

         File newPos = new File(webContextDiskPath.toString()+File.separator+m_WEB_INF);
                 if ( !warFile.renameTo(newPos) ){
            warFile.delete();
                 }
         */

        return true;

    }

    private boolean isPortletWarFile(String filePath) {
        // now let's see it the webapp is a JSR-168 portlet file.
        try {
            JarFile warJarFile = new JarFile(filePath);
            JarEntry webinfEntry = warJarFile.getJarEntry("WEB-INF/portlet.xml");
            if (webinfEntry == null) {
                return false;
            }
            return true;
        } catch (IOException ioe) {
            logger.error("Error while trying to open WAR file : " + filePath,
                    ioe);
            return false;
        }
    }

    /**
     * Deploy a single .war web component file
     *
     * @param webContext the web context
     * @param filePath   the full path to the war file
     */
    protected boolean deployPortletWarFile(String webContext,
                                           String filePath)
            throws JahiaException {

        List webApps = new ArrayList();
        StringBuffer webContextDiskPath = new StringBuffer(m_WebAppRootPath);
        webContextDiskPath.append(File.separator);
        webContextDiskPath.append(webContext);

        StringBuffer webContextBuff = new StringBuffer(webContext);
        JahiaWebAppsWarHandler wah = new JahiaWebAppsWarHandler(filePath);
        webApps = wah.getWebAppsPackage().getWebApps();
        JahiaWebAppDef webAppDef = (JahiaWebAppDef) webApps.get(0);
        wah.closeArchiveFile();

        // run pam.deploy for demo app
        // NOTE:  Jetspeed needs to be running at this point to talk to the registry --OR--
        // we can start up a container here to get basic registry services in place
        // the database also needs to be up and running at this time

        File warFile = new File(filePath);
        String fileNamePart = warFile.getName();
        warFile = null;

        // if we got to here it means the file has a .war extension, we simply remove it
        // to get the portletAppName

        String warFileName = filePath;
        String portletAppName = fileNamePart.substring(0,
                fileNamePart.length() - ".war".length());
        webAppDef.setName(portletAppName);

        try {
            // the easiest way to do this is to simply copy the WAR file into the Jetspeed deployment
            // directory.
            /** todo either remove this completely for Pluto or find another way */
            FileUtils.copyFileToDirectory(new File(filePath), new File(jetspeedDeploymentDirectory));

        } catch (Exception t) {
            String errMsg = "Failed to deploy portlet application " +
                    portletAppName + " to web app root " +
                    m_WebAppRootPath + " with WAR file: " + warFileName;
            logger.error(errMsg + "\n" + t.toString(), t);
            throw new JahiaException(
                    "JahiaTomcatWebAppsDeployerBaseService::deployPortletWarFile()",
                    "JahiaTomcatWebAppsDeployerBaseService" + errMsg,
                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, t);
        }

        // register Web Apps in Jahia
        File f = new File(filePath);
        registerWebApps(webContextBuff.toString(), f.getName(), webApps);
        f = null;
        System.gc();

        // move the war to the context
        warFile = new File(filePath);
        // warFile.deleteOnExit();
        int count = 0;
        while ((count < 20) &&
                (!warFile.renameTo(new File(warFile.getName() + ".deployed")))) {
            count++;
            logger.debug("Attempt#" + count + " to rename " + warFile);
            warFile = null;
            System.gc();
            warFile = new File(filePath);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.error("Thread sleep interrupted", ie);
            }
        }

        /*
                 webContextDiskPath.append(File.separator);
                 webContextDiskPath.append(warFile.getName());

         File newPos = new File(webContextDiskPath.toString()+File.separator+m_WEB_INF);
                 if ( !warFile.renameTo(newPos) ){
            warFile.delete();
                 }
         */

        return true;

    }

    //-------------------------------------------------------------------------
    /**
     * Deploy an unziped directory
     *
     * @param webContext the web context
     * @param filePath   the full path to the war file
     */
    protected synchronized boolean deployDirectory(
            String webContext,
            String filePath)
            throws JahiaException {

        String webContextDiskPath = m_WebAppRootPath + webContext;
        File tmpFile = new File(webContextDiskPath);
        File curFile = new File(filePath);
        boolean sameDir = false;

        sameDir = tmpFile.equals(curFile);

        //System.out.println(" to deploy webContext =" + webContext + ", path=" + filePath);
        //System.out.println(" deploy Directory webContextDiskPath =" + webContextDiskPath );

        if (sameDir || !tmpFile.isDirectory()) {

            JahiaWebAppsPackage aPackage = loadWebAppInfoFromDirectory(filePath);
            if (aPackage != null && (aPackage.getWebApps().size() > 0)) {

                // register Web Apps in Jahia
                registerWebApps(webContext, "", aPackage.getWebApps());

                return true;
            }
        }

        return false;

    }

    //-------------------------------------------------------------------------
    /**
     * A simple way to pass a List of .ear of war files
     * to be deployed
     *
     * @param files a List of .ear or .war files
     * @return (boolean) true if done correctly
     */
    public boolean deploy(List files) {

        synchronized (files) {

            int size = files.size();
            File fileItem = null;

            for (int i = 0; i < size; i++) {

                fileItem = (File) files.get(i);

                if (fileItem != null && fileItem.isFile()) {
                    logger.debug("Found new application to deploy : " +
                            fileItem.getName());
                    try {

                        JahiaWebAppsPackage pack = null;
                        if (canDeploy()) {
                            if (fileItem.getName().endsWith(".ear") ||
                                    fileItem.getName()
                                            .endsWith(".war")) {
                                pack = loadWebAppInfo(fileItem.getAbsolutePath());
                                if (pack != null) {
                                    if (!deploy(pack.getContextRoot(),
                                            fileItem.getAbsolutePath())) {
                                        fileItem.delete();
                                        return false;
                                    }
                                } else {
                                    try {
                                        File newFile = new File(
                                                fileItem.getAbsolutePath() +
                                                        "_error");
                                        //newFile.createNewFile();
                                        fileItem.renameTo(newFile);
                                    } catch (Exception t) {
                                        logger.error(
                                                "Error renaming error file " +
                                                        fileItem.toString() + " to " +
                                                        fileItem.getAbsolutePath() +
                                                        "_error",
                                                t);
                                    }
                                }
                            }
                        } else {
                            addNewFile(fileItem.getAbsolutePath());
                        }

                    } catch (JahiaException e) {
                        String errMsg = "Failed deploying Application file " +
                                fileItem.getName();
                        logger.error(errMsg, e);
                        fileItem.delete();
                    }
                }
            }
        }
        return true;
    }

    //-------------------------------------------------------------------------
    /**
     * Handle Ear application File Deployment
     *
     * @param filePath the full path to the ear file
     * @return (boolean) true if success full
     */
    protected boolean handleEarFile(String filePath)
            throws JahiaException {

        //logger.info("started");

        // Create a Ear Handler
        JahiaEarFileHandler earh = null;

        try {

            earh = new JahiaEarFileHandler(filePath);

            // Get WebComponents ( list of war files )
            List webComponents = earh.getWebComponents();
            int size = webComponents.size();

            if (size <= 0) {
                return false;
            }

            // Deploy web components ( war files )
            Web_Component webComp = null;
            String webURI = null; // name of the war file

            for (int i = 0; i < size; i++) {

                webComp = (Web_Component) webComponents.get(i);

                webURI = webComp.getWebURI();

                if (webURI != null && (webURI.length() > 0)) {

                    //logger.debug("start extracting entry " + webURI);

                    // extract the war file
                    earh.extractEntry(webURI, m_TempFolderDiskPath);

                    // get the extracted war file
                    File warFile = new File(m_TempFolderDiskPath +
                            File.separator + webURI);

                    String deployContext = webComp.getContextRoot();
                    if (deployContext == null) {
                        deployContext = JahiaTools.removeFileExtension(warFile.getName(), ".war");
                    } else {
                        if (deployContext.startsWith("/")) {
                            deployContext = deployContext.substring(1);
                        }
                    }

                    if (!deployWarFile(
                            deployContext,
                            warFile.getAbsolutePath())) {
                        return false;
                    }

                }

            }

        } catch (JahiaException e) {

            String errMsg = "Failed handling webApps file: " + e.getMessage();
            logger.error(errMsg, e);
            throw new JahiaException(
                    "JahiaTomcatWebAppsDeployerBaseService::deployEarFile()",
                    "JahiaTomcatWebAppsDeployerBaseService" + errMsg,
                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, e);

        } finally {

            // Important to close the JarFile object !!!!
            // cannot delete it otherwise
            if (earh != null) {
                earh.closeArchiveFile();
            }
        }

        return true;
    }

    //-------------------------------------------------------------------------
    /**
     * extract Web Component file in the web app context and
     * return the list of Web App Definition objects
     *
     * @param webAppContext the full path to the Web App Context Root
     * @param filePath      the full path to the war file
     * @return the list of Web App Definitions
     */
    protected List handleWarFile(String webAppContext, String filePath, boolean unzipWarFile)
            throws JahiaException {

        // Create a WebApp War Handler
        JahiaWebAppsWarHandler wah = null;

        List webApps = new ArrayList();

        try {

            wah = new JahiaWebAppsWarHandler(filePath);

            webApps = wah.getWebAppsPackage().getWebApps();
            int size = webApps.size();

            if (size > 0) {

                // create the dir
                File f = new File(webAppContext);
                if (!f.isDirectory() && !f.mkdirs()) {

                    String errMsg = "Failed creating the war context root dir " + f.getAbsolutePath();
                    logger.error(errMsg);
                    throw new JahiaException(
                            "JahiaTomcatWebAppsDeployerBaseService::deployWarFile()",
                            "JahiaTomcatWebAppsDeployerBaseService: " + errMsg,
                            JahiaException.SERVICE_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }

                // Remove the Meta-Inf folder
                removeMetaInfFolder(f.getAbsolutePath());
                if (unzipWarFile)
                    // extract the war file in the Web App Context Root
                    wah.unzip(webAppContext);

            }

        } catch (JahiaException e) {

            String errMsg = "Failed handling webApps file ";
            logger.error(errMsg + "\n" + e.toString(), e);
            throw new JahiaException(
                    "JahiaTomcatWebAppsDeployerBaseService::deployWarFile()",
                    "JahiaTomcatWebAppsDeployerBaseService" + errMsg,
                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, e);

        } finally {

            // Important to close the JarFile object !!!!
            // cannot delete it otherwise
            if (wah != null) {
                wah.closeArchiveFile();
            }
        }

        return webApps;
    }

    //-------------------------------------------------------------------------
    /**
     * Active Web Application in Tomcat
     *
     * @param context        The Context for the webApps
     * @param webAppDiskPath The absolute disk path to the unpacked web app
     * @return return true if successfull
     */
    protected boolean activateWebApp(
            String context,
            String webAppDiskPath
    )
            throws JahiaException {

        TomcatWebAppsDeployer deployer = new TomcatWebAppsDeployer(
                m_ServerType,
                m_JahiaWebAppsDeployerBaseURL,
                m_TomcatUserName,
                m_TomcatUserPassword
        );
        File tmpFile = new File(webAppDiskPath);
        String fileUrl = null;
        try {
            fileUrl = tmpFile.toURL().toString();
        } catch (java.net.MalformedURLException ue) {
            logger.debug(
                    "Error while activating app with context=" + context +
                            " and webAppDiskPath=" + webAppDiskPath,
                    ue);
            return false;
        }

        return deployer.deploy("/" + context, fileUrl);

    }

    //-------------------------------------------------------------------------
    /**
     * Undeploy a Web Application in Tomcat
     *
     * @param context The Context for the webApps
     * @return return true if success full
     */
    protected boolean undeployWebApp(String context)
            throws JahiaException {

        TomcatWebAppsDeployer deployer = new TomcatWebAppsDeployer(
                m_ServerType,
                m_JahiaWebAppsDeployerBaseURL,
                m_TomcatUserName,
                m_TomcatUserPassword
        );

        if (context.startsWith("/")) {
            deployer.stop(context);
            return deployer.undeploy(context);
        } else {
            deployer.stop("/" + context);
            return deployer.undeploy("/" + context);
        }

    }

    //-------------------------------------------------------------------------
    /**
     * Create the web context
     *
     * @param webContext the web context
     * @return the full web context path
     */
    protected String createWebContext(String webContext) {

        StringBuffer strBuf = new StringBuffer(1024);
        strBuf.append(m_WebAppRootPath);
        strBuf.append(webContext);

        // full path to the application context root folder
        String webContextPath = strBuf.toString();

        File f = new File(webContextPath);

        if (!f.isDirectory() && !f.mkdirs()) {

            return null;
        }

        return webContextPath;
    }

    //-------------------------------------------------------------------------
    /**
     * Add a User with the manager role to tomcat-users.xml
     *
     * @param docPath the full path to the tomcat-users.xml file
     * @return true if successfull
     */
    protected boolean addManagerUser(String docPath) {

        try {

            String password = null;
            m_TomcatUserPassword = JahiaKeyGen.getKey(15);
            Tomcat_Users_Xml doc = new Tomcat_Users_Xml(docPath);

            password = doc.getUserPassword(m_TomcatUserName, m_TomcatUserRoles);
            if (password == null) {
                doc.addUser(m_TomcatUserName, m_TomcatUserPassword,
                        m_TomcatUserRoles);
                doc.write();
            } else if (password.equals("")) {
                doc.updateUser(m_TomcatUserName, m_TomcatUserPassword,
                        m_TomcatUserRoles);
                doc.write();
            } else {
                m_TomcatUserPassword = password;
            }
        } catch (JahiaException e) {
            logger.debug("Exception while modifying Tomcat user file", e);
            return false;
        }
        return true;
    }

    //-------------------------------------------------------------------------
    /**
     * Return true if the tomcat-users.xml has been correctly loaded.
     * If not, deployment functionality are not available and always return false
     *
     * @return (boolean) true if successfull
     */
    public boolean canDeploy() {
        if (m_TomcatInitErr) {
            logger.debug(
                    "Cannot deploy application because of failure of the initialization of the deployment service. Check your logs for warnings or errors.");
        }

        return (!m_TomcatInitErr);

    }

} // end JahiaTomcatWebAppsDeployerService
