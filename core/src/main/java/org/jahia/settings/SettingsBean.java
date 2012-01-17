/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.PathResolver;
import org.jahia.bin.Jahia;
import org.jahia.configuration.deployers.ServerDeploymentFactory;
import org.jahia.configuration.deployers.ServerDeploymentInterface;
import org.jahia.utils.properties.PropertiesManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SettingsBean implements ServletContextAware, InitializingBean, ApplicationContextAware {

    private static final transient Logger logger =
            org.slf4j.LoggerFactory.getLogger (SettingsBean.class);
    
    public static final String JAHIA_PROPERTIES_FILE_PATH = "/WEB-INF/etc/config/jahia.properties";
    
    /** The map holding all the settings. */
    final private FastHashMap settings = new FastHashMap ();

    private PathResolver pathResolver = null;
    private String licenseFilename;
    private String propertiesFileName;
    // this is the famous build number...
    private int buildNumber;

    private Properties properties;


    // this is the list of jahia.properties server disk path and context path values...
    private String server;
    private String serverVersion;
    private String serverHome;
    private String jahiaHomeDiskPath;
    private String jahiaTemplatesDiskPath;
    private String jahiaWebAppsDiskPath;
    private String jahiaFilesDiskPath;
    private String jahiaEtcDiskPath;
    private String jahiaVarDiskPath;
    private String jahiaWebAppsDeployerBaseURL;
    private String jahiaImportsDiskPath;
    private String jahiaSharedTemplatesDiskPath;
    private String jahiaDatabaseScriptsPath;

    public String getJahiaDatabaseScriptsPath() {
        return jahiaDatabaseScriptsPath;
    }

    private String classDiskPath;

    // this is the list of jahia.properties files values...
    private long jahiaFileUploadMaxSize;

    // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    private boolean useRelativeSiteURLs;

    // Default language code for multi-language system
    private String defaultLanguageCode;

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

    private int siteURLPortOverride = -1;

    private boolean isSiteErrorEnabled;

    private String operatingMode = "development";
    private boolean productionMode = false;
    private boolean distantPublicationServerMode = true;
    
    // Settings to control servlet response wrapper flushing
    private boolean wrapperBufferFlushingActivated = true;


    private static SettingsBean instance = null;
    private boolean considerPreferredLanguageAfterLogin;
    
    private boolean considerDefaultJVMLocale;

    private boolean permanentMoveForVanityURL = true;

    private boolean dumpErrorsToFiles = true;
    private int fileDumpMaxRegroupingOfPreviousException = 500;
    private boolean useJstackForThreadDumps;  
    private boolean urlRewriteSeoRulesEnabled;

    private ServerDeploymentInterface serverDeployer = null;

	private boolean maintenanceMode;

    private ServletContext servletContext;

    private Resource licenseFile;

    private ApplicationContext applicationContext;

    private List<String> licenseFileLocations;

    private boolean disableJsessionIdParameter = true;
    private String jsessionIdParameterName = "jsessionid";

    private String guestUserResourceModuleName;
    private String guestUserResourceKey;

    private String guestGroupResourceModuleName;
    private String guestGroupResourceKey;
    
    private boolean fileServletStatisticsEnabled;

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
    					List<String> licenseFileLocations) throws IOException {
        this.pathResolver = pathResolver;
        this.properties = new Properties();
        properties.putAll(props);
        this.licenseFileLocations = licenseFileLocations;
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
            
            maintenanceMode = getBoolean("maintenanceMode", false);
            
            jahiaTemplatesDiskPath = pathResolver.resolvePath ("/modules/");
            classDiskPath = pathResolver.resolvePath ("/WEB-INF/classes/");
            jahiaFilesDiskPath = convertContexted (getString("jahiaFilesDiskPath"), pathResolver);
            jahiaEtcDiskPath = convertContexted (getString("jahiaEtcDiskPath"), pathResolver);
            jahiaVarDiskPath = convertContexted (getString("jahiaVarDiskPath"), pathResolver);
            tmpContentDiskPath = convertContexted (getString("tmpContentDiskPath"), pathResolver);
            try {
                File tmpContentDisk = new File(tmpContentDiskPath);
                if (!tmpContentDisk.exists()) {
                    tmpContentDisk.mkdirs();
                }
            } catch (Exception e) {
                logger.error("Provided folder for tmpContentDiskPath is not valid. Cause: " + e.getMessage(), e);
            }
            jahiaImportsDiskPath = convertContexted (getString("jahiaImportsDiskPath"), pathResolver);
            jahiaSharedTemplatesDiskPath = convertContexted (getString("jahiaSharedTemplatesDiskPath"), pathResolver);
            jahiaDatabaseScriptsPath = jahiaVarDiskPath + File.separator + "db";

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
            mail_paranoia = StringUtils.defaultIfEmpty(getString("mail_paranoia", "Disabled"), "Disabled");

            isProcessingServer = getBoolean("processingServer", true);

            siteURLPortOverride = getInt("siteURLPortOverride", 0);

            isSiteErrorEnabled = getBoolean("site.error.enabled",false);

            operatingMode = getString("operatingMode", "development");
            productionMode = !"development".equalsIgnoreCase(operatingMode);
            distantPublicationServerMode = "distantPublicationServer".equalsIgnoreCase(operatingMode);

            wrapperBufferFlushingActivated = getBoolean("wrapperBufferFlushingActivated", true);

            permanentMoveForVanityURL = getBoolean("permanentMoveForVanityURL", true);

            dumpErrorsToFiles = getBoolean("dumpErrorsToFiles", true);
            fileDumpMaxRegroupingOfPreviousException = getInt("fileDumpMaxRegroupingOfPreviousException", 500);
            useJstackForThreadDumps = getBoolean("useJstackForThreadDumps", false);

            urlRewriteSeoRulesEnabled = getBoolean("urlRewriteSeoRulesEnabled", false);

            disableJsessionIdParameter = getBoolean("disableJsessionIdParameter", true);
            jsessionIdParameterName = getString("jsessionIdParameterName", "jsessionid");

            guestUserResourceModuleName = getString("guestUserResourceModuleName");
            guestUserResourceKey = getString("guestUserResourceKey");

            guestGroupResourceModuleName = getString("guestGroupResourceModuleName");
            guestGroupResourceKey = getString("guestGroupResourceKey");
            
            fileServletStatisticsEnabled = getBoolean("jahia.fileServlet.statisticsEnabled", false);

            settings.put("userManagementUserNamePattern", getString(
                    "userManagementUserNamePattern", "[\\w\\{\\}\\-]+"));
            settings.put("userManagementGroupNamePattern", getString(
                    "userManagementGroupNamePattern", "[\\w\\{\\}\\-]+"));

            settings.put("default_templates_set",
                    getString("default_templates_set"));

            settings.put("templates.modules.onError", getString("templates.modules.onError", "compact"));
            
            settings.setFast(true);
            // If cluster is activated then try to expose some properties as system properties for JGroups
            boolean clusterActivated = getBoolean("cluster.activated",false);
            System.setProperty("cluster.activated", Boolean.toString(clusterActivated));
            if (System.getProperty("cluster.node.serverId") == null) {
            	System.setProperty("cluster.node.serverId", getString("cluster.node.serverId", "jahiaServer1"));
            }
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
            System.setProperty("jahia.jackrabbit.consistencyCheck", String.valueOf(getBoolean("jahia.jackrabbit.consistencyCheck", false)));
            System.setProperty("jahia.jackrabbit.consistencyFix", String.valueOf(getBoolean("jahia.jackrabbit.consistencyFix", false)));
            System.setProperty("jahia.jackrabbit.onWorkspaceInconsistency", getString("jahia.jackrabbit.onWorkspaceInconsistency", "log"));
            if (getBoolean("jahia.jackrabbit.reindexOnStartup", false)) {
                Resource repoHome = applicationContext.getResource(getString(
                        "jahia.jackrabbit.home", "WEB-INF/var/repository"));
                try {
                    if (repoHome.exists() && repoHome.getFile() != null) {
                        JCRContentUtils.deleteJackrabbitIndexes(repoHome.getFile());
                    } else {
                        logger.warn("Unable to delete JCR repository index folders in home {}",
                                repoHome);
                    }
                } catch (IOException e) {
                    logger.error("Unable to delete JCR repository index folders in home "
                            + repoHome, e);
                }
            }
        } catch (NullPointerException npe) {
            logger.error("Properties file is not valid...!", npe);
        } catch (NumberFormatException nfe) {
            logger.error("Properties file is not valid...!", nfe);
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


    public String getLicenseFileName () {
        return licenseFilename;
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

    public boolean isDevelopmentMode() {
        return !productionMode;
    }

    /**
     * to get the Site errors page behavior
     * @return a boolean
     */
    public boolean getSiteErrorEnabled() {
        return isSiteErrorEnabled;
    }

    public boolean isWrapperBufferFlushingActivated() {
        return wrapperBufferFlushingActivated;
    }

    public void setWrapperBufferFlushingActivated(boolean wrapperBufferFlushingActivated) {
        this.wrapperBufferFlushingActivated = wrapperBufferFlushingActivated;
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

    public boolean isDumpErrorsToFiles() {
        return dumpErrorsToFiles;
    }

    public int getFileDumpMaxRegroupingOfPreviousException() {
        return fileDumpMaxRegroupingOfPreviousException;
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

	public boolean isMaintenanceMode() {
		return maintenanceMode;
	}

    public boolean isDisableJsessionIdParameter() {
        return disableJsessionIdParameter;
    }

    public void setDisableJsessionIdParameter(boolean disableJsessionIdParameter) {
        this.disableJsessionIdParameter = disableJsessionIdParameter;
    }

    public String getJsessionIdParameterName() {
        return jsessionIdParameterName;
    }

    public void setJsessionIdParameterName(String jsessionIdParameterName) {
        this.jsessionIdParameterName = jsessionIdParameterName;
    }

    public String getGuestUserResourceKey() {
        return guestUserResourceKey;
    }

    public String getGuestUserResourceModuleName() {
        return guestUserResourceModuleName;
    }

    public String getGuestGroupResourceModuleName() {
        return guestGroupResourceModuleName;
    }

    public void setGuestGroupResourceModuleName(String guestGroupResourceModuleName) {
        this.guestGroupResourceModuleName = guestGroupResourceModuleName;
    }

    public String getGuestGroupResourceKey() {
        return guestGroupResourceKey;
    }

    public void setGuestGroupResourceKey(String guestGroupResourceKey) {
        this.guestGroupResourceKey = guestGroupResourceKey;
    }

    /**
     * Convert a String starting with the word "$context" into a real filesystem
     * path. This method is principally used by JahiaPrivateSettings and to
     * convert jahia.properties settings.
     *
     * @param convert      The string to convert.
     * @param pathResolver The path resolver used to get the real path.
     * @author Alexandre Kraft
     */
    public static String convertContexted(String convert,
                                          PathResolver pathResolver) {
        if (convert.startsWith("$context/")) {
            convert = pathResolver.resolvePath(convert.substring(8, convert.length()));
        }
        return convert;
    } // end convertContexted

    /**
     * Convert a string starting with the word "$webContext" into a real
     * filesystem path.
     *
     * @param convert the string to convert
     * @return converted string
     */
    public static String convertWebContexted(String convert) {
        return convert.startsWith("$webContext/") ? Jahia.getContextPath()
                + convert.substring("$webContext".length(), convert.length())
                : convert;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
    /**
     * Used to get the templates context.
     *
     * @return  The templates context.
     */
    public String getTemplatesContext() {
        return "/modules/";
    }

    public Resource getLicenseFile() {
        return licenseFile;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (licenseFileLocations != null) {
            for (String location : licenseFileLocations) {
                String path = location.trim();
                if ("file:/".equals(path)) {
                    continue;
                }
                try {
                    for (Resource resource : applicationContext.getResources(path)) {
                        if (resource.exists()) {
                            licenseFile = resource;
                            break;
                        }
                    }
                    if (licenseFile != null) {
                        break;
                    }
                } catch (IOException e) {
                    // ignore missing locations
                }
            }
        }
    }

    public String getOperatingMode() {
        return operatingMode;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public boolean isDistantPublicationServerMode() {
        return distantPublicationServerMode;
    }

    public boolean isUseJstackForThreadDumps() {
        return useJstackForThreadDumps;
    }

    public boolean isUrlRewriteSeoRulesEnabled() {
        return urlRewriteSeoRulesEnabled;
    }

    public boolean isFileServletStatisticsEnabled() {
        return fileServletStatisticsEnabled;
    }
}