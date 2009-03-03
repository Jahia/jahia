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

// $Id$
//
//  Jahia
//
//  30.10.2000  EV  added in jahia.
//  17.01.2001  AK  change dispatcher method.
//  19.01.2001  AK  replace methods doGet and doPost by the method service.
//  29.01.2001  AK  change re-init way, remove sets methods.
//  10.02.2001  AK  pseudo-bypass the login by forwarding request attributes.
//  27.03.2001  AK  javadoc and change the access to JahiaPrivateSettings.load().
//  28.03.2001  AK  add some jahia path variables.
//  29.03.2001  AK  rename jahia.basic file in jahia.skeleton.
//  20.04.2001  AK  bugfix request uri.
//  17.05.2001  AK  tomcat users check during init.
//  23.05.2001  NK  bug two same parameter in url resolved by removing pathinfo data from request uri
//


package org.jahia.bin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.log4j.Logger;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.mbeans.JahiaMBeanServer;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.SsoValve;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.locks.JahiaLocksRegistry;
import org.jahia.resourcebundle.ResourceMessage;
import org.jahia.security.license.License;
import org.jahia.security.license.LicenseConstants;
import org.jahia.security.license.LicenseManager;
import org.jahia.security.license.LicensePackage;
import org.jahia.security.license.Limit;
import org.jahia.services.cache.CacheService;
import org.jahia.services.deamons.filewatcher.FileListSync;
import org.jahia.services.search.JahiaSearchBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.urls.URI;
import org.jahia.urls.URICodec;
import org.jahia.utils.JahiaChrono;
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Version;
import org.jahia.utils.WebAppPathResolver;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.modifier.TomcatUsersModifier;
import org.xml.sax.SAXException;

/**
 * desc:  This is the main servlet of Jahia.
 *   ----=[  Welcome to the Jahia portal  ]=----
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author  Eric Vassalli
 * @author  Alexandre Kraft
 * @author  Khue N'Guyen
 * @version 1.0
 */
public final class Jahia extends org.apache.struts.action.ActionServlet implements
    JahiaInterface {

    private static final long serialVersionUID = -4811687571425897497L;
    
    private static Logger logger = Logger.getLogger(Jahia.class);
    private static Logger accessLogger = Logger.getLogger("accessLogger");

    /** this class name */
    static public final String CLASS_NAME = "Jahia";

    /** properties filename */
    static private final String PROPERTIES_FILENAME = "jahia.properties";

    /** properties filename */
    static private final String PRELOAD_CLASSES_FILENAME = "preloadclasses.xml";

    /** license filename */
    static private final String LICENSE_FILENAME = "license.xml";

    /** skeleton properties filename */
    static private final String PROPERTIES_BASIC = "jahia.skeleton";

    /** ... */
    static private final String JAHIA_LAUNCH = "jahiaLaunch";

    /** ... */
    static private final String INIT_PARAM_WEBINF_PATH = "webinf_path";

    // web app descriptor initialization parameter
    static private final String INIT_PARAM_SUPPORTED_JDK_VERSIONS =
        "supported_jdk_versions";

    static private final String INIT_PARAM_ADMIN_SERVLET_PATH =
        "admin_servlet_path";

    static private final String INIT_PARAM_CONFIG_SERVLET_PATH =
        "config_servlet_path";

    static private final String CTX_PARAM_CONTEXT_PATH =
        "contextPath";

    static private final String CTX_PARAM_DEFAULT_SERVLET_PATH =
        "defaultJahiaServletPath";

    /** ... */
    static public final String COPYRIGHT =
            "&copy; Copyright 2002-2009  <a href=\"http://www.jahia.org\" target=\"newJahia\">Jahia Ltd</a> -";

    public final static String COPYRIGHT_TXT = "2009 Jahia Ltd." ;

    /**
     * the lock name in the lock registery
     */
    public static final String JAHIA_LOCK_NAME = CLASS_NAME + "_lock_name";

    /** Jahia lock session id */
    public static final String JAHIA_LOCK_SESSION_ID = CLASS_NAME +
        "_lock_session_id";

    /** Jahia lock session id */
    public static final String JAHIA_LOCK = CLASS_NAME + "_lock";

    /** Jahia lock user */
    public static final String JAHIA_LOCK_USER = CLASS_NAME + "_lock_user";

    static private int initTryCount = 0;
    static private boolean reInit = false;
    static private boolean tomcatXMLRestart = false;
    static private boolean mInitError;
    static private boolean mInitWarning;
    static private boolean mInitiated = false;
    static private boolean runInstaller;
    static private boolean maintenance = false;

    public static final String VERSION = "6.0";

    static protected final String JDK_REQUIRED = "1.4";

    private static SettingsBean jSettings;
    private ServletConfig config;
    static private ServletConfig staticServletConfig;

    static protected Exception initException; // used to catch exception on init, where we don't have request and response...
    static protected Exception initWarningException;

    static private String jahiaPropertiesFileName;
    static protected String mLicenseFilename;
    static private String publicKeyStoreResourceName = "/jahiapublickeystore";
    static private String publicKeyStorePassword = "jahiapublickeystore";

    static protected String jahiaBasicFileName;
    static protected String jahiaPropertiesPath;
    static protected String jahiaTemplatesScriptsPath;
    static protected String jahiaEtcFilesPath;
    static protected String jahiaVarFilesPath;
    static protected String jahiaBaseFilesPath = "";

    static private String jahiaServletPath;
    static private String jahiaContextPath;
    static private int jahiaHttpPort = -1;

    static private String jahiaInitAdminServletPath;
    static private String jahiaInitConfigServletPath;

    static private License coreLicense;
    static private ResourceMessage[] licenseErrorMessages;

    static private boolean supportedJDKWarningAlreadyShowed = false;

    static private ThreadLocal<ProcessingContext> paramBeanThreadLocal = new ThreadLocal<ProcessingContext>();
    static private ThreadLocal<HttpServlet> servletThreadLocal = new ThreadLocal<HttpServlet>();
    static private Pipeline authPipeline;
    static private Pipeline processPipeline;

    private static int BUILD_NUMBER = -1;

    /** Jahia server release number */
    private static double RELEASE_NUMBER = 6.0;

    /** Jahia server patch number */
    private static int PATCH_NUMBER = 0;
    

    public static int getBuildNumber() {
        if (BUILD_NUMBER == -1) {
            try {
                URL urlToMavenPom = Jahia.class
                        .getResource("/META-INF/jahia-impl-marker.txt");
                if (urlToMavenPom != null) {
                    InputStream in = Jahia.class
                            .getResourceAsStream("/META-INF/jahia-impl-marker.txt");
                    try {
                        String buildNumber = IOUtils.toString(in);
                        BUILD_NUMBER = Integer.parseInt(buildNumber);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                } else {
                    BUILD_NUMBER = 0;
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                BUILD_NUMBER = 0;
            } catch (NumberFormatException nfe) {
                logger.error(nfe.getMessage(), nfe);
                BUILD_NUMBER = 0;
            }
        }

        return BUILD_NUMBER;
    }

    public static double getReleaseNumber() {
        return RELEASE_NUMBER;
    }

    public static int getPatchNumber() {
        return PATCH_NUMBER;
    }


    //-------------------------------------------------------------------------
    /**
     * Default init inherited from HttpServlet.
     *
     * @param   aConfig  Servlet configuration (inherited).
     */
    public void init (final ServletConfig aConfig)
        throws ServletException {
        super.init(aConfig);
        logger.info("Initializing Jahia...");

        String webinf_path;

        mInitError = false;
        runInstaller = false;
        if (initTryCount < 5) {
            initTryCount++;
        }

        // the first time display the welcome banner...
        if (initTryCount == 1) {
            JahiaConsole.startupWithTrust(getBuildNumber());
        }

        // get servlet basic variables, like confid and context...
        this.config = aConfig;
        staticServletConfig = aConfig;
        final ServletContext context = aConfig.getServletContext();
        final WebAppPathResolver pathResolver = new WebAppPathResolver();
        pathResolver.setServletContext(aConfig.getServletContext());

        /* old system, that doesn't work well when setting up Jahia in root
           context
        Jahia.jahiaContextPath = "/" + this.context.getServletContextName();
        Jahia.jahiaServletPath = "/" + config.getServletName();
        */

        if (jahiaContextPath == null) {
            initContextData(getServletContext());
        }
        // get some value from the web.xml file...
        webinf_path = this.config.getInitParameter(INIT_PARAM_WEBINF_PATH);
        final String supportedJDKVersions = this.config.getInitParameter(
            INIT_PARAM_SUPPORTED_JDK_VERSIONS);
        jahiaInitAdminServletPath = this.config.getInitParameter(
            INIT_PARAM_ADMIN_SERVLET_PATH);
        jahiaInitConfigServletPath = this.config.getInitParameter(
            INIT_PARAM_CONFIG_SERVLET_PATH);

        if ( (!supportedJDKWarningAlreadyShowed) &&
            (supportedJDKVersions != null)) {
            Version currentJDKVersion;
            try {
                currentJDKVersion = new Version(System.getProperty(
                    "java.version"));
                if (!isSupportedJDKVersion(currentJDKVersion,
                                           supportedJDKVersions)) {
                    mInitWarning = true;
                    StringBuffer jemsg = new StringBuffer();
                    jemsg.append("WARNING<br/>\n");
                    jemsg.append(
                        "You are using an unsupported JDK version\n");
                    jemsg.append("or have an invalid ").append(
                                 INIT_PARAM_SUPPORTED_JDK_VERSIONS).append(
                                 " parameter string in \n");
                    jemsg.append(
                        "the deployment descriptor file web.xml.\n");
                    jemsg.append(
                        "<br/><br/>Here is the range specified in the web.xml file : ").
                            append(supportedJDKVersions).append(".\n");
                    jemsg.append(
                            "<br/>If you want to disable this warning, remove the ");
                    jemsg.append(INIT_PARAM_SUPPORTED_JDK_VERSIONS);
                    jemsg.append("\n");
                    jemsg.append(
                        "<br/>initialization parameter in the tomcat/webapps/jahia/WEB-INF/web.xml<br/>\n");
                    jemsg.append("<br/><br/>Please note that if you deactivate this check or use unsupported versions<br/>\n");
                    jemsg.append("<br/>You might run into serious problems and we cannot offer support for these.<br/>\n");
                    jemsg.append("<br/>You may download a supported JDK from <a href=\"http://java.sun.com\" target=\"_newSunWindow\">http://java.sun.com</a>.");
                    jemsg.append("<br/><br/>&nbsp;\n");
                    initWarningException = new JahiaException(jemsg.toString(),
                        "JDK version warning",
                        JahiaException.INITIALIZATION_ERROR,
                        JahiaException.WARNING_SEVERITY);
                    logger.error("Invalid JDK version", initWarningException);
                }
            } catch (NumberFormatException nfe) {
                logger.warn("Couldn't convert JDK version to internal version testing system, ignoring JDK version test...");
            }
            supportedJDKWarningAlreadyShowed = true;
        }

        if (Jahia.jahiaInitAdminServletPath == null) {
            logger.warn("Error in web.xml for init parameter " +
                         INIT_PARAM_ADMIN_SERVLET_PATH +
                         ". Make sure it's set...Trying to use hardcoded /administration/ dispatching ...");
            jahiaInitAdminServletPath = "/administration/";
        }

        if (Jahia.jahiaInitConfigServletPath == null) {
            logger.warn("Error in web.xml for init parameter " +
                         INIT_PARAM_CONFIG_SERVLET_PATH +
                ". Make sure it's set...Trying to use hardcoded /installation/ dispatching ...");
            jahiaInitConfigServletPath = "/installation/";
        }

        jahiaEtcFilesPath = context.getRealPath(webinf_path + "/etc");
        jahiaVarFilesPath = context.getRealPath(webinf_path + "/var");

        // set default paths...
        jahiaPropertiesPath = context.getRealPath(webinf_path +
            "/etc/config/");
        jahiaPropertiesFileName = jahiaPropertiesPath + File.separator +
                                  PROPERTIES_FILENAME;
        jahiaBasicFileName = jahiaPropertiesPath + File.separator +
                             PROPERTIES_BASIC;
        mLicenseFilename = jahiaPropertiesPath + File.separator +
                           LICENSE_FILENAME;

        jahiaBaseFilesPath = context.getRealPath(webinf_path + "/var");
        jahiaTemplatesScriptsPath = jahiaBaseFilesPath + File.separator +
                                    "templates";

        // now let's preload some classes that have static initializations
        // that need to be performed before we go further...
        String preloadConfigurationFileName = jahiaPropertiesPath +
                                              File.separator +
                                              PRELOAD_CLASSES_FILENAME;
        try {
            // the constructor does everything, including loading the classes,
            // so we don't need to do anything besides creating an instance that
            // we can dispose of immediately after.
            new ClassesPreloadManager(preloadConfigurationFileName);
        } catch (IOException ioe) {
            logger.warn("IO exception raised while trying to load classes preload XML configuration file [" +
                         preloadConfigurationFileName + "]", ioe);
        } catch (SAXException saxe) {
            logger.warn(
                "IO exception while trying to parse classes preload XML configuration file [" +
                preloadConfigurationFileName + "]", saxe);
        } catch (ClassNotFoundException cnfe) {
            logger.warn("Could not preload class because it couldn't be found",
                         cnfe);
        }
        // none of the above exceptions are fatal, they just don't preload all
        // the classes, but this might cause problems elsewhere !

        // check if there is a jahia.properties file...
        final File jahiaProperties = new File(jahiaPropertiesFileName);
        boolean jahiaPropertiesExists = jahiaProperties.exists();

        /* init the listener registry */
        //JahiaListenersRegistry.getInstance().init( this.config );

        // if the jahia properties file exists try to init...
        if (!jahiaPropertiesExists) {
            // jahia.properties doesn't exists, launch JahiaConfigurationWizard...
            Jahia.runInstaller = true;
            return;
        }
        
        // Check if the license file exists.
        final File licenseFile = new File(mLicenseFilename);
        if (!licenseFile.exists()) {
            logger.fatal(
                "Could not find jahia.license file (was looking for it at " +
                licenseFile.toString() + ")");
            mInitError = true;
            return;
        }

        // Check the license file
        mInitError = (! checkLicense());
        if (mInitError) {
            logger.fatal("Invalid License !");
            initException = new JahiaException("Invalid License",
                "Environement Initialization Exception",
                JahiaException.
                        INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY);
            return;
        }

        try {
            // retrieve the jSettings object...
            Jahia.jSettings = SettingsBean.getInstance();
            
            Jahia.jSettings.setBuildNumber(getBuildNumber());
            
            Jahia.jSettings
                    .setDeprecatedNonContainerFieldsUsed(((JahiaFieldsDataManager) SpringContextSingleton
                            .getInstance().getContext().getBean(
                                    JahiaFieldsDataManager.class.getName()))
                            .hasActiveFieldsWithoutContainer());
        } catch (NullPointerException npe) {
            // error while reading jahia.properties, launch JahiaConfigurationWizard...
            Jahia.runInstaller = true;
            return;
        } catch (NumberFormatException nfe) {
            // error while reading jahia.properties, launch JahiaConfigurationWizard...
            Jahia.runInstaller = true;
            return;
        }

        // check server type... and if it's tomcat, check the tomcat-users.xml file...
        tomcatXMLRestart = TomcatUsersModifier.ensureValidity(
            jahiaPropertiesFileName);

        // Initialize all the registered services.
        try {
            JahiaMBeanServer.getInstance().init(getSettings());
            if (initServicesRegistry()) {
                try {
                    EnginesRegistry.getInstance().init();
                    JahiaListenersRegistry.getInstance().init(config);
                    FileListSync.getInstance().start();
                } catch (NullPointerException ex) {
                    logger.fatal(
                        "CRITICAL : Error while initializing one of the needed services!", ex);

                    // init error, stop Jahia!
                    mInitError = true;
                    return;
                }
                // 30.01.2002 NK : Patch for old database with templates without ACL
                ServicesRegistry.getInstance().
                    getJahiaPageTemplateService().
                    patchTemplateWithoutACL();

                ServicesRegistry.getInstance().getSchedulerService().startSchedulers();

                // Todo : Have a convenience way to add listeners from service
//                JahiaEventListenerInterface listener = ServicesRegistry.getInstance()
//                                                       .getMetadataService().getMetadataEventListener();
//                JahiaListenersRegistry.getInstance().addListener(listener);
            }

            // Check the license file
            mInitError = (! checkLicenseLimit());
            if (mInitError) {
                String licenseErrors = processLicenseErrorMessages();
                if (licenseErrors == null) {
                    initException = new JahiaException(
                        "License Limit Violation",
                        "Environement Initialization Exception",
                        JahiaException.INITIALIZATION_ERROR,
                        JahiaException.FATAL_SEVERITY);
                } else {
                    initException = new JahiaException(
                        licenseErrors,
                        licenseErrors,
                        JahiaException.INITIALIZATION_ERROR,
                        JahiaException.FATAL_SEVERITY);
                }
                logger.fatal(
                        "License error: " + initException.getMessage(), initException);
                return;
            }
            // JahiaMBeanServer.getInstance().init(getSettings());

            // Activate the JMS synchronization if needed
            final CacheService factory = ServicesRegistry.getInstance().getCacheService();
            if (factory.isClusterCache()) {
                factory.enableClusterSync();
            }

            // let's set the URI generation default encoding according to
            // the Jahia settings.
            URICodec.setDefaultEncoding(jSettings.getDefaultURIEncoding());
            URI.setDefaultEncoding(jSettings.getDefaultURIEncoding());

            createAuthorizationPipeline(config);
            createProcessingPipeline(config);

            // initialize content portlets
            ServicesRegistry.getInstance().getJahiaWebAppsDeployerService().initPortletListener();

            /* todo let's find a cleaner way to initialize this static repository reference, maybe if we move
             * Jackrabbit to be initialized using Spring we could initialize these dependencies using Spring
              * injections ? */
            // Initialize this to make sure we call it in Jahia's context instead of a portlet's context, as
            // portlets also use Jackrabbit-stored preferences.
            JahiaAccessManager.getRepository();
            JahiaAccessManager.getJahiaUserService();

        } catch (Exception je) {
            logger.error("Error during initialization of Jahia", je);
            // init error, stop Jahia!
            mInitError = true;
            initException = je;
            return;
        }

        mInitiated = true;

    } // end init

    private void createAuthorizationPipeline (final ServletConfig aConfig) throws JahiaException {
        try {
            authPipeline = (Pipeline) SpringContextSingleton.getInstance().getContext().getBean("authPipeline");
            authPipeline.initialize();
        } catch (PipelineException e) {
            Throwable t = e;
            if (e.getNested() != null) {
                t = e.getNested();
                logger.error("Error while initializing authorization pipeline", t);
            }
            throw new JahiaException(
                "Error while initializing authorization pipeline",
                t.getMessage(), JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY, t);
        } catch (Exception e) {
            throw new JahiaException(
                "Error while initializing authorization pipeline",
                e.getMessage(), JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY, e);
        }
    }

    private void createProcessingPipeline (final ServletConfig aConfig) throws JahiaException {
        try {
            processPipeline = (Pipeline) SpringContextSingleton.getInstance().getContext().getBean("processPipeline");
            processPipeline.initialize();
        } catch (PipelineException e) {
            Throwable t = e;
            if (e.getNested() != null) {
                t = e.getNested();
                logger.error("Error while initializing authorization pipeline", t);
            }
            throw new JahiaException(
                "Error while initializing authorization pipeline",
                t.getMessage(), JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY, t);
        } catch (Exception e) {
            throw new JahiaException(
                "Error while initializing authorization pipeline",
                e.getMessage(), JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY, e);
        }
    }

    public void destroy() {

        logger.info("shutdown requested !");

        // check first if Jahia was initialized or if we just ran the configuration wizard.
        if (!mInitiated) {
            super.destroy();
            return;
        }
        try {
            // first we shutdown the scheduler, because it might be running lots of
            // jobs.
            ServicesRegistry.getInstance().getSchedulerService().stop();
        } catch (Exception je) {
            logger.debug("Error while stopping job scheduler", je);
            if (!logger.isDebugEnabled()) {
                logger.info("Unable to stop job scheduler");
            }
        }
        try {
            // Flush locks for this server
            ServicesRegistry.getInstance().getLockService().purgeLocksForServer();
        } catch (Exception je) {
            logger.debug("Error while flushing locks", je);
            if (!logger.isDebugEnabled()) {
                logger.debug("Unable to flush locks");
            }
        }
        try {
            ServicesRegistry.getInstance().shutdown();
        } catch (Exception je) {
            logger.debug("Error while shutting down services", je);
            if (!logger.isDebugEnabled()) {
                logger.info("Unable to shut down services");
            }
        }
        super.destroy();

        logger.info("done shuting down !");

    }


    /*
     * Default service inherited from HttpServlet.
     * @author  Alexandre Kraft
     * @author  Fulco Houkes
     *
     * @param   request     Servlet request (inherited).
     * @param   response    Servlet response (inherited).
     */
    public void service (final HttpServletRequest request,
                         final HttpServletResponse response)
        throws IOException,
        ServletException {

        /** todo Can we put this someplace else, and furthermore can be only
         *  do this if we have a session variable set or something ?
         */
        // The following part is the most important part of the servlet, which makes sure
        // we read all the encodings in UTF-8. This is documented in the Servlet API 2.3
        // specification, under the SRV 4.9 section, page 37
        // logger.debug("Character encoding passed: " + request.getCharacterEncoding() );
        if (jSettings != null) {
            if (jSettings.isUtf8Encoding()) {
                // bad browser, doesn't send character encoding :(
                // we can force the encoding ONLY if we do this call before any
                // getParameter() call is done !
                request.setCharacterEncoding("UTF-8");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("--------------------------------------------- NEW "
                    + request.getMethod().toUpperCase() + " REQUEST ---");
            logger.debug("New request, URL=[" + request.getRequestURI()
                    + "], query=[" + request.getQueryString()
                    + "] serverName=[" + request.getServerName() + "]");

        logger.debug("Character encoding set as: "
                     + request.getCharacterEncoding());
        }

        copySessionCookieToRootContext(request, response);

        HttpSession session = request.getSession(false);
        if (session == null || !request.isRequestedSessionIdValid()) {
            // Session could be false because of new user, or missing JSESSIONID
            // id for a non-cookie browser, or a false cookie maybe ?
            logger.debug("Session is null");
            session = request.getSession(true);
            logger.debug("New session id=[" + session.getId() + "]");
        }

        if (Jahia.isInitiated() && !checkLockAccess(session)) {
            logger.debug("Jahia is locked by the super admin!");
            config.getServletContext().getRequestDispatcher(
                    "/errors/locked.jsp").forward(request, response);
            return;
        }

        // WEB APP ISSUE
        // This session attribute is used to track that targeted application
        // has been already processed or not.
        final String appid = request.getParameter("appid");
        if ( appid != null ){
            if ( "true".equals(request.getParameter("resetAppSession")) ){
                session.removeAttribute("org.jahia.services.applications.wasProcessed." + appid);
            }
        }

        try {

            if (jahiaContextPath == null) {
                jahiaContextPath = request.getContextPath();
            }

            if (Jahia.jahiaHttpPort == -1) {
                Jahia.jahiaHttpPort = request.getServerPort();
            }

            // check witch action to do...
            if (mInitWarning) { // init error, stop Jahia...
                // we only display warnings once.
                Exception exToThrow = initWarningException; 
                initWarningException = null;
                mInitWarning = false;
                if (exToThrow != null) {
                    throw exToThrow;
                }
            } else if (mInitError) { // init error, stop Jahia...
                logger.error("INIT ERROR. Jahia is not started.");
                if (initException != null) {
                    throw initException;
                }

                return;

            } else if (runInstaller || SpringContextSingleton.getInstance().getContext() == null) { // run the installer...
                logger.debug("Redirecting to Configuration Wizard...");
                runInstaller = false;
                reInit = true;
                jahiaLaunch(request, response, Jahia.jahiaInitConfigServletPath);

                return;

            } else if (request.getAttribute(JAHIA_LAUNCH) != null) { // redirect on an another servlet...

                if (request.getAttribute(JAHIA_LAUNCH).equals("installation")) {
                    logger.debug("Redirecting to Configuration Wizard...");
                    jahiaLaunch(request, response,
                                Jahia.jahiaInitConfigServletPath);
                    return;
                    } else if (request.getAttribute(JAHIA_LAUNCH).equals(
                            "administration")) {
                        logger.debug("Redirecting to Administration...");
                        jahiaLaunch(request, response,
                                    Jahia.jahiaInitAdminServletPath);
                        return;
                    }

            } else if (reInit) { // re-init Jahia...
                logger.debug("Reinitializing Jahia...");
                Jahia.reInit = false;
                init(config);
            } else if (maintenance) {
                jahiaLaunch(request, response, "/errors/maintenance.jsp");
                return;
            }

            if (ParamBean.getDefaultSite() == null) {
                JahiaSitesService jahiaSitesService = ServicesRegistry.getInstance().getJahiaSitesService();
                if (jahiaSitesService.getNbSites() > 0) {
                    JahiaSite jahiaSite = jahiaSitesService.getSiteByKey(org.jahia.settings.SettingsBean.getInstance().getDefaultSite());
                    if (jahiaSite == null) {
                        jahiaSite = (JahiaSite) jahiaSitesService.getSites().next();
                    }
                    jahiaSitesService.setDefaultSite(jahiaSite);
                } else {
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {
                        for (int i = 0; i < cookies.length; i++) {
                            Cookie curCookie = cookies[i];
                            if (curCookie.getName().equals("jahiaWizardKey")) {
                                String value = new String(Base64
                                        .decodeBase64(URLDecoder.decode(
                                                curCookie.getValue(), "UTF-8")
                                                .getBytes("UTF-8")), "UTF-8");
                                StringTokenizer t = new StringTokenizer(value,":");
                                String name = t.nextToken();
                                final JahiaUser rootUser = ServicesRegistry.getInstance().
                                        getJahiaUserManagerService().lookupUser(name);
                                if (rootUser != null) {
                                    if (rootUser.verifyPassword(t.nextToken())) {
                                        session.setAttribute(ProcessingContext.SESSION_USER,
                                                rootUser);
                                        session.setAttribute(JahiaAdministration.CLASS_NAME + "isSuperAdmin",Boolean.TRUE);
                                        session.setAttribute(JahiaAdministration.CLASS_NAME + "accessGranted",Boolean.TRUE);
                                        session.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaLoginUsername", name);
                                        session.setAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia",Boolean.TRUE);
                                    }
                                } 
                            }
                        }
                    }

                    jahiaLaunch(request, response,
                                Jahia.jahiaInitAdminServletPath+"?do=sites&sub=list");
                    return;
                }
            }

            // please restart tomcat... config has been modified...
            /*if (tomcatXMLRestart) {
                session = request.getSession();
                if (session != null) {
                    session.setAttribute(JahiaAdministration.CLASS_NAME +
                            "jahiaDisplayMessage",
                            "Web applications are being deployed...<br/>" +
                            "Please wait a few moments until it is done<br/>" +
                            "and then shutdown and restart Jahia");
                }
            }   */

            // create the parambean (jParams)...
            final ParamBean jParams = createParamBean(request,response,session);

            if (jParams == null) {
                logger.warn("ParamBean not available, aborting processing...");
                return;
            }
            servletThreadLocal.set(this);
            ServicesRegistry.getInstance().getSchedulerService().startRequest();

            request.setAttribute("org.jahia.params.ParamBean",
                    jParams);
            process(request, response);

            // display time to fetch object plus other info
            if (jParams.getUser() != null && logger.isInfoEnabled()) {
                if (!"true".equals(jParams.getSessionState().getAttribute("needToRefreshParentPage"))) {
                    StringBuffer sb = new StringBuffer(100);
                    sb.append("Processed [").append(jParams.getRequest().getRequestURI());
                    sb.append("] user=[").append(jParams.getUser().getUsername() )
                            .append("] ip=[" ).append(jParams.getRequest().getRemoteAddr() )
                            .append("] sessionID=[").append(jParams.getSessionID())
                            .append("] in [" ).append(JahiaChrono.getInstance().read(jParams.getStartTime()) )
                            .append("ms]");
                            
                    logger.info(sb.toString());
                }
            }
            if (accessLogger.isDebugEnabled()) {
                accessLogger.debug(new StringBuffer(255).append(";").append(jParams.getRealRequest().getRemoteAddr()).append(";").append(jParams.getSiteID()).append(";").append(jParams.getPageID()).append(";").append(jParams.getLocale().toString()).append(";").append(jParams.getUser().getUsername()));
            }
            paramBeanThreadLocal.set(null);
            servletThreadLocal.set(null);
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
            try {
                ServicesRegistry.getInstance().getSchedulerService().endRequest();
            } catch (JahiaException e) {
                logger.error("Cannot start delayed jobs ",e);
            }
        } catch (Exception e) {
            ErrorHandler.getInstance().handle(e, request, response);
        } finally {
            JahiaSearchBaseService.closeAllOpenLuceneQueryRequestOrSearcher();
            paramBeanThreadLocal.set(null);
            servletThreadLocal.set(null);
        }
    } // end service

    public static ParamBean createParamBean(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final HttpSession session)
    throws IOException, JahiaSiteNotFoundException, JahiaException {

        // all is okay, let's continue...
        boolean exitAdminMode;
        final Integer I = (Integer) session.getAttribute(ProcessingContext.
             SESSION_JAHIA_RUNNING_MODE);

        ParamBean jParams = null;
        try {
            final ProcessingContextFactory pcf = (ProcessingContextFactory) SpringContextSingleton.
                    getInstance().getContext().getBean(ProcessingContextFactory.class.getName());

            jParams = pcf.getContext(request, response, getStaticServletConfig().getServletContext());

            if (jParams != null) {

                exitAdminMode = ( (I != null) &&
                        (I.intValue() == ADMIN_MODE) &&
                        jParams.getEngine().equals(ProcessingContext.
                        CORE_ENGINE_NAME));

                paramBeanThreadLocal.set(jParams);

                // tell we are in Jahia Core mode
                if (exitAdminMode) {
                    session.setAttribute(ProcessingContext.
                            SESSION_JAHIA_RUNNING_MODE,
                            Integer.valueOf(Jahia.CORE_MODE));
                    logger.debug("Switch to Jahia.CORE_MODE");
                }
            }
        } catch (JahiaPageNotFoundException ex) {
            // PAGE NOT FOUND EXCEPTION
            logger.debug(ex.getJahiaErrorMsg(), ex);
//            logger.error(ex.getJahiaErrorMsg());
            String requestURI = request.getRequestURI();
            JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");
            if (site != null && requestURI.indexOf("/op/edit") > 0) {
                    String redirectURL = requestURI;
                    int pidPos = requestURI.indexOf("/pid/");
                    if (pidPos != -1) {
                        // found PID in URL, let's replace it's value.
                        int nextSlashPos = requestURI.indexOf("/", pidPos + "/pid/".length());
                        if (nextSlashPos == -1) {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID();
                        } else {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID() + requestURI.substring(nextSlashPos);
                        }
                        response.sendRedirect(redirectURL);                        
                    } else {
                        pidPos = requestURI.substring(0,requestURI.length()-2).lastIndexOf("/");
                        // We are using url key
                        int nextSlashPos = requestURI.indexOf("/", pidPos + "/pid/".length());
                        if (nextSlashPos == -1) {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID();
                        } else {
                            redirectURL = requestURI.substring(0, pidPos) + "/pid/"+site.getHomePageID() + requestURI.substring(nextSlashPos);
                        }
                        response.sendRedirect(redirectURL);
                    }
            } else {
                throw ex;
            }
        }
        return jParams;
    }

    /**
     * Helper method to copy JSESSION cookies from a non null context to a root
     * context. Warning this MIGHT disrupt normal functioning of the servlet
     * container.
     * @param request the request object containing the cookies to read
     * @param response the response object in which to copy the session cookie
     * to the root context.
     */
    public static void copySessionCookieToRootContext (final HttpServletRequest request,
                                                       final HttpServletResponse response) {

        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                final Cookie curCookie = cookies[i];
                if ("JSESSIONID".equals(curCookie.getName())) {
                    String curPath = curCookie.getPath();
                    if ( (curPath != null) && (! ("".equals(curPath)))) {
                        // we found a session cookie that has a non null path,
                        // let's copy it to a null path.
                        logger.debug(
                            "Copying non-root context cookie to root context");
                        Cookie rootCookie = (Cookie) curCookie.clone();
                        rootCookie.setPath("");
                        response.addCookie(rootCookie);
                    }
                }
                if (logger.isDebugEnabled()){
                    logger.debug("Cookie domain=[" + curCookie.getDomain() +
                            "] path=[" + curCookie.getPath() +
                            "] name=[" + curCookie.getName() +
                            "] value=[" + curCookie.getValue() + "]");
                }
            }
        } else {
            logger.debug("No cookies found.");
        }
    }

    /* static accessors...
     */
    public static String getJahiaPropertiesFileName () {
        return jahiaPropertiesFileName;
    }

    public static String getServletPath () {
        return jahiaServletPath;
    }

    public static String getContextPath () {
        return jahiaContextPath;
    }

    public static int getJahiaHttpPort() {
        return jahiaHttpPort;
    }

    public static void setJahiaHttpPort(int theJahiaHttpPort) {
        Jahia.jahiaHttpPort = theJahiaHttpPort;
    }

    public static String getInitAdminServletPath () {
        return jahiaInitAdminServletPath;
    }

    public static String getInitConfigServletPath () {
        return jahiaInitConfigServletPath;
    }

    public static ProcessingContext getThreadParamBean () {
        return paramBeanThreadLocal.get();
    }

    public static void setThreadParamBean(final ProcessingContext processingContext) {
        paramBeanThreadLocal.set(processingContext);
    }

    public static HttpServlet getJahiaServlet () {
        return servletThreadLocal.get();
    }

    //-------------------------------------------------------------------------
    /**
     * Call the initialization of the services registry.
     *
     * @return  Return <code>true</code> on success or <code>false</code> on any failure.
     */
    protected boolean initServicesRegistry ()
        throws JahiaException {

        logger.debug("Start the Services Registry ...");

        try {
            final ServicesRegistry registry = ServicesRegistry.getInstance();
            if (registry != null) {
                registry.init(Jahia.jSettings);
                logger.debug("Services Registry is running...");
                return true;
            }

            logger.debug(
                "  -> ERROR : Could not get the Services Registry instance.");
            return false;
        } catch (JahiaException je) {
            throw new JahiaException(je.getJahiaErrorMsg(),
                "Service Registry Initialization Exception",
                JahiaException.INITIALIZATION_ERROR,
                JahiaException.FATAL_SEVERITY,
                je);
        }
    } // end initServicesRegistry

    //-------------------------------------------------------------------------
    /**
         * Forward the flow to an another servlet. The name of the destination servlet
     * is defined on the <code>destination</code> parameter.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   destination   Destination for requestDispatcher.forward().
     */
    private void jahiaLaunch (final HttpServletRequest request,
                              final HttpServletResponse response,
                              final String destination) {
        try {
            getServletContext().getRequestDispatcher(destination).forward(
                request, response);

        } catch (IOException ie) {
            logger.error("IOException on method jahiaLaunch.", ie);
        } catch (ServletException se) {
            logger.error("ServletException on method jahiaLaunch.", se != null
                    && se.getRootCause() != null ? se.getRootCause() : se);
        }
    } // end jahiaLaunch

    //-------------------------------------------------------------------------
    private boolean checkLicense () {

        if (coreLicense == null) {
            try {
                final LicenseManager licenseManager = LicenseManager.getInstance();
                licenseManager.load(mLicenseFilename);
                final LicensePackage jahiaLicensePackage = licenseManager.
                        getLicensePackage(LicenseConstants.JAHIA_PRODUCT_NAME);
                coreLicense = jahiaLicensePackage.getLicense(LicenseConstants.CORE_COMPONENT);
                final InputStream keystoreIn =
                    Jahia.class.getResourceAsStream(
                        publicKeyStoreResourceName);
                if (keystoreIn != null) {
                    final String keystorePassword = publicKeyStorePassword;
                    boolean signaturesOk =
                        licenseManager.verifyAllSignatures(keystoreIn,
                        keystorePassword);
                    if (signaturesOk) {
                        logger.debug("Signatures are valid");
                    } else {
                        logger.error("Invalid license signatures !");
                        coreLicense = null;
                    }
                } else {
                    logger.error("Error while loading public key store file [" + publicKeyStoreResourceName + "] from classpath");
                    coreLicense = null;
                }

            } catch (IOException ioe) {
                coreLicense = null;
                logger.error("Error during license check ", ioe);
                return false;
            } catch (SAXException saxe) {
                coreLicense = null;
                logger.error("Error during license check ", saxe);
                return false;
            }
        }

        return true;
    }

    //-------------------------------------------------------------------------
    private boolean checkLicenseLimit() {

        if (coreLicense == null) {
            if (! checkLicense()) {
                return false;
            }
        }

        if (coreLicense != null) {
            // could still be null if load failed.
            final boolean result = coreLicense.checkLimits();
            if (! result) {
                licenseErrorMessages = coreLicense.getErrorMessages();
            }
            return result;
        } else {
            return false;
        }
    }

    private String processLicenseErrorMessages() {
        final StringBuffer result = new StringBuffer();
        if (licenseErrorMessages == null) {
            return null;
        }
        for (int i=0; i < licenseErrorMessages.length; i++) {
            final String resource = JahiaResourceBundle.getMessageResource(licenseErrorMessages[i].
                    getResourceKey(), LanguageCodeConverters.languageCodeToLocale(getSettings().
                    getDefaultLanguageCode()));
            if (resource != null) {
                String formatted = resource;
                if (licenseErrorMessages[i].getParameters() != null) {
                    formatted = MessageFormat.format(resource,
                        licenseErrorMessages[i].getParameters());
                }
                result.append(formatted);
                if (i < (licenseErrorMessages.length - 1)) {
                    result.append(",");
                }
            } else {
            logger.error("Could not find resource " + licenseErrorMessages[i].getResourceKey() + " in system resource bundle");
            }
        }
        return result.toString();
    }

    //-------------------------------------------------------------------------
    /**
     * Return the private settings
     *
     * @return JahiaPrivateSettings
     */
    public static SettingsBean getSettings () {
        return jSettings;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the License Key
     *
     */
    public static License getCoreLicense () {
        if (coreLicense == null) {
            return null;
        }
        return coreLicense;
    }

    public static boolean checkCoreLimit(final String limitName) {
        if (coreLicense == null) {
            return false;
        }
        final Limit limit = coreLicense.getLimit(limitName);
        if (limit == null) {
            return true;
        }
        return limit.check();
    }

    public static boolean checkSiteLimit() {
        return checkCoreLimit(LicenseConstants.SITE_LIMIT_NAME);
    }

    public static boolean checkUserLimit() {
        return checkCoreLimit(LicenseConstants.USER_LIMIT_NAME);
    }

    public static boolean checkTemplateLimit() {
        return checkCoreLimit(LicenseConstants.TEMPLATE_LIMIT_NAME);
    }

    public static boolean checkPageLimit() {
        return checkCoreLimit(LicenseConstants.PAGE_LIMIT_NAME);
    }

    public static int getCoreIntLimit(final String limitName) {
        if (coreLicense == null) {
            return 0;
        }
        Limit limit = coreLicense.getLimit(limitName);
        if (limit == null) {
            return -1;
        }
        String valueStr = limit.getValueStr();
        return Integer.parseInt(valueStr);
    }

    public static int getUserLimit() {
        return getCoreIntLimit(LicenseConstants.USER_LIMIT_NAME);
    }

    public static int getSiteLimit() {
        return getCoreIntLimit(LicenseConstants.SITE_LIMIT_NAME);
    }

    public static int getPageLimit() {
        return getCoreIntLimit(LicenseConstants.PAGE_LIMIT_NAME);
    }

    public static int getTemplateLimit() {
        return getCoreIntLimit(LicenseConstants.TEMPLATE_LIMIT_NAME);
    }

    //-------------------------------------------------------------------------
    /**
     * Return true if this class has been fully initiated
     *
     * @return boolean true if this class has been fully initaited
     */
    public static boolean isInitiated () {
        return mInitiated;
    }


    public static boolean isMaintenance() {
        return maintenance;
    }

    public static void setMaintenance(boolean maintenance) {
        Jahia.maintenance = maintenance;
    }

    
    //-------------------------------------------------------------------------
    /**
     * Get the Jahia Lock.
     * Use this to force Jahia to ignore all requests except for those of the current session.
     *
     *
     * @param user      the user must be a root admin
     * @param session   the session
     * @return byte[] lock, the lock or null if the lock is not available.
     */
    public static synchronized byte[] getLock (final JahiaUser user,
                                               final HttpSession session)
        throws JahiaException {

        if (!isInitiated()) {
            return null;
        }

        byte[] lock = null;

        final Map<String, Object> lockParams = JahiaLocksRegistry.getInstance().getLock(
            JAHIA_LOCK_NAME);

        if (!user.isAdminMember(0)) { // a super admin user
            throw new JahiaException(CLASS_NAME + ".getLock",
                                     "No rigth to get the lock on Jahia",
                                     JahiaException.LOCK_ERROR,
                                     JahiaException.ERROR_SEVERITY);
        }

        if (lockParams == null) {

            lock = MakeLock(user, session);

        } else {

            if (JahiaLocksRegistry.getInstance().isLockValid(JAHIA_LOCK_NAME)) {

                // Check if the session is the one that locked Jahia.
                final String sessionID = (String) lockParams.get(
                    JAHIA_LOCK_SESSION_ID);
                if (sessionID.equals(session.getId())) {

                    // reset the timeout time
                    JahiaLocksRegistry.getInstance().resetLockTimeout(
                        JAHIA_LOCK_NAME);
                    lock = (byte[]) lockParams.get(JAHIA_LOCK);

                }

            } else {
                // the lock has been timed out and is available.
                lock = MakeLock(user, session);
            }
        }

        session.setAttribute(JAHIA_LOCK_NAME, lock);

        return lock;

    }

    //-------------------------------------------------------------------------
    private static byte[] MakeLock (final JahiaUser user, final HttpSession session) {
        final Map<String, Object> lockParams = new HashMap<String, Object>();
        final byte[] lock = new byte[1];
        lockParams.put(JAHIA_LOCK_USER, user);
        lockParams.put(JAHIA_LOCK_SESSION_ID, session.getId());
        lockParams.put(JAHIA_LOCK, lock);

        // create the lock in the registry.
        final int timeout = session.getMaxInactiveInterval();
        JahiaLocksRegistry.getInstance().setLock(JAHIA_LOCK_NAME, lockParams,
                                                 timeout);
        return lock;

    }

    //-------------------------------------------------------------------------
    /**
         * To free the lock, you must give back the lock object stored in your session.
     * The JAHIA_LOCK attribute in session is set to null
     *
     * @param lock  the original lock
     */
    public static synchronized boolean releaseLock (final byte[] lock) {

        if (!isInitiated()) {
            return false;
        }

        if (lock == null) {
            return false;
        }

        final Map<String, Object> lockParams = JahiaLocksRegistry.getInstance().getLock(
            JAHIA_LOCK_NAME);

        final byte[] storedLock = (byte[]) lockParams.get(JAHIA_LOCK);

        if (lock == storedLock) {

            JahiaLocksRegistry.getInstance().removeLock(JAHIA_LOCK_NAME);

            return true;
        }

        return false;

    }

    //-------------------------------------------------------------------------
    /**
     * Check if Jahia is authorized to process request from current session
     *
     * @return boolean false if no access allowed
     */
    public static synchronized boolean checkLockAccess (final HttpSession session) {

        if (!isInitiated()) {
            //logger.debug("Jahia is not initialized");
            return false;
        }

        //logger.debug("Jahia is initialized");

        final Map<String, Object> lockParams = JahiaLocksRegistry.getInstance().getLock(
            JAHIA_LOCK_NAME);

        if (lockParams == null) {

            //logger.debug("lock params in session is null");
            return true; // Jahia is not locked

        } else {

            if (JahiaLocksRegistry.getInstance().isLockValid(JAHIA_LOCK_NAME)) {

                //logger.debug(JAHIA_LOCK_NAME + " lock is valid");

                // Check if the session is the one that locked Jahia.
                final String sessionID = (String) lockParams.get(
                    JAHIA_LOCK_SESSION_ID);
                if (sessionID.equals(session.getId())) {
                    // reset the timeout time
                    JahiaLocksRegistry.getInstance().resetLockTimeout(
                        JAHIA_LOCK_NAME);
                    return true;
                }

            } else {

                //logger.debug(JAHIA_LOCK_NAME + " lock no more valid");

                return true; // no more lock
            }
        }

        return false;
    }

    /**
     * Check if the current JDK we are running Jahia on is supported. The
     * supported JDK string is a specially encoded String that checks only
     * the versions.
     *
     * The accepted format is the following :
     *      version <= x <= version
     * or
     *      version < x < version
     * The "x" character is mandatory !
     *
     * @param currentJDKVersion the current JDK version we are using, this is
     * a valid version object.
     * @param supportedJDKString
     */
    private boolean isSupportedJDKVersion (final Version currentJDKVersion,
                                           final String supportedJDKString) {
        if (supportedJDKString == null) {
            // we deactivate the check if we specify no supported JDKs
            return true;
        }

        final String workString = supportedJDKString.toLowerCase();
        int xPos = workString.indexOf("x");

        if (xPos == -1) {
            logger.debug("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                         " following format : 1.2 < x <= 1.3 (the 'x' " +
                         "character is mandatory and was missing in " +
                         "this case : [" + supportedJDKString + "] )");
            return false;
        }
        final String leftArg = workString.substring(0, xPos).trim();
        final String rightArg = workString.substring(xPos + 1).trim();

        if (leftArg.endsWith("<=")) {
            final String leftVersionStr = leftArg.substring(0, leftArg.length() - 2).
                                    trim();
            Version lowerVersion;
            try {
                lowerVersion = new Version(leftVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in lower version number conversion", nfe);
                return false;
            }
            if (lowerVersion.compareTo(currentJDKVersion) > 0) {
                return false;
            }
        } else if (leftArg.endsWith("<")) {
            final String leftVersionStr = leftArg.substring(0, leftArg.length() - 1).
                                    trim();
            Version lowerVersion;
            try {
                lowerVersion = new Version(leftVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in lower number conversion", nfe);
                return false;
            }
            if (lowerVersion.compareTo(currentJDKVersion) >= 0) {
                return false;
            }
        } else {
            logger.error("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                " following format : 1.2 < x <= 1.3. Current string : [" +
                supportedJDKString + "] )");
            return false;
        }

        if (rightArg.startsWith("<=")) {
            final String rightVersionStr = rightArg.substring(2).trim();
            Version upperVersion;
            try {
                upperVersion = new Version(rightVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in upper number conversion", nfe);
                return false;
            }
            if (upperVersion.compareTo(currentJDKVersion) < 0) {
                return false;
            }
        } else if (rightArg.startsWith("<")) {
            final String rightVersionStr = rightArg.substring(1).trim();
            Version upperVersion;
            try {
                upperVersion = new Version(rightVersionStr);
            } catch (NumberFormatException nfe) {
                logger.error("Error in upper number conversion", nfe);
                return false;
            }
            if (upperVersion.compareTo(currentJDKVersion) <= 0) {
                return false;
            }
        } else {
            logger.error("Invalid supported_jdk_versions initialization " +
                         " parameter in web.xml, it MUST be in the " +
                " following format : 1.2 < x <= 1.3. Current string : [" +
                supportedJDKString + "] )");
            return false;
        }

        return true;
    }

    /**
     *
     * @param request
     * @param response
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void process (final HttpServletRequest request,
                         final HttpServletResponse response)
        throws IOException, ServletException {
        try {
            final ParamBean jParams =
                (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
            // Create a JahiaData without loaded containers and fields
            final JahiaData jData = new JahiaData(jParams, false);
            jParams.getRequest().setAttribute("org.jahia.data.JahiaData", jData);
            super.process(jParams.getRequest(), jParams.getResponse());
        } catch (JahiaException je) {
            logger.error(je.getMessage(), je);
        }

    }

    static public Pipeline getAuthPipeline() {
        return authPipeline;
    }

    // BEGIN [added by Pascal Aubry for CAS authentication]
    /**
     * Tell if Jahia is using Single Sign-On for authentication.
     * @return true if SSO should be used, false otherwise.
     */
    static public boolean usesSso() {
        return authPipeline.hasValveOfClass(SsoValve.class);
    }
    /**
     * Return the (first) SSO valve.
     * @return a SsoValve instance, or null if none.
     */
    static public SsoValve getSsoValve() {
        return (SsoValve) authPipeline.getFirstValveOfClass(SsoValve.class);
    }
    // END [added by Pascal Aubry for CAS authentication]

    static public Pipeline getProcessPipeline() {
        return processPipeline;
    }

    static public ServletConfig getStaticServletConfig() {
        return staticServletConfig;
    }

    static String getDefaultServletPath(ServletContext ctx) {
        String path = ctx.getInitParameter(CTX_PARAM_DEFAULT_SERVLET_PATH);
        if (null == path) {
            // should we alternatively default it to '/Jahia'?
            throw new RuntimeException("Missing required context-param '"
                    + CTX_PARAM_DEFAULT_SERVLET_PATH + "'"
                    + " in the web.xml. Initialization failed.");
        }
        return path;
    }
    
    public static void initContextData(ServletContext servletContext) {
        String ctxPath = servletContext.getInitParameter(CTX_PARAM_CONTEXT_PATH);
        Jahia.jahiaContextPath = ctxPath.equals("/") ? "" : ctxPath;
        Jahia.jahiaServletPath = getDefaultServletPath(servletContext);
    }

} // end Jahia
