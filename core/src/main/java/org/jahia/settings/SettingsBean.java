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
//  JahiaPrivateSettings
//
//  18.11.2000  EV  added in jahia.
//  22.01.2001  FH  created readJahiaPropertiesFile() method and changed.
//  06.02.2001  AK  set readJahiaPropertiesFile as static method.
//  27.03.2001  AK  use the properties manager from org.jahia.utils.properties.
//  27.07.2001  SB  added jahiaLdapDiskPath
//  15.01.2002  NK  added mime types. mime types are loaded from web.xml files.
//  24.08.2003  FH  - removed redundant casts
//                  - removed private attribute privateSettings, as it was never used.
//                  - javadoc fixes
//
// @author  Eric Vassalli
// @author  Fulco Houkes
// @author  Alexandre Kraft

package org.jahia.settings;

import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.jahia.admin.database.DatabaseScripts;
import org.jahia.data.constants.JahiaConstants;
import org.jahia.tools.files.MimeTypesFromWebAppXmlFile;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.PathResolver;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.utils.xml.DtdEntityResolver;
import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SettingsBean {

    public boolean isConsiderPreferredLanguageAfterLogin() {
        return considerPreferredLanguageAfterLogin;
    }

    public void setConsiderPreferredLanguageAfterLogin(
            boolean considerPreferredLanguageAfterLogin) {
        this.considerPreferredLanguageAfterLogin = considerPreferredLanguageAfterLogin;
    }

    private static final transient Logger logger =
            Logger.getLogger (SettingsBean.class);
    
    /** The map holding all the settings. */
    final private FastHashMap settings = new FastHashMap ();

    private PathResolver pathResolver = null;
    private String licenseFilename;
    private String propertiesFileName;
    // this is the famous build number...
    public int buildNumber;

    private Properties properties;


    // this is the list of jahia.properties server disk path and context path values...
    private String server;
    private String serverHomeDiskPath;
    private String jahiaHomeDiskPath;
    private String jahiaTemplatesDiskPath;
    private String jahiaHtmlEditorsDiskPath;
    private String jahiaWebAppsDiskPath;
    private String jahiaEnginesDiskPath;
    private String jahiaJspDiskPath;
    private String jahiaFilesDiskPath;
    private String jahiaEtcDiskPath;
    private String jahiaVarDiskPath;
    private String jahiaFilesBigTextDiskPath;
    private String jahiaFilesTemplatesDiskPath;
    private String jahiaCasDiskPath;
    private String jahiaHostHttpPath;
    private String jahiaTemplatesHttpPath;
    private String jahiaEnginesHttpPath;
    private String jahiaWebAppsDeployerBaseURL;
    private String jahiaJavaScriptDiskPath;
    private String jahiaNewTemplatesDiskPath;
    private String jahiaNewWebAppsDiskPath;
    private String jahiaImportsDiskPath;
    private String jahiaSharedTemplatesDiskPath;
    private String jahiaDatabaseScriptsPath;

    public String getJahiaDatabaseScriptsPath() {
        return jahiaDatabaseScriptsPath;
    }

    private String jspContext;
    private String templatesContext;
    private String htmlEditorsContext;
    private String enginesContext;
    private String javascriptContext;

    private String jahiaJavaScriptHttpPath;
    private String classDiskPath;
    private String componentsDiskPath;
    private String localAccessUri;

    // directory to output cache configuration file
    private String jahiaOutputCacheConfigDiskPath;

    // map containing all max_cached_*...
    private Map<String, Long> maxCachedValues;
    // map containing all max_cachedgroups_*...
    private Map<String, Long> maxCachedGroupsValues;


    // this is the list of jahia.properties autodeployer values...
    private int jahiaWebAppsAutoDeploy;

    // this is the list of jahia.properties files values...
    private String jahiaFileRepositoryDiskPath;
    private long jahiaFileUploadMaxSize;

    // Characters encoding
    private boolean utf8Encoding;

    // Lock activation
    private boolean locksActivated;

    // flat view activation
    private boolean enableSelectPageFlatView;

    private boolean outputContainerCacheActivated = false;

    // Activation / deactivation of site ID in URLs
    private boolean siteIDInURL;

    // Activation / deactivation of site/page-ID match check
    private boolean performSiteAndPageIDMatchCheck;

    // Activation / deactivation of site ID in Search Hit page URLs
    private boolean siteIDInSearchHitPageURL;

    // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    private boolean useRelativeSiteURLs;

    // Flag that indicates whether applications can inherit from Jahia session
    // attributes.
    private boolean appInheritJahiaSessionAttributes;

    // Default language code for multi-language system
    private String defaultLanguageCode;

    // Default site
    private String defaultSite;

    private boolean aclPreloadActive = true;

    // should the webdav filenames be converted into full-ascii
    private boolean transformFilenames;

    private boolean transformDirnames;

    private boolean preloadFolders;


    // the (optional) url the user will be redirected after logout
    public String logoutRedirectUrl;
    // The (optional) URL the user will be forwarded to after logout
    public String logoutForwardUrl;
    // Generally do a client side redirect instead of forward after logout 
    public boolean doRedirectOnLogout = true;    

    // this is the list of jahia.properties mail settings values...
    public boolean mail_service_activated;
    public String mail_server;
    public String mail_administrator;
    public String mail_from;
    public String mail_paranoia;
    public int mail_maxRegroupingOfPreviousException = 500;

    private DtdEntityResolver mResolver;
    protected Properties mimeTypes;

    private boolean jmxHTTPAdaptorActivated = false;
    private boolean jmxXSLProcessorActivated = false;
    private boolean jmxRMIAdaptorActivated = false;

    private String jmxHTTPHostname = null;
    private int jmxHTTPPort = 8082;
    private String jmxHTTPAutorizationMode = null;
    private String jmxHTTPAuthorizationUser = null;
    private String jmxHTTPAuthorizationPassword = null;
    private String jmxHTTPProcessorNameString = null;
    private String jmxHTTPSocketFactoryNameString = null;
    private boolean jmxRMISSLServerSocketFactoryActivated = false;
    private String jmxRMISSLServerSocketFactoryKeyStoreName = null;
    private String jmxRMISSLServerSocketFactoryKeyStorePassword = null;
    private String jmxRMISSLServerSocketFactoryKeyManagerPassword = null;
    private String defaultResponseBodyEncoding;
    private String defaultURIEncoding;
    private int preloadCountForPageProperties;
    private boolean jmxActivated;

    private int cookieAuthIDLength;
    private String cookieAuthUserPropertyName;
    private String cookieAuthCookieName;
    private int cookieAuthMaxAgeInSeconds;
    private boolean cookieAuthRenewalActivated;
    private boolean cookieAuthActivated;

    private String authPipelineFileName;
    private String authPipelineClassName;

    private String processPipelineClassName;
    private String processPipelineFileName;

    private String tmpContentDiskPath;
    private long templatesObserverInterval;
    private long webAppsObserverInterval;
    private String schedulerConfigFile;
    private String ramSchedulerConfigFile;

    private boolean isProcessingServer;

    private String siteServerNameTestURLExpr;
    private int siteServerNameTestConnectTimeout;
    private int siteURLPortOverride = -1;

    private boolean isSiteErrorEnabled;

    private String jetspeedDeploymentDirectory;

    private int workflowMaxNotificationEmails = 20;
    private boolean workflowNotifyAdminOnMaxNotificationReached = true;
    private String workflowMaxNotificationReachedTemplate;

    private String freeMemoryLimit= new Double((Runtime.getRuntime().maxMemory()*0.2)/ (1024*1024)).longValue() + "MB";

    private int clusterCacheMaxBatchSize = 100000;
    private String cacheClusterUnderlyingImplementation = "jahiaReferenceCache";

    private int cacheMaxGroups = 10000;

    private int maxAggregatedEvents = 5000;

    private boolean useFlatFileManager;
    private boolean showTimeBasedPublishingIcons;
    private boolean developmentMode = true;
    private boolean readOnlyMode = false;
    private boolean waiAutoAddMissingAttributes;
    private int connectionTimeoutForProductionJob;

    //flags for aes
    private boolean tbpDisp;
    private boolean wflowDisp;
    private boolean chatDisp;
    private boolean aclDisp;
    private boolean pdispDisp=true;

    // Title size display
    private int siteMapTitlesLength;
    private int workflowTitlesLength;

    // hibernate batchloading
    private boolean batchLoadingEnabled = true;
    private int batchLoadingSize = 20;

    // pagination settings
    private int preloadedItemsForPagination = 100;
    private int paginationWindowSize;

    // Core engine page generation queue configuration parameters
    private int maxParallelProcessings = 40;
    private long pageGenerationWaitTime = 30000; // in milliseconds
    private long pageGenerationWaitTimeOnStartup = 10000; // in milliseconds
    private int suggestedRetryTimeAfterTimeout = 60; // in seconds
    private int suggestedRetryTimeAfterTimeoutOnStartup = 15; // in seconds

    // Preload group members when loading user groups from DB
    final public static String PRELOAD_DBGROUP_MEMBERS_ACTIVATED = "preloadDBGroupMembersActivated";

    private int editModeSessionTimeout = 2*60*60; // 2 hours

    // The db max elements for SQL IN clause
    private int dBMaxElementsForInClause = 1000;

    private boolean workflowDisplayStatusForLinkedPages;

    private boolean workflowUseExternalByDefault;

    // pdisplay
    private String pdisplayTimerRefresh;

    private int nbMaxContentAliasName = 10;

    private boolean displayMarkedForDeletedContentObjects;

    private boolean deprecatedNonContainerFieldsUsed = false;

    // Settings to control servlet response wrapper flushing
    private boolean wrapperBufferFlushingActivated = true;
    private boolean wrapperBufferFlushingAlsoForPortlets = true;


    // ESI, default expiration age in seconds
    private long containerCacheDefaultExpirationDelay;

    private boolean containerCacheLiveModeOnly = false;

    private static SettingsBean instance = null;
    private boolean preloadDBGroupMembersActivated;

    private boolean inlineEditingActivated = false;

    private boolean portletAJAXRenderingActivated = false;
    
    private boolean gmailPasswordExported = true;

    private boolean considerPreferredLanguageAfterLogin;

    // enable ACL check when displaying the current page path
    private boolean checkAclInPagePath ;

    private String ehCacheJahiaFile;
    /**
     * Default constructor.
     *
     * @param   pathResolver a path resolver used to locate files on the disk.
     * @param   propertiesFilename  The jahia.properties file complete path.
     * @param   licenseFilename the name of the license file.
     * @param   buildNumber         The Jahia build number.
     */
    public SettingsBean (PathResolver pathResolver,
                         String propertiesFilename,
                         String licenseFilename,
                         int buildNumber) {
        //this.config = config;
        //this.context = config.getServletContext ();
        this.pathResolver = pathResolver;
        this.propertiesFileName = propertiesFilename;
        this.buildNumber = buildNumber;
        this.licenseFilename = licenseFilename;
        instance = this;

    } // end constructor

    public SettingsBean(PathResolver pathResolver,
                        Resource propertiesFile,
                        Resource licenseFile) throws IOException {
        this.pathResolver = pathResolver;
        this.propertiesFileName = propertiesFile.getFile().toString();
        this.licenseFilename = licenseFile.getFile().toString();
        instance = this;
    }

    public static SettingsBean getInstance() {
        return instance;
    }
    /**
     * Read the jahia.properties file.
     *
     * @return  On success return the Jahia properties, or null on any failure.
     */
    public Properties readJahiaPropertiesFile () {
        PropertiesManager propertiesManager = new PropertiesManager (propertiesFileName);
        return propertiesManager.getPropertiesObject ();
    } // end readJahiaPropertiesFile

    public boolean isEnableSelectPageFlatView() {
        return enableSelectPageFlatView;
    }

    /**
     * This method load and convert properties from the jahia.properties file,
     * and set some variables used by the SettingsBean class.
     */
    public void load () {
        PropertiesManager propertiesManager = new PropertiesManager (propertiesFileName);
        properties = propertiesManager.getPropertiesObject ();

        // try to get values from the properties object...
        try {
            // disk path, url's and context...
            server = getString("server");
            serverHomeDiskPath = getString("serverHomeDiskPath");
            jahiaTemplatesDiskPath = pathResolver.resolvePath (getString("jahiaTemplatesDiskPath"));
            jahiaHtmlEditorsDiskPath = pathResolver.resolvePath (getString("jahiaHtmlEditorsDiskPath"));
            jahiaJspDiskPath = pathResolver.resolvePath (getString("jahiaJspDiskPath"));
            jahiaEnginesDiskPath = pathResolver.resolvePath (getString("jahiaEnginesDiskPath"));
            jahiaJavaScriptDiskPath = pathResolver.resolvePath (getString("jahiaJavaScriptDiskPath"));
            classDiskPath = pathResolver.resolvePath (getString("classDiskPath"));
            componentsDiskPath = pathResolver.resolvePath (getString("componentsDiskPath"));
            jahiaFilesDiskPath = JahiaTools.convertContexted (getString("jahiaFilesDiskPath"), pathResolver);
            jahiaEtcDiskPath = JahiaTools.convertContexted (getString("jahiaEtcDiskPath"), pathResolver);
            jahiaVarDiskPath = JahiaTools.convertContexted (getString("jahiaVarDiskPath"), pathResolver);
            jahiaFilesBigTextDiskPath = JahiaTools.convertContexted (getString("jahiaFilesBigTextDiskPath"), pathResolver);
            tmpContentDiskPath = JahiaTools.convertContexted (getString("tmpContentDiskPath"), pathResolver);
            jahiaFilesTemplatesDiskPath = JahiaTools.convertContexted (getString("jahiaFilesTemplatesDiskPath"), pathResolver);
            jahiaCasDiskPath = JahiaTools.convertContexted (getString("jahiaCasDiskPath"), pathResolver);
            jahiaNewTemplatesDiskPath = JahiaTools.convertContexted (getString("jahiaNewTemplatesDiskPath"), pathResolver);
            jahiaNewWebAppsDiskPath = JahiaTools.convertContexted (getString("jahiaNewWebAppsDiskPath"), pathResolver);
            jahiaImportsDiskPath = JahiaTools.convertContexted (getString("jahiaImportsDiskPath"), pathResolver);
            jahiaSharedTemplatesDiskPath = JahiaTools.convertContexted (getString("jahiaSharedTemplatesDiskPath"), pathResolver);
            jahiaOutputCacheConfigDiskPath = JahiaTools.convertContexted (getString("jahiaOutputCacheConfigDiskPath"), pathResolver);
            jahiaDatabaseScriptsPath = jahiaVarDiskPath + File.separator + "db";

            jahiaHostHttpPath = getString("jahiaHostHttpPath");
            jahiaTemplatesHttpPath = JahiaTools.convertWebContexted(getString("jahiaTemplatesHttpPath"));
            jahiaEnginesHttpPath = JahiaTools.convertWebContexted(getString("jahiaEnginesHttpPath"));
            jahiaJavaScriptHttpPath = JahiaTools.convertWebContexted(getString("jahiaJavaScriptHttpPath"));
            jspContext = getString("jahiaJspDiskPath");
            templatesContext = getString("jahiaTemplatesDiskPath");
            htmlEditorsContext = getString("jahiaHtmlEditorsDiskPath");
            enginesContext = getString("jahiaEnginesDiskPath");
            javascriptContext = getString("jahiaJavaScriptDiskPath");
            enableSelectPageFlatView = getBoolean("enableSelectPageFlatView", true);
            displayMarkedForDeletedContentObjects = getBoolean("displayMarkedForDeletedContentObjects", false);

            // jahia real path...
            File jahiaContextFolder = new File (pathResolver.resolvePath("." + File.separator));
            File parent = jahiaContextFolder.getAbsoluteFile().getParentFile ();

            if (server.indexOf (JahiaConstants.SERVER_TOMCAT4_BETA1) != -1) {         // the server is tomcatb1...
                jahiaHomeDiskPath = jahiaContextFolder.getAbsolutePath ();
                jahiaWebAppsDiskPath = parent.getAbsolutePath () + File.separator;
            } else if (server.indexOf (JahiaConstants.SERVER_TOMCAT) != -1) {      // the server is tomcat
                jahiaHomeDiskPath = parent.getAbsolutePath ();
                jahiaWebAppsDiskPath = parent.getParentFile ().getAbsolutePath () + File.separator;
                /*
                } else if ( (server.indexOf(JahiaConstants.SERVER_TOMCAT4_BETA2) != -1)
                        || (server.indexOf(JahiaConstants.SERVER_TOMCAT4_BETA3) != -1)
                        || (server.indexOf(JahiaConstants.SERVER_TOMCAT4_BETA6) != -1) ) {             // the server is tomcat...
                    jahiaHomeDiskPath	 = parent.getAbsolutePath();
                    jahiaWebAppsDiskPath =  parent.getParentFile().getAbsolutePath() + File.separator;
                */
            } else {
                jahiaHomeDiskPath = jahiaContextFolder.getAbsolutePath ();
                jahiaWebAppsDiskPath = jahiaContextFolder.getAbsolutePath ();
            }

            // autodeployer...
            String webappAutoDeploy = getString("jahiaWebAppsAutoDeploy");
            if (webappAutoDeploy != null) {
                jahiaWebAppsAutoDeploy = Integer.parseInt (webappAutoDeploy);
            }
            templatesObserverInterval = getLong("templates.observer.interval", 5000);
            webAppsObserverInterval = getLong("webapps.observer.interval", 5000);

            // files...
            jahiaFileRepositoryDiskPath = JahiaTools.convertContexted (getString("jahiaFileRepositoryDiskPath"), pathResolver);
            jahiaFileUploadMaxSize = Long.parseLong (getString("jahiaFileUploadMaxSize"));

            // chars encoding
            utf8Encoding = getBoolean("utf8Encoding");
            defaultResponseBodyEncoding = getString("defaultResponseBodyEncoding", "ISO-8859-1");
            defaultURIEncoding = getString("defaultURIEncoding", "UTF-8");

            // Lock activation
            locksActivated = getBoolean ("locksActivated", true);

            outputContainerCacheActivated = getBoolean("outputContainerCacheActivated", false);

            // activation / deactivation of site ID in URL
            siteIDInURL = getBoolean ("siteIDInURL", true);

            // activation / deactivation of site/page ID match check
            performSiteAndPageIDMatchCheck = getBoolean ("performSiteAndPageIDMatchCheck", false);

            // activation / deactivation of site ID in Search Hit Page URL
            siteIDInSearchHitPageURL = getBoolean ("siteIDInSearchHitPageURL", false);

            // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
            useRelativeSiteURLs = getBoolean ("useRelativeSiteURLs", false);

            // Flag that indicates whether applications can inherit from Jahia session
            // attributes.
            appInheritJahiaSessionAttributes =getBoolean ("webapps.dispatcher.inheritJahiaSessionAttributes", false);

            // base URL (schema, host, port) to call the web apps deployer service.
            jahiaWebAppsDeployerBaseURL = getString ("jahiaWebAppsDeployerBaseURL", "http://127.0.0.1:8080/manager");

            // multi language default language code property.
            defaultLanguageCode = getString ("org.jahia.multilang.default_language_code", "en");

            considerPreferredLanguageAfterLogin = getBoolean("considerPreferredLanguageAfterLogin", false);

            aclPreloadActive = getBoolean("org.jahia.acl.preload_active", true);

            preloadCountForPageProperties = getInt("org.jahia.pages.properties.preload_count", 2000);

            // mail settings...
            mail_service_activated = getBoolean("mail_service_activated", false);
            mail_server = getString("mail_server");
            mail_administrator = getString("mail_administrator");
            mail_from = getString("mail_from");
            mail_maxRegroupingOfPreviousException = getInt("mail_maxRegroupingOfPreviousException", 500);

            // paranoia settings...
            mail_paranoia = getString("mail_paranoia", "Disabled");

            jmxActivated = getBoolean("org.jahia.jmx.activated", false);
            jmxHTTPAdaptorActivated = getBoolean("org.jahia.jmx.httpAdaptorActivated", false);
            jmxXSLProcessorActivated = getBoolean("org.jahia.jmx.xslProcessorActivated", false);
            jmxRMIAdaptorActivated = getBoolean("org.jahia.jmx.rmiAdaptorActivated", false);
            jmxHTTPHostname = getString("org.jahia.jmx.httpHostName", "localhost");
            jmxHTTPPort = getInt("org.jahia.jmx.httpPort", 8082);
            jmxHTTPAutorizationMode = getString("org.jahia.jmx.httpAuthorizationMode", null);
            jmxHTTPAuthorizationUser = getString("org.jahia.jmx.httpAuthorizationUser", null);
            jmxHTTPAuthorizationPassword = getString("org.jahia.jmx.httpAuthorizationPassword", null);
            jmxHTTPProcessorNameString = getString("org.jahia.jmx.httpProcessorNameString", null);
            jmxHTTPSocketFactoryNameString = getString("org.jahia.jmx.httpSocketFactoryNameString", null);
            jmxRMISSLServerSocketFactoryActivated = getBoolean("org.jahia.jmx.rmiSSLServerSocketFactoryActivated", false);
            jmxRMISSLServerSocketFactoryKeyStoreName = getString("org.jahia.jmx.rmiSSLServerSocketFactoryKeyStoreName", null);
            jmxRMISSLServerSocketFactoryKeyStorePassword = getString("org.jahia.jmx.rmiSSLServerSocketFactoryKeyStorePassword", null);
            jmxRMISSLServerSocketFactoryKeyManagerPassword = getString("org.jahia.jmx.rmiSSLServerSocketFactoryKeyManagerPassword", null);

            // load mime types
            initDtdEntityResolver ();
            loadMimeTypes ();

            defaultSite = getString("defautSite");

            // load MaxCached values (max_cached_*)
            maxCachedValues = new HashMap<String, Long>();
            for (Enumeration<?> e = properties.propertyNames (); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                String lowerKey = key.toLowerCase ().trim ();
                // yes this is a max_cached value
                if (lowerKey.indexOf ("max_cached_") == 0) {
                    String cacheKey = key.trim().substring ("max_cached_".length());
                    String value = properties.getProperty (key);
                    if ((cacheKey != null) && (value != null)) {
                        Long maxSize = null;
                        try {
                            maxSize = Long.valueOf(value);
                        } catch (NumberFormatException nfe) {
                            logger.error("Error while parsing value for cache size " + cacheKey + ", ignoring...");
                            maxSize = null;
                        }
                        if (maxSize != null) {
                            maxCachedValues.put (cacheKey, maxSize);
                        }
                    } else {
                        logger.debug ("Ignoring cache key : " + key + " because value or key is invalid (value=" + value + ")");
                    }
                }
            }

            // load MaxGroups values (max_cachedgroups_*)
            maxCachedGroupsValues = new HashMap<String, Long>();
            for (Enumeration<?> e = properties.propertyNames (); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                String lowerKey = key.toLowerCase ().trim ();
                // yes this is a max_cached value
                if (lowerKey.indexOf ("max_cachedgroups_") == 0) {
                    String cacheKey = key.trim().substring ("max_cachedgroups_".length());
                    String value = properties.getProperty (key);
                    if ((cacheKey != null) && (value != null)) {
                        Long maxGroupSize = null;
                        try {
                            maxGroupSize = Long.valueOf(value);
                        } catch (NumberFormatException nfe) {
                            logger.error("Error while parsing value for cache group size " + cacheKey + ", ignoring...");
                            maxGroupSize = null;
                        }
                        if (maxGroupSize != null) {
                            maxCachedGroupsValues.put (cacheKey, maxGroupSize);
                        }
                    } else {
                        logger.debug ("Ignoring cache groups key : " + key + " because value or key is invalid (value=" + value + ")");
                    }
                }
            }

            // webdav settings
            transformFilenames = getBoolean("transformFilenames", false);
            transformDirnames  = getBoolean("transformDirnames", true);
            preloadFolders  = getBoolean("preloadFolders", false);

            cookieAuthActivated = getBoolean("cookieAuthActivated", true);
            cookieAuthIDLength = getInt("cookieAuthIDLength", 30);
            cookieAuthUserPropertyName = getString("cookieAuthUserPropertyName", "org.jahia.user.cookieauth.id");
            cookieAuthCookieName = getString("cookieAuthCookieName", "jid");
            cookieAuthMaxAgeInSeconds = getInt("cookieAuthMaxAgeInSeconds", 60*60*24*30 /* 30 days expiration */);
            cookieAuthRenewalActivated = getBoolean("cookieAuthRenewalActivated", true);

            authPipelineFileName = getString("authPipelineFileName", "/WEB-INF/etc/config/auth-pipeline.xml");
            authPipelineClassName = getString("authPipelineClassName", "org.jahia.pipelines.impl.GenericPipeline");

            processPipelineFileName = getString("processPipelineFileName", "/WEB-INF/etc/config/process-pipeline.xml");
            processPipelineClassName = getString("processPipelineClassName", "org.jahia.pipelines.impl.GenericPipeline");
            schedulerConfigFile = JahiaTools.convertContexted (getString("schedulerConfigFile", "$context/WEB-INF/etc/config/quartz.properties"), pathResolver);
            ramSchedulerConfigFile = JahiaTools.convertContexted (getString("ramSchedulerConfigFile", "$context/WEB-INF/etc/config/quartz-ram.properties"), pathResolver);
            isProcessingServer = getBoolean("processingServer", true);

            siteServerNameTestURLExpr = getString("siteServerNameTestURLExpr", "${request.scheme}://${siteServerName}:${request.serverPort}${request.contextPath}/isjahia.jsp");
            siteServerNameTestConnectTimeout = getInt("siteServerNameTestConnectTimeout", 500);

            siteURLPortOverride = getInt("siteURLPortOverride", 0);

            workflowMaxNotificationEmails = getInt("workflowMaxNotificationEmails", 20);
            workflowNotifyAdminOnMaxNotificationReached = getBoolean("workflowNotifyAdminOnMaxNotificationReached", true);
            workflowMaxNotificationReachedTemplate = JahiaTools.convertContexted (getString("workflowMaxNotificationReachedTemplate", "workflow_maxnotif.groovy"), pathResolver);
            workflowDisplayStatusForLinkedPages = getBoolean("workflowDisplayStatusForLinkedPages", true);
            workflowUseExternalByDefault = getBoolean("workflowUseExternalByDefault", true);

            jetspeedDeploymentDirectory = JahiaTools.convertContexted (getString("jetspeedDeploymentDirectory", "$context/WEB-INF/deploy"), pathResolver);

            // the (optional) url the user will be redirected after logout
            logoutRedirectUrl = getString("logoutRedirectUrl", null);
            // the (optional) url the user will be forwarded to after logout
            logoutForwardUrl = getString("logoutForwardUrl", null);
            doRedirectOnLogout = getBoolean("doRedirectOnLogout", true);
            
            isSiteErrorEnabled = getBoolean("site.error.enabled",false);
            String tmp = getString("freeMemoryLimit",new Double((Runtime.getRuntime().maxMemory()*0.2)/ (1024*1024)).longValue() + "MB");
            if(tmp.indexOf("MB")>=0) {
                freeMemoryLimit = tmp;
            }
            // configures underlying cache implementation used in jahia's cluster cache
            cacheClusterUnderlyingImplementation = getString("cacheClusterUnderlyingImplementation", "jahiaReferenceCache");

            clusterCacheMaxBatchSize = getInt("clusterCacheMaxBatchSize", 100000);
            cacheMaxGroups = getInt("cacheMaxGroups", 10000);

            maxAggregatedEvents = getInt("maxAggregatedEvents", 5000);

            useFlatFileManager = getBoolean("useFlatFileManager", true);
            waiAutoAddMissingAttributes = getBoolean("waiAutoAddMissingAttributes", true);
            showTimeBasedPublishingIcons = getBoolean("showTimeBasedPublishingIcons", true);
            localAccessUri = getString("localAccessUri", "http://localhost:8080");
            developmentMode = getBoolean("developmentMode",true);
            readOnlyMode = getBoolean("readOnlyMode",false);
            tbpDisp = getBoolean("timebased_display",false);
            aclDisp = getBoolean("aclDiff_display",false);
            wflowDisp =getBoolean("workflow_display",false);
            chatDisp =getBoolean("chat_display",false);
            pdispDisp =getBoolean("process_display",true);
            connectionTimeoutForProductionJob = getInt("connectionTimeoutForProductionJob",60000);
            siteMapTitlesLength = getInt("siteMapTitlesLength", 25);
            workflowTitlesLength = getInt("workflowTitlesLength", 25);

            // hibernate batchloading
            batchLoadingEnabled = getBoolean("batchLoadingEnabled",true);
            batchLoadingSize = getInt("batchLoadingSize",20);

            preloadedItemsForPagination = getInt("preloadedItemsForPagination",preloadedItemsForPagination);
            paginationWindowSize = getInt("paginationWindowSize", 20);

            // Maximum parallel heavy processing threads
            maxParallelProcessings = getInt("maxParallelProcessings", maxParallelProcessings);
            pageGenerationWaitTime = getLong("pageGenerationWaitTime", pageGenerationWaitTime);
            suggestedRetryTimeAfterTimeout = getInt("suggestedRetryTimeAfterTimeout", suggestedRetryTimeAfterTimeout);
            pageGenerationWaitTimeOnStartup = getLong("pageGenerationWaitTimeOnStartup", pageGenerationWaitTimeOnStartup);
            suggestedRetryTimeAfterTimeoutOnStartup = getInt("suggestedRetryTimeAfterTimeoutOnStartup", suggestedRetryTimeAfterTimeoutOnStartup);

            editModeSessionTimeout = getInt("editModeSessionTimeout", 2*60*60);

            dBMaxElementsForInClause = getInt("db_max_elements_for_in_clause", dBMaxElementsForInClause);

            pdisplayTimerRefresh = getString("pdisplay.timer.refresh","5000");

            nbMaxContentAliasName = getInt("nbMaxContentAliasName",nbMaxContentAliasName);

            wrapperBufferFlushingActivated = getBoolean("wrapperBufferFlushingActivated", true);
            wrapperBufferFlushingAlsoForPortlets = getBoolean("wrapperBufferFlushingAlsoForPortlets", true);

            containerCacheDefaultExpirationDelay = getLong("containerCacheDefaultExpirationDelay",3600*4); //4 hours

            containerCacheLiveModeOnly = getBoolean("containerCacheLiveModeOnly", false);

            inlineEditingActivated = getBoolean("inlineEditingActivated", false);
            portletAJAXRenderingActivated = getBoolean("portletAJAXRenderingActivated", false);

            preloadDBGroupMembersActivated = getBoolean(PRELOAD_DBGROUP_MEMBERS_ACTIVATED,true);

            gmailPasswordExported = getBoolean("gmailPasswordExported", true);

            checkAclInPagePath = getBoolean("checkAclInPagePath", true) ;

            try {
                DatabaseScripts scriptsManager = new DatabaseScripts();
                List<Map<String, String>> scriptsInfos = scriptsManager.getDatabaseScriptsInfos (
                        scriptsManager.getDatabaseScriptsFileObjects (getJahiaDatabaseScriptsPath()), pathResolver);
                for (Map<String, String> curDatabaseHash : scriptsInfos) {
                    String database_script = curDatabaseHash.get("jahia.database.script");
                    if (database_script.equals(getPropertiesFile().getProperty("db_script"))) {
                        if (curDatabaseHash.get("jahia.database.max_elements_for_in_clause") != null){
                            try {
                                int val = dBMaxElementsForInClause = Integer.parseInt(((String)curDatabaseHash
                                    .get("jahia.database.max_elements_for_in_clause")).trim());
                                if ( val > 0 ){
                                    dBMaxElementsForInClause = val;
                                }
                            } catch ( Exception t ){
                            }
                        }
                        break;
                    }
                }
            } catch ( Exception t ){
                logger.debug("Error loading db scripts, db_max_elements_for_in_clause will be the default value:"
                        + dBMaxElementsForInClause,t);
            }

            settings.put("userManagementUserNamePattern", getString(
                    "userManagementUserNamePattern", "[\\w\\{\\}\\-]+"));
            settings.put("userManagementGroupNamePattern", getString(
                    "userManagementGroupNamePattern", "[\\w\\{\\}\\-]+"));

            settings.put("default_templates_set",
                    getString("default_templates_set"));

            settings.put("templates.boxes.onError",
                    getString("templates.boxes.onError", null));

            settings.setFast(true);
            ehCacheJahiaFile = getString("ehcache.jahia.file","ehcache-jahia.xml");
            // If cluster is activated then try to expose some properties as system properties for JGroups
            boolean clusterActivated = getBoolean("cluster.activated",false);
            if(clusterActivated) {
                // First expose tcp ip binding address
                String tcpIpBinding = getString("cluster.tcp.start.ip_address");
                String numInitialMembers = getString("cluster.tcp.num_initial_members");
                System.setProperty("cluster.tcp.start.ip_address",tcpIpBinding);
                System.setProperty("cluster.tcp.num_initial_members",numInitialMembers);
                // Second get ehcache jgroups configuration for jahia
                System.setProperty("cluster.tcp.ehcache.jahia.nodes.ip_address",getString("cluster.tcp.ehcache.jahia.nodes.ip_address"));
                System.setProperty("cluster.tcp.ehcache.jahia.port",getString("cluster.tcp.ehcache.jahia.port"));
                // Second get ehcache jgroups configuration for hibernate
                System.setProperty("cluster.tcp.ehcache.hibernate.nodes.ip_address",getString("cluster.tcp.ehcache.hibernate.nodes.ip_address"));
                System.setProperty("cluster.tcp.ehcache.hibernate.port",getString("cluster.tcp.ehcache.hibernate.port"));
            }
        } catch (NullPointerException npe) {
            logger.debug ("Properties file is not valid...!", npe);
        } catch (NumberFormatException nfe) {
            logger.debug ("Properties file is not valid...!", nfe);
        }
    } // end load


    private boolean getBoolean (String propertyName)
        throws NoSuchElementException {
        boolean result = false;
        String curProperty = properties.getProperty(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            if (!"".equals(curProperty)) {
                result = Boolean.valueOf(curProperty).booleanValue();
                return result;
            } else {
                throw new NoSuchElementException("Boolean property : " +
                                                 propertyName + " is empty!");
            }
        } else {
            throw new NoSuchElementException("No boolean found for property : " +
                                             propertyName);
        }
    }

    private boolean getBoolean (String propertyName, boolean defaultValue) {
        try {
            return getBoolean(propertyName);
        } catch (NoSuchElementException nsee) {
            return defaultValue;
        }
    }

    private String getString (String propertyName) throws NoSuchElementException {
        String result;
        String curProperty = properties.getProperty(propertyName);
        if (curProperty != null) {
            result = curProperty.trim();
            return result;
        } else {
            throw new NoSuchElementException("No String found for property : " +
                                             propertyName);
        }
    }

    private String getString (String propertyName, String defaultValue) {
        try {
            return getString(propertyName);
        } catch (NoSuchElementException nsee) {
            return defaultValue;
        }
    }

    private int getInt (String propertyName)
        throws NoSuchElementException {
        int result = -1;
        String curProperty = properties.getProperty(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Integer.parseInt(curProperty);
            return result;
        } else {
            throw new NoSuchElementException("No int found for property : " +
                                             propertyName);
        }
    }

    private int getInt (String propertyName, int defaultValue) {
        try {
            return getInt(propertyName);
        } catch (NoSuchElementException nsee) {
            return defaultValue;
        }
    }

    private long getLong (String propertyName)
        throws NoSuchElementException {
        long result = -1;
        String curProperty = properties.getProperty(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Long.parseLong(curProperty);
            return result;
        } else {
            throw new NoSuchElementException("No long found for property : " +
                                             propertyName);
        }
    }

    private long getLong (String propertyName, long defaultValue) {
        try {
            return getLong(propertyName);
        } catch (NoSuchElementException nsee) {
            return defaultValue;
        }
    }

    /** Looks up the specified <code>key</code> parameter as a <code>String</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Returns <code>null</code> when the
     *               parameter could not be found.
     */
    public String lookupString (String key) {
        Object param = settings.get (key);
        if (param instanceof String)
            return (String) param;
        return null;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>boolean</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>false</code> when the
     *               parameter could not be found.
     */
    public boolean lookupBoolean (String key) {
        Object param = settings.get (key);
        if (param instanceof Boolean)
            return ((Boolean) param).booleanValue ();
        return false;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    public long lookupLong (String key) {
        Object param = settings.get (key);
        if (param instanceof Long)
            return ((Long) param).longValue ();
        return Long.MIN_VALUE;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    public int lookupInt (String key) {
        Object param = settings.get (key);
        if (param instanceof Integer)
            return ((Integer) param).intValue ();
        return Integer.MIN_VALUE;
    }

    /**
     * Get the principal properties object.
     *
     * @return  Properties object containing all properties from jahia.properties file.
     */
    public Properties getPropertiesFile () {
        return this.properties;
    } // end getPropertiesFile


    //-------------------------------------------------------------------------
    public String getLicenseFileName () {
        return licenseFilename;
    }

    public void setLicenseFilename(String licenseFilename) {
        this.licenseFilename = licenseFilename;
    }

    public void setSiteServerNameTestURLExpr (String siteServerNameTestURLExpr) {

        this.siteServerNameTestURLExpr = siteServerNameTestURLExpr;
    }

    public void setSiteServerNameTestConnectTimeout (int
        siteServerNameTestConnectTimeout) {
        this.siteServerNameTestConnectTimeout =
            siteServerNameTestConnectTimeout;
    }

    //--------------------------------------------------------------------------
    /**
     * Load mime types from web.xml files
     *
     * <mime-mapping>
     *   <extension>abs</extension>
     *   <mime-type>audio/x-mpeg</mime-type>
     * </mime-mapping>
     * <mime-mapping>
     *   <extension>ai</extension>
     *   <mime-type>application/postscript</mime-type>
     * </mime-mapping>
     */
    private void loadMimeTypes () {

        String filepath = pathResolver.resolvePath ("/WEB-INF/web.xml");
        File f = new File (filepath);
        if (f.exists () && f.canRead ()) {
            try {
                MimeTypesFromWebAppXmlFile mtHandler
                        = new MimeTypesFromWebAppXmlFile (f.getAbsolutePath (), mResolver);

                mimeTypes = mtHandler.getMimeTypes ();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (mimeTypes == null)
            mimeTypes = new Properties ();

    }

    //--------------------------------------------------------------------------
    /**
     * initiate the Dtd entity resolver we use with local dtd
     */
    private void initDtdEntityResolver () {

        mResolver = new DtdEntityResolver ();
        String diskPath = this.jahiaEtcDiskPath;

        // register local DTD
        //System.out.println(diskPath + JahiaConstants.WEB_DTD_RESOURCE_PATH_22);
        File resourceFile = new File (diskPath, JahiaConstants.WEB_DTD_RESOURCE_PATH_22);
        mResolver.registerDTD (JahiaConstants.WEB_DTD_PUBLICID_22, resourceFile);

        //System.out.println(diskPath + JahiaConstants.WEB_DTD_RESOURCE_PATH_23);
        resourceFile = new File (diskPath, JahiaConstants.WEB_DTD_RESOURCE_PATH_23);
        mResolver.registerDTD (JahiaConstants.WEB_DTD_PUBLICID_23, resourceFile);

        //System.out.println(diskPath + JahiaConstants.J2EE_APP_DTD_RESOURCE_PATH_12);
        resourceFile = new File (diskPath, JahiaConstants.J2EE_APP_DTD_RESOURCE_PATH_12);
        mResolver.registerDTD (JahiaConstants.J2EE_APP_DTD_PUBLICID_12, resourceFile);

        mResolver
                .registerSchema(JahiaConstants.TEMPLATES_DESCRIPTOR_20_URI,
                        new File(diskPath,
                                JahiaConstants.TEMPLATES_DESCRIPTOR_20_PATH));
    }

    //--------------------------------------------------------------------------
    /**
     * Return the mime types
     *
     * @return Properties mimeTypes
     */
    public Properties getMimeTypes () {
        return mimeTypes;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the Dtd entity resolver
     *
     * @return EntityResolver
     */
    public EntityResolver getDtdEntityResolver () {
        return mResolver;
    }

    /**
     * Does Jahia use the Unicode Transformation Format to encode output Strings
     *
     * @return  True if UTF-8 encoding, false otherwise
     */
    public boolean isUtf8Encoding () {
        return utf8Encoding;
    }

    /**
     * Are the locks verification activated ?
     *
     * @return The locksActived parameter.
     */
    public boolean areLocksActivated () {
        return locksActivated;
    }

    /**
     * Return status of site ID in URL generation
     * @return true if the site ID should be generated in all URLs
     */
    public boolean isSiteIDInURL () {
        return siteIDInURL;
    }

    /**
     * Return the activation/deactivation switch of the site/page-ID match check
     * @return true if the site and page ID match check should be performed
     */
    public boolean isPerformSiteAndPageIDMatchCheck () {
        return performSiteAndPageIDMatchCheck;
    }

    /**
     * Returns status of site ID in Search Hit page URLs
     *
     * @return true if search Hit Page URLs should be generated with siteKey when they come from another site than
     * the current site on which the search is launched
     */
    public boolean isSiteIDInSearchHitPageURL() {
        return siteIDInSearchHitPageURL;
    }

    public void setSiteIDInSearchHitPageURL(boolean siteIDInSearchHitPageURL) {
        this.siteIDInSearchHitPageURL = siteIDInSearchHitPageURL;
    }
    /**
     * Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    */
    public boolean isUseRelativeSiteURLs() {
        return useRelativeSiteURLs;
    }

    public void setUseRelativeSiteURLs(boolean val) {
        this.useRelativeSiteURLs = val;
    }


    public boolean isAppInheritingJahiaSessionAttributes () {
        return appInheritJahiaSessionAttributes;
    }


    public String getJahiaWebAppsDeployerBaseURL () {
        return jahiaWebAppsDeployerBaseURL;
    }

    public String getDefaultLanguageCode () {
        return defaultLanguageCode;
    }

    public boolean isAclPreloadActive() {
        return aclPreloadActive;
    }

    public void setAclPreloadActive(boolean aclPreloadActive) {
        this.aclPreloadActive = aclPreloadActive;
    }

    /**
     * Used to get the build number.
     *
     * @return  The build number.
     */
    public int getBuildNumber() {
        return buildNumber;
    } // end getBuildNumber

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * Used to get the server name (tomcat, orion, etc).
     *
     * @return  The server name.
     */
    public String getServer() {
        return server;
    } // end getServer

    /**
     * Used to get the server home filesystem disk path.
     *
     * @return  The server home filesystem disk path.
     */
    public String getServerHomeDiskPath() {
        return serverHomeDiskPath;
    } // end getServerHomeDiskPath

    /**
     * Used to get the web apps disk path.
     *
     * @return  The web apps disk path.
     */
    public String getJahiaWebAppsDiskPath() {
        return jahiaWebAppsDiskPath;
    } // end getJahiaWebAppsDiskPath

    /**
     * Used to get the templates disk path.
     *
     * @return  The templates disk path.
     */
    public String getJahiaTemplatesDiskPath() {
        return jahiaTemplatesDiskPath;
    } // end getJahiaTemplatesDiskPath

    /**
     * Returns the HtmlEditors Root Disk Path
     * @return  Returns the HtmlEditors Root Disk Path
     */
    public String getJahiaHtmlEditorsDiskPath() {
        return this.jahiaHtmlEditorsDiskPath;
    }

    /**
     * Used to get the engines disk path.
     *
     * @return  The engines disk path.
     */
    public String getJahiaEnginesDiskPath() {
        return jahiaEnginesDiskPath;
    } // end getJahiaEnginesDiskPath

    /**
     * Used to get the jahiafiles disk path.
     * @author  Eric Vassalli
     *
     * @return  The jahiafiles disk path.
     */
    /** todo removed by Serge Huber because of disk reorganisation */
    /*
    public String getJahiaFilesDiskPath() {
        return jahiaFilesDiskPath;
    } // end getJahiaFilesDiskPath
    */

    /**
     * Used to get the jahiafiles /etc disk path.
     *
     * @return  The jahiafiles /etc disk path.
     */
    public String getJahiaEtcDiskPath() {
        return jahiaEtcDiskPath;
    }

    /**
     * Used to get the jahiafiles /var disk path.
     *
     * @return  The jahiafiles /var disk path.
     */
    public String getJahiaVarDiskPath() {
        return jahiaVarDiskPath;
    }


    /**
     * Used to get the data jahiafiles disk path.
     *
     * @return  The data jahiafiles disk path.
     */
    public String getJahiaFilesDataDiskPath() {
        return jahiaFilesBigTextDiskPath;
    } // end getJahiaFilesDataDiskPath

    /**
     * Used to get the templates jahiafiles disk path.
     *
     * @return  The templates jahiafiles disk path.
     */
    public String getJahiaFilesTemplatesDiskPath() {
        return jahiaFilesTemplatesDiskPath;
    } // end getJahiaFilesTemplatesDiskPath


    /**
     * Used to get the CAS configuration directory disk path.
     *
     * @return  The Cas configuration disk path.
     */
    public String getJahiaCasDiskPath() {
        return jahiaCasDiskPath;
    } // end getJahiaCasDiskPath

    /**
     * Returns the disk path to the output cache configuration directory
     * @return a String containing the disk path to the output cache configuration
     * directory
     */

    public String getJahiaOutputCacheConfigDiskPath() {
        return jahiaOutputCacheConfigDiskPath;
    }

    /**
     * Used to get the new templates disk path.
     *
     * @return  The new templates disk path.
     */
    public String getJahiaNewTemplatesDiskPath() {
        return jahiaNewTemplatesDiskPath;
    } // end getJahiaNewTemplatesDiskPath


    /**
     * Used to get the shared templates disk path.
     *
     * @return  The shared templates disk path.
     */
    public String getJahiaSharedTemplatesDiskPath() {
        return jahiaSharedTemplatesDiskPath;
    }


    /**
     * Used to get the components disk path.
     *
     * @return  The components disk path.
     */
    public String getComponentsDiskPath() {
        return componentsDiskPath;
    } // end getComponentsDiskPath

    /**
     * Url to make a local jahia request
     */
    public String getLocalAccessUri() {
        return localAccessUri;
    }

    /**
     * Used to get the templates http path.
     *
     * @return  The templates http path.
     */
    public String getJahiaTemplatesHttpPath() {
        return jahiaTemplatesHttpPath;
    } // end getJahiaTemplatesHttpPath

    /**
     * Used to get the engines http path.
     *
     * @return  The engines http path.
     */
    public String getJahiaEnginesHttpPath() {
        return jahiaEnginesHttpPath;
    } // end getJahiaEnginesHttpPath

    /**
     * Used to get the javascript http path.
     * (returns the URL of the Javascript file needed by Jahia)
     * @return  The javascript http path.
     */
    public String getJsHttpPath() {
        return jahiaJavaScriptHttpPath;
    } // end getJsHttpPath

    /**
     * Used to get the jsp context.
     *
     * @return  The jsp context.
     */
    public String getJspContext() {
        return jspContext;
    } // end getJspContext

    /**
     * Used to get the html editor context.
     *
     * @return  The html editor context.
     */
    public String getHtmlEditorsContext() {
        return this.htmlEditorsContext;
    }

    /**
     * Used to get the templates context.
     *
     * @return  The templates context.
     */
    public String getTemplatesContext() {
        return templatesContext;
    } // end getTemplatesContext

    /**
     * Used to get the engines context.
     *
     * @return  The engines context.
     */
    public String getEnginesContext() {
        return enginesContext;
    } // end getEnginesContext(

    /**
     * Used to get the javascript context.
     *
     * @return  The javascript context.
     */
    public String getJavascriptContext() {
        return javascriptContext;
    } // end getJavascriptContext(

    /**
     * Used to get the webapps autodeployer flag.
     *
     * @return  The webapps autodeployer flag.
     */
    public int getJahiaWebAppsAutoDeploy() {
        return jahiaWebAppsAutoDeploy;
    } // end getJahiaWebAppsAutoDeploy

    /*
    public ServletConfig getConfig() {
        return config;
    }
    public ServletContext getContext() {
        return context;
    }
    */

    public String getClassDiskPath() {
        return classDiskPath;
    }
    public String getJahiaFileRepositoryDiskPath() {
        return jahiaFileRepositoryDiskPath;
    }
    public String getJahiaFilesBigTextDiskPath() {
        return jahiaFilesBigTextDiskPath;
    }
    public String getJahiaFilesDiskPath() {
        return jahiaFilesDiskPath;
    }
    public long getJahiaFileUploadMaxSize() {
        return jahiaFileUploadMaxSize;
    }
    public String getJahiaHomeDiskPath() {
        return jahiaHomeDiskPath;
    }
    public String getJahiaHostHttpPath() {
        return jahiaHostHttpPath;
    }
    public String getJahiaJavaScriptDiskPath() {
        return jahiaJavaScriptDiskPath;
    }
    public String getJahiaJavaScriptHttpPath() {
        return jahiaJavaScriptHttpPath;
    }
    public String getJahiaJspDiskPath() {
        return jahiaJspDiskPath;
    }
    public Map<String, Long> getMaxCachedValues() {
        return maxCachedValues;
    }
    public Map<String, Long> getMaxCachedGroupsValues() {
        return maxCachedGroupsValues;
    }
    public String getJahiaNewWebAppsDiskPath() {
        return jahiaNewWebAppsDiskPath;
    }
    public String getJahiaImportsDiskPath() {
        return jahiaImportsDiskPath;
    }
    public boolean isLocksActivated() {
        return locksActivated;
    }
    public String getMail_administrator() {
        return mail_administrator;
    }
    public String getMail_from() {
        return mail_from;
    }
    public String getMail_paranoia() {
        return mail_paranoia;
    }
    public String getMail_server() {
        return mail_server;
    }
    public boolean isJmxActivated() {
        return jmxActivated;
    }
    public boolean isJmxHTTPAdaptorActivated() {
        return jmxHTTPAdaptorActivated;
    }
    public String getJmxHTTPAuthorizationPassword() {
        return jmxHTTPAuthorizationPassword;
    }
    public String getJmxHTTPAuthorizationUser() {
        return jmxHTTPAuthorizationUser;
    }
    public String getJmxHTTPAutorizationMode() {
        return jmxHTTPAutorizationMode;
    }
    public String getJmxHTTPHostname() {
        return jmxHTTPHostname;
    }
    public int getJmxHTTPPort() {
        return jmxHTTPPort;
    }
    public String getJmxHTTPProcessorNameString() {
        return jmxHTTPProcessorNameString;
    }
    public String getJmxHTTPSocketFactoryNameString() {
        return jmxHTTPSocketFactoryNameString;
    }
    public boolean isJmxRMIAdaptorActivated() {
        return jmxRMIAdaptorActivated;
    }
    public boolean isJmxXSLProcessorActivated() {
        return jmxXSLProcessorActivated;
    }
    public boolean isJmxRMISSLServerSocketFactoryActivated() {
        return jmxRMISSLServerSocketFactoryActivated;
    }
    public String getJmxRMISSLServerSocketFactoryKeyStoreName() {
        return jmxRMISSLServerSocketFactoryKeyStoreName;
    }
    public String getJmxRMISSLServerSocketFactoryKeyStorePassword() {
        return jmxRMISSLServerSocketFactoryKeyStorePassword;
    }
    public String getJmxRMISSLServerSocketFactoryKeyManagerPassword() {
        return jmxRMISSLServerSocketFactoryKeyManagerPassword;
    }

    public String getDefaultResponseBodyEncoding() {
        return defaultResponseBodyEncoding;
    }
    public String getDefaultURIEncoding() {
        return defaultURIEncoding;
    }
    public String getDefaultSite() {
        return defaultSite;
    }
    public int getPreloadCountForPageProperties() {
        return preloadCountForPageProperties;
    }

    public boolean isTransformFilenames() {
        return transformFilenames;
    }

    public boolean isTransformDirnames() {
        return transformDirnames;
    }
    public boolean isPreloadFolders() {
        return preloadFolders;
    }
    public int getCookieAuthIDLength() {
        return cookieAuthIDLength;
    }
    public String getCookieAuthUserPropertyName() {
        return cookieAuthUserPropertyName;
    }
    public String getCookieAuthCookieName() {
        return cookieAuthCookieName;
    }
    public int getCookieAuthMaxAgeInSeconds() {
        return cookieAuthMaxAgeInSeconds;
    }
    public boolean isCookieAuthRenewalActivated() {
        return cookieAuthRenewalActivated;
    }
    public String getAuthPipelineFileName() {
        return authPipelineFileName;
    }
    public String getAuthPipelineClassName() {
        return authPipelineClassName;
    }
    public String getProcessPipelineClassName() {
        return processPipelineClassName;
    }
    public String getProcessPipelineFileName() {
        return processPipelineFileName;
    }
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public boolean isCookieAuthActivated() {
        return cookieAuthActivated;
    }
    public String getTmpContentDiskPath() {
    return tmpContentDiskPath;
  }
    public long getTemplatesObserverInterval() {
        return templatesObserverInterval;
    }
    public long getWebAppsObserverInterval() {
        return webAppsObserverInterval;
    }
    public String getSchedulerConfigFile() {
        return schedulerConfigFile;
    }

    public String getRamSchedulerConfigFile() {
        return ramSchedulerConfigFile;
    }

    public boolean isProcessingServer() {
        return isProcessingServer;
    }

    public String getSiteServerNameTestURLExpr () {

        return siteServerNameTestURLExpr;
    }

    public int getSiteServerNameTestConnectTimeout () {
        return siteServerNameTestConnectTimeout;
    }

    public String getJetspeedDeploymentDirectory() {
        return jetspeedDeploymentDirectory;
    }

    public void setJetspeedDeploymentDirectory(String jetspeedDeploymentDirectory) {
        this.jetspeedDeploymentDirectory = jetspeedDeploymentDirectory;
    }

    public String getPropertiesFileName() {
        return propertiesFileName;
    }

    public void setPropertiesFileName(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public int getWorkflowMaxNotificationEmails() {
        return workflowMaxNotificationEmails;
    }

    public void setWorkflowMaxNotificationEmails(int workflowMaxNotificationEmails) {
        this.workflowMaxNotificationEmails = workflowMaxNotificationEmails;
    }

    public boolean isWorkflowNotifyAdminOnMaxNotificationReached() {
        return workflowNotifyAdminOnMaxNotificationReached;
    }

    public void setWorkflowNotifyAdminOnMaxNotificationReached(boolean workflowNotifyAdminOnMaxNotificationReached) {
        this.workflowNotifyAdminOnMaxNotificationReached = workflowNotifyAdminOnMaxNotificationReached;
    }

    public String getWorkflowMaxNotificationReachedTemplate() {
        return workflowMaxNotificationReachedTemplate;
    }

    public void setWorkflowMaxNotificationReachedTemplate(String workflowMaxNotificationReachedTemplate) {
        this.workflowMaxNotificationReachedTemplate = workflowMaxNotificationReachedTemplate;
    }

    public int getSiteURLPortOverride() {
        return siteURLPortOverride;
    }

    public void setSiteURLPortOverride(int siteURLPortOverride) {
        this.siteURLPortOverride = siteURLPortOverride;
    }

    public int getMail_maxRegroupingOfPreviousException() {
        return mail_maxRegroupingOfPreviousException;
    }

    public void setMail_maxRegroupingOfPreviousException(int mail_maxRegroupingOfPreviousException) {
        this.mail_maxRegroupingOfPreviousException = mail_maxRegroupingOfPreviousException;
    }

    /**
     * the (optional) url the user will be redirected after logout
     */
    public String getLogoutRedirectUrl() {
        return logoutRedirectUrl;
    }

    /**
     * Returns the (optional) URL user will be forwarded to after logout.
     * @return the (optional) URL user will be forwarded to after logout
     */
    public String getLogoutForwardUrl() {
        return logoutForwardUrl;
    }

    public void setLogoutRedirectUrl(String logoutRedirectUrl) {
        this.logoutRedirectUrl = logoutRedirectUrl;
    }

    public boolean isDoRedirectOnLogout() {
        return doRedirectOnLogout;
    }    

    public String getFreeMemoryLimit() {
        return freeMemoryLimit;
    }

    public void setFreeMemoryLimit(String freeMemoryLimit) {
        if(freeMemoryLimit.indexOf("MB")<=0) return;
        this.freeMemoryLimit = freeMemoryLimit;
    }

    public int getClusterCacheMaxBatchSize() {
        return clusterCacheMaxBatchSize;
    }

    public void setClusterCacheMaxBatchSize(int clusterCacheMaxBatchSize) {
        this.clusterCacheMaxBatchSize = clusterCacheMaxBatchSize;
    }

    public boolean isUseFlatFileManager() {
        return useFlatFileManager;
    }

    public boolean isWaiAutoAddMissingAttributes() {
        return waiAutoAddMissingAttributes;
    }

    public boolean showTimeBasedPublishingIcons() {
        return showTimeBasedPublishingIcons;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    /**
     * to get the Site errors page behavior
     * @return a boolean
     */
    public boolean getSiteErrorEnabled() {
        return isSiteErrorEnabled;
    }

    public int getConnectionTimeoutForProductionJob() {
        return connectionTimeoutForProductionJob;
    }

    public boolean isAclDisp() {
        return aclDisp;
    }

    public boolean isTbpDisp() {
        return tbpDisp;
    }

    public boolean isWflowDisp() {
        return wflowDisp;
    }

    public boolean isChatDisp() {
        return chatDisp;
    }

    public boolean isPdispDisp() {
        return pdispDisp;
    }

    public int getSiteMapTitlesLength() {
        return siteMapTitlesLength;
    }

    public int getWorkflowTitlesLength() {
        return workflowTitlesLength;
    }

    public boolean isBatchLoadingEnabled() {
        return batchLoadingEnabled;
    }

    public void setBatchLoadingEnabled(boolean batchLoadingEnabled) {
        this.batchLoadingEnabled = batchLoadingEnabled;
    }

    public int getBatchLoadingSize() {
        return batchLoadingSize;
    }

    public void setBatchLoadingSize(int batchLoadingSize) {
        this.batchLoadingSize = batchLoadingSize;
    }


	public String getCacheClusterUnderlyingImplementation() {
		return cacheClusterUnderlyingImplementation;
	}

	public void setCacheClusterUnderlyingImplementation(
			String cacheClusterUnderlyingImplementation) {
		this.cacheClusterUnderlyingImplementation = cacheClusterUnderlyingImplementation;
	}

    /**
     * max items allowed to be preloaded for pagination calculation.
     * @return
     */
    public int getPreloadedItemsForPagination() {
        return preloadedItemsForPagination;
    }

    public void setPreloadedItemsForPagination(int preloadedItemsForPagination) {
        this.preloadedItemsForPagination = preloadedItemsForPagination;
    }

    public int getCacheMaxGroups() {
        return cacheMaxGroups;
    }

    public void setCacheMaxGroups(int cacheMaxGroups) {
        this.cacheMaxGroups = cacheMaxGroups;
    }

    public int getMaxAggregatedEvents() {
        return maxAggregatedEvents;
    }

    public void setMaxAggregatedEvents(int maxAggregatedEvents) {
        this.maxAggregatedEvents = maxAggregatedEvents;
    }

    public long getPageGenerationWaitTime() {
        return pageGenerationWaitTime;
    }

    public int getSuggestedRetryTimeAfterTimeout() {
        return suggestedRetryTimeAfterTimeout;
    }

    public long getPageGenerationWaitTimeOnStartup() {
        return pageGenerationWaitTimeOnStartup;
    }

    public int getSuggestedRetryTimeAfterTimeoutOnStartup() {
        return suggestedRetryTimeAfterTimeoutOnStartup;
    }

    public int getMaxParallelProcessings() {
        return maxParallelProcessings;
    }


    public int getEditModeSessionTimeout() {
        return editModeSessionTimeout;
    }

    public void setEditModeSessionTimeout(int editModeSessionTimeout) {
        this.editModeSessionTimeout = editModeSessionTimeout;
    }

    public boolean isOutputContainerCacheActivated() {
        return outputContainerCacheActivated;
    }

    public void setOutputContainerCacheActivated(boolean outputContainerCacheActivated) {
        this.outputContainerCacheActivated = outputContainerCacheActivated;
    }


    /**
     * Returns the DB max elements for SQL IN clause, to limit the scope of returned row
     * or for optimized sql query rewritting.
     *
     * @return
     */
    public int getDBMaxElementsForInClause() {
        return dBMaxElementsForInClause;
    }

    public boolean isPreloadDBGroupMembersActivated() {
        return preloadDBGroupMembersActivated;
    }

    public String getPdisplayTimerRefresh() {
        return pdisplayTimerRefresh;
    }

    /**
     * Return the number max of alias name.
     * @return
     */
    public int getNbMaxContentAliasName() {
        return nbMaxContentAliasName;
    }

    public void setNbMaxContentAliasName(int nbMaxContentAliasName) {
        this.nbMaxContentAliasName = nbMaxContentAliasName;
    }

    public boolean isDisplayMarkedForDeletedContentObjects() {
        return displayMarkedForDeletedContentObjects;
    }

    public boolean areDeprecatedNonContainerFieldsUsed() {
        return deprecatedNonContainerFieldsUsed;
    }

    public void setDeprecatedNonContainerFieldsUsed(
            boolean deprecatedNonContainerFieldsUsed) {
        if (!this.deprecatedNonContainerFieldsUsed && deprecatedNonContainerFieldsUsed){
            logger.warn("YOU ARE USING FIELDS WITHOUT CONTAINERS, WHICH ARE DEPRECATED AND WILL NOT BE SUPPORTED IN THE NEXT MAJOR RELEASE OF JAHIA.");
            Thread.dumpStack();
        }
        this.deprecatedNonContainerFieldsUsed = deprecatedNonContainerFieldsUsed;
    }

    public boolean isWrapperBufferFlushingActivated() {
        return wrapperBufferFlushingActivated;
    }

    public void setWrapperBufferFlushingActivated(boolean wrapperBufferFlushingActivated) {
        this.wrapperBufferFlushingActivated = wrapperBufferFlushingActivated;
    }

    public boolean isWrapperBufferFlushingAlsoForPortlets() {
        return wrapperBufferFlushingAlsoForPortlets;
    }

    public void setWrapperBufferFlushingAlsoForPortlets(boolean wrapperBufferFlushingAlsoForPortlets) {
        this.wrapperBufferFlushingAlsoForPortlets = wrapperBufferFlushingAlsoForPortlets;
    }

    public long getContainerCacheDefaultExpirationDelay() {
        return containerCacheDefaultExpirationDelay;
    }

    public void setContainerCacheDefaultExpirationDelay(long containerCacheDefaultExpirationDelay) {
        this.containerCacheDefaultExpirationDelay = containerCacheDefaultExpirationDelay;
    }

    public boolean isContainerCacheLiveModeOnly() {
        return containerCacheLiveModeOnly;
    }

    public void setContainerCacheLiveModeOnly(boolean containerCacheLiveModeOnly) {
        this.containerCacheLiveModeOnly = containerCacheLiveModeOnly;
    }

    public boolean isWorkflowDisplayStatusForLinkedPages() {
        return workflowDisplayStatusForLinkedPages;
    }

    public void setWorkflowDisplayStatusForLinkedPages(
            boolean workflowDisplayStatusForLinkedPages) {
        this.workflowDisplayStatusForLinkedPages = workflowDisplayStatusForLinkedPages;
    }

    public boolean isWorkflowUseExternalByDefault() {
        return workflowUseExternalByDefault;
    }

    public void setWorkflowUseExternalByDefault(boolean workflowUseExternalByDefault) {
        this.workflowUseExternalByDefault = workflowUseExternalByDefault;
    }

    public boolean isInlineEditingActivated() {
        return inlineEditingActivated;
    }

    public void setInlineEditingActivated(boolean inlineEditingActivated) {
        this.inlineEditingActivated = inlineEditingActivated;
    }

    public boolean isPortletAJAXRenderingActivated() {
        return portletAJAXRenderingActivated;
    }

    public void setPortletAJAXRenderingActivated(boolean portletAJAXRenderingActivated) {
        this.portletAJAXRenderingActivated = portletAJAXRenderingActivated;
    }

    public boolean isGmailPasswordExported() {
        return gmailPasswordExported;
    }

    public void setGmailPasswordExported(boolean gmailPasswordExported) {
        this.gmailPasswordExported = gmailPasswordExported;
    }

    public int getPaginationWindowSize() {
        return paginationWindowSize;
    }

    public void setCheckAclInPagePath(boolean checkAclInPagePath) {
        this.checkAclInPagePath = checkAclInPagePath ;
    }

    public boolean isCheckAclInPagePath() {
        return this.checkAclInPagePath ;
    }

    public String getEhCacheJahiaFile() {
        return ehCacheJahiaFile;
    }
}
