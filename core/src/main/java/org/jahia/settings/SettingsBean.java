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
import org.jahia.utils.JahiaTools;
import org.jahia.utils.PathResolver;
import org.jahia.utils.maven.plugin.deployers.ServerDeploymentFactory;
import org.jahia.utils.maven.plugin.deployers.ServerDeploymentInterface;
import org.jahia.utils.properties.PropertiesManager;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SettingsBean {

    private static final transient Logger logger =
            Logger.getLogger (SettingsBean.class);
    
    public static final String JAHIA_PROPERTIES_FILE_PATH = "/WEB-INF/etc/config/jahia.properties";
    
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
    private String serverVersion;
    private String serverHome;
    private String jahiaHomeDiskPath;
    private String jahiaTemplatesDiskPath;
    private String jahiaWebAppsDiskPath;
    private String jahiaEnginesDiskPath;
    private String jahiaJspDiskPath;
    private String jahiaFilesDiskPath;
    private String jahiaEtcDiskPath;
    private String jahiaVarDiskPath;
    private String jahiaHostHttpPath;
    private String jahiaTemplatesHttpPath;
    private String jahiaEnginesHttpPath;
    private String jahiaWebAppsDeployerBaseURL;
    private String jahiaJavaScriptDiskPath;
    private String jahiaNewWebAppsDiskPath;
    private String jahiaImportsDiskPath;
    private String jahiaSharedTemplatesDiskPath;
    private String jahiaDatabaseScriptsPath;
    private String jahiaCkEditorDiskPath;

    public String getJahiaDatabaseScriptsPath() {
        return jahiaDatabaseScriptsPath;
    }

    private String jspContext;
    private String templatesContext;
    private String enginesContext;
    private String javascriptContext;

    private String jahiaJavaScriptHttpPath;
    private String classDiskPath;
    // map containing all max_cached_*...
    private Map<String, Long> maxCachedValues;
    // map containing all max_cachedgroups_*...
    private Map<String, Long> maxCachedGroupsValues;

    // this is the list of jahia.properties files values...
    private long jahiaFileUploadMaxSize;

    // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    private boolean useRelativeSiteURLs;

    // Default language code for multi-language system
    private String defaultLanguageCode;

    // the (optional) url the user will be redirected after logout
    public String logoutRedirectUrl;
    // The (optional) URL the user will be forwarded to after logout
    public String logoutForwardUrl;
    // Generally do a client side redirect instead of forward after logout 
    private boolean doRedirectOnLogout = true;    

    // this is the list of jahia.properties mail settings values...
    private boolean mail_service_activated;
    private String mail_server;
    private String mail_administrator;
    private String mail_from;
    private String mail_paranoia;
    private int mail_maxRegroupingOfPreviousException = 500;

    private String characterEncoding;

    private String tmpContentDiskPath;
    private long templatesObserverInterval;
    private boolean isProcessingServer;

    private String siteServerNameTestURLExpr;
    private int siteServerNameTestConnectTimeout;
    private int siteURLPortOverride = -1;

    private boolean isSiteErrorEnabled;

    private String freeMemoryLimit= new Double((Runtime.getRuntime().maxMemory()*0.2)/ (1024*1024)).longValue() + "MB";

    private int clusterCacheMaxBatchSize = 100000;
    private String cacheClusterUnderlyingImplementation = "jahiaReferenceCache";

    private int cacheMaxGroups = 10000;

    private boolean developmentMode = true;
    // Settings to control servlet response wrapper flushing
    private boolean wrapperBufferFlushingActivated = true;


    private static SettingsBean instance = null;
    private boolean considerPreferredLanguageAfterLogin;
    
    private boolean considerDefaultJVMLocale;

    private String ehCacheJahiaFile;

    private boolean permanentMoveForVanityURL = true;
    
    private ServerDeploymentInterface serverDeployer = null;

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
    					Properties props,
    					Resource licenseFile) throws IOException {
        this.pathResolver = pathResolver;
        this.properties = props;
        this.licenseFilename = licenseFile.getFile().toString();
        instance = this;
    }

    public static SettingsBean getInstance() {
        return instance;
    }

    /**
     * This method load and convert properties from the jahia.properties file,
     * and set some variables used by the SettingsBean class.
     */
    public void load () {
    	if (properties == null && propertiesFileName != null) {
    		properties = new PropertiesManager(propertiesFileName).getPropertiesObject();
    	}

        // try to get values from the properties object...
        try {
            // disk path, url's and context...
            server = getString("server");
            serverVersion = getString("serverVersion", "");
            serverHome = getString("serverHome", "");
            ServerDeploymentFactory.setTargetServerDirectory(serverHome);
            serverDeployer = ServerDeploymentFactory.getInstance().getImplementation(server + serverVersion);
            
            jahiaTemplatesDiskPath = pathResolver.resolvePath (getString("jahiaTemplatesDiskPath"));
            jahiaJspDiskPath = pathResolver.resolvePath (getString("jahiaJspDiskPath"));
            jahiaEnginesDiskPath = pathResolver.resolvePath (getString("jahiaEnginesDiskPath"));
            jahiaJavaScriptDiskPath = pathResolver.resolvePath (getString("jahiaJavaScriptDiskPath"));
            classDiskPath = pathResolver.resolvePath ("/WEB-INF/classes/");
            jahiaFilesDiskPath = JahiaTools.convertContexted (getString("jahiaFilesDiskPath"), pathResolver);
            jahiaEtcDiskPath = JahiaTools.convertContexted (getString("jahiaEtcDiskPath"), pathResolver);
            jahiaVarDiskPath = JahiaTools.convertContexted (getString("jahiaVarDiskPath"), pathResolver);
            tmpContentDiskPath = JahiaTools.convertContexted (getString("tmpContentDiskPath"), pathResolver);
            try {
                File tmpContentDisk = new File(tmpContentDiskPath);
                if (!tmpContentDisk.exists()) {
                    tmpContentDisk.mkdirs();
                }
            } catch (Exception e) {
                logger.error("Provided folder for tmpContentDiskPath is not valid. Cause: " + e.getMessage(), e);
            }
            jahiaNewWebAppsDiskPath = JahiaTools.convertContexted (getString("jahiaNewWebAppsDiskPath"), pathResolver);
            jahiaImportsDiskPath = JahiaTools.convertContexted (getString("jahiaImportsDiskPath"), pathResolver);
            jahiaSharedTemplatesDiskPath = JahiaTools.convertContexted (getString("jahiaSharedTemplatesDiskPath"), pathResolver);
            jahiaCkEditorDiskPath = JahiaTools.convertContexted (getString("jahiaCkEditorDiskPath"), pathResolver);
            jahiaDatabaseScriptsPath = jahiaVarDiskPath + File.separator + "db";

            jahiaHostHttpPath = getString("jahiaHostHttpPath");
            jahiaTemplatesHttpPath = JahiaTools.convertWebContexted(getString("jahiaTemplatesHttpPath"));
            jahiaEnginesHttpPath = JahiaTools.convertWebContexted(getString("jahiaEnginesHttpPath"));
            jahiaJavaScriptHttpPath = JahiaTools.convertWebContexted(getString("jahiaJavaScriptHttpPath"));
            jspContext = getString("jahiaJspDiskPath");
            templatesContext = getString("jahiaTemplatesDiskPath");
            enginesContext = getString("jahiaEnginesDiskPath");
            javascriptContext = getString("jahiaJavaScriptDiskPath");

            // jahia real path...
            File jahiaContextFolder = new File (pathResolver.resolvePath("." + File.separator));
            File parent = jahiaContextFolder.getAbsoluteFile().getParentFile ();

            if (server.toLowerCase().contains("tomcat")) {      // the server is tomcat
                jahiaHomeDiskPath = parent.getAbsolutePath ();
                // look in the properties file. If not found guess from jahiaContextFolder
                jahiaWebAppsDiskPath = properties.getProperty("jahiaWebAppsDiskPath");
                if(jahiaWebAppsDiskPath == null || jahiaWebAppsDiskPath.length() == 0){
                    jahiaWebAppsDiskPath = parent.getParentFile ().getAbsolutePath () + File.separator;
                }else{
                   jahiaWebAppsDiskPath.trim();
                }
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
            templatesObserverInterval = getLong("templates.observer.interval", 5000);

            // files...
            jahiaFileUploadMaxSize = getLong("jahiaFileUploadMaxSize");

            characterEncoding = getString("characterEncoding", "UTF-8");

            // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
            useRelativeSiteURLs = getBoolean ("useRelativeSiteURLs", false);

            // base URL (schema, host, port) to call the web apps deployer service.
            jahiaWebAppsDeployerBaseURL = getString ("jahiaWebAppsDeployerBaseURL", "http://127.0.0.1:8080/manager");

            // multi language default language code property.
            defaultLanguageCode = getString ("org.jahia.multilang.default_language_code", "en");

            considerDefaultJVMLocale = getBoolean("considerDefaultJVMLocale", false);
                
            considerPreferredLanguageAfterLogin = getBoolean("considerPreferredLanguageAfterLogin", false);

            // mail settings...
            mail_service_activated = getBoolean("mail_service_activated", false);
            mail_server = getString("mail_server");
            mail_administrator = getString("mail_administrator");
            mail_from = getString("mail_from");
            mail_maxRegroupingOfPreviousException = getInt("mail_maxRegroupingOfPreviousException", 500);

            // paranoia settings...
            mail_paranoia = getString("mail_paranoia", "Disabled");

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

            isProcessingServer = getBoolean("processingServer", true);

            siteServerNameTestURLExpr = getString("siteServerNameTestURLExpr", "${request.scheme}://${siteServerName}:${request.serverPort}${request.contextPath}/isjahia.jsp");
            siteServerNameTestConnectTimeout = getInt("siteServerNameTestConnectTimeout", 500);

            siteURLPortOverride = getInt("siteURLPortOverride", 0);

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

            developmentMode = getBoolean("developmentMode",true);

            wrapperBufferFlushingActivated = getBoolean("wrapperBufferFlushingActivated", true);

            permanentMoveForVanityURL = getBoolean("permanentMoveForVanityURL", true);

            settings.put("userManagementUserNamePattern", getString(
                    "userManagementUserNamePattern", "[\\w\\{\\}\\-]+"));
            settings.put("userManagementGroupNamePattern", getString(
                    "userManagementGroupNamePattern", "[\\w\\{\\}\\-]+"));

            settings.put("default_templates_set",
                    getString("default_templates_set"));

            settings.put("templates.modules.onError", getString("templates.modules.onError", "compact"));

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

    /**
     * Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    */
    public boolean isUseRelativeSiteURLs() {
        return useRelativeSiteURLs;
    }

    public void setUseRelativeSiteURLs(boolean val) {
        this.useRelativeSiteURLs = val;
    }

    public String getJahiaWebAppsDeployerBaseURL () {
        return jahiaWebAppsDeployerBaseURL;
    }

    public String getDefaultLanguageCode () {
        return defaultLanguageCode;
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
    public String getServerHome() {
        return serverHome;
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
     * Used to get the shared templates disk path.
     *
     * @return  The shared templates disk path.
     */
    public String getJahiaSharedTemplatesDiskPath() {
        return jahiaSharedTemplatesDiskPath;
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
     * Used to get the templates context.
     *
     * @return  The templates context.
     */
    public String getTemplatesContext() {
        return templatesContext;
    }

    /**
     * Used to get the engines context.
     *
     * @return  The engines context.
     */
    public String getEnginesContext() {
        return enginesContext;
    }

    /**
     * Used to get the javascript context.
     *
     * @return  The javascript context.
     */
    public String getJavascriptContext() {
        return javascriptContext;
    }

    public String getClassDiskPath() {
        return classDiskPath;
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

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public PathResolver getPathResolver() {
        return pathResolver;
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public String getTmpContentDiskPath() {
    return tmpContentDiskPath;
  }
    public long getTemplatesObserverInterval() {
        return templatesObserverInterval;
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
        return null;
        //throw new UnsupportedOperationException("jetspeedDeploymentDirectory no longer supported!");
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

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    /**
     * to get the Site errors page behavior
     * @return a boolean
     */
    public boolean getSiteErrorEnabled() {
        return isSiteErrorEnabled;
    }

    public String getCacheClusterUnderlyingImplementation() {
		return cacheClusterUnderlyingImplementation;
	}

	public void setCacheClusterUnderlyingImplementation(
			String cacheClusterUnderlyingImplementation) {
		this.cacheClusterUnderlyingImplementation = cacheClusterUnderlyingImplementation;
	}

    public int getCacheMaxGroups() {
        return cacheMaxGroups;
    }

    public void setCacheMaxGroups(int cacheMaxGroups) {
        this.cacheMaxGroups = cacheMaxGroups;
    }

    public boolean isWrapperBufferFlushingActivated() {
        return wrapperBufferFlushingActivated;
    }

    public void setWrapperBufferFlushingActivated(boolean wrapperBufferFlushingActivated) {
        this.wrapperBufferFlushingActivated = wrapperBufferFlushingActivated;
    }

    public String getEhCacheJahiaFile() {
        return ehCacheJahiaFile;
    }

    public boolean isConsiderDefaultJVMLocale() {
        return considerDefaultJVMLocale;
    }

    public void setConsiderDefaultJVMLocale(boolean considerDefaultJVMLocale) {
        this.considerDefaultJVMLocale = considerDefaultJVMLocale;
    }

    public boolean isConsiderPreferredLanguageAfterLogin() {
        return considerPreferredLanguageAfterLogin;
    }

    public void setConsiderPreferredLanguageAfterLogin(
            boolean considerPreferredLanguageAfterLogin) {
        this.considerPreferredLanguageAfterLogin = considerPreferredLanguageAfterLogin;
    }

    /**
     * Saves the current configuration back to files. 
     */
    public void save() {
    	
    }

	public boolean isMail_service_activated() {
    	return mail_service_activated;
    }

	public void setMail_service_activated(boolean mailServiceActivated) {
    	mail_service_activated = mailServiceActivated;
    }

	public void setMail_server(String mailServer) {
    	mail_server = mailServer;
    }

	public void setMail_administrator(String mailAdministrator) {
    	mail_administrator = mailAdministrator;
    }

	public void setMail_from(String mailFrom) {
    	mail_from = mailFrom;
    }

	public void setMail_paranoia(String mailParanoia) {
    	mail_paranoia = mailParanoia;
    }

    public boolean isPermanentMoveForVanityURL() {
        return permanentMoveForVanityURL;
    }

    public String getJahiaCkEditorDiskPath() {
        return jahiaCkEditorDiskPath;
    }

    /**
     * @return the serverVersion
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * @return the serverDeployer
     */
    public ServerDeploymentInterface getServerDeployer() {
        return serverDeployer;
    }
}