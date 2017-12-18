/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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

import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.LoggerProvider;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaSearchManager;
import org.apache.jackrabbit.core.query.lucene.JahiaSearchIndex;
import org.apache.jackrabbit.core.query.lucene.join.QueryEngine;
import org.apache.jackrabbit.core.stats.StatManager;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.configuration.deployers.ServerDeploymentFactory;
import org.jahia.configuration.deployers.ServerDeploymentInterface;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.tools.patches.GroovyPatcher;
import org.jahia.tools.patches.SqlPatcher;
import org.jahia.utils.DatabaseUtils;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.PathResolver;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.utils.zip.ZipEntryCharsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.*;

import static org.jahia.bin.listeners.JahiaContextLoaderListener.setSystemProperty;

public class SettingsBean implements ServletContextAware, InitializingBean, ApplicationContextAware {

    public static final String JAHIA_PROPERTIES_FILE_PATH = "/WEB-INF/etc/config/jahia.properties";
    private static final String JAHIA_BACKUP_RESTORE_MARKER = "backup-restore";
    public static final String JAHIA_BACKUP_RESTORE_SYSTEM_PROP = "jahia.backup-restore";
    private static final Logger logger = LoggerFactory.getLogger(SettingsBean.class);

    private static SettingsBean instance = null;
    private static volatile File errorDir;
    private static volatile File threadDir;
    private static volatile File heapDir;

    private final FastHashMap settings = new FastHashMap(); // The map holding all the settings.
    private PathResolver pathResolver = null;
    private String licenseFilename;
    private String propertiesFileName;
    private int buildNumber; // this is the famous build number...
    private Properties properties;
    private String classDiskPath;
    private long jahiaFileUploadMaxSize; // this is the list of jahia.properties files values...
    private boolean useRelativeSiteURLs; // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    private String defaultLanguageCode; // Default language code for multi-language system
    private long jahiaJCRUserCountLimit = -1; // limit for reading JCR users (in administration)
    private int mail_maxRegroupingOfPreviousException = 500;
    private String characterEncoding;
    private String tmpContentDiskPath;
    private boolean isProcessingServer;
    private int siteURLPortOverride = -1;
    private boolean isSiteErrorEnabled;
    private String operatingMode = "development";
    private String autoStartNewModuleVersion = "auto";
    private boolean productionMode = false;
    private boolean distantPublicationServerMode = true;
    private boolean considerPreferredLanguageAfterLogin;
    private boolean considerDefaultJVMLocale;
    private boolean permanentMoveForVanityURL = true;
    private boolean dumpErrorsToFiles = true;
    private int fileDumpMaxRegroupingOfPreviousException = 500;
    private boolean useJstackForThreadDumps;
    private boolean urlRewriteRemoveCmsPrefix;
    private boolean urlRewriteSeoRulesEnabled;
    private boolean urlRewriteUseAbsoluteUrls;
    private ServerDeploymentInterface serverDeployer = null;
    private boolean maintenanceMode;
    private int sessionExpiryTime;
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
    private Locale defaultLocale;
    private int importMaxBatch;
    private int maxNameSize;
    private boolean expandImportedFilesOnDisk;
    private String expandImportedFilesOnDiskPath;
    private int accessManagerPathPermissionCacheMaxSize = 100;
    private int queryApproxCountLimit;
    private boolean readOnlyMode;
    private DataSource dataSource;
    private String internetExplorerCompatibility;
    private boolean clusterActivated;
    private boolean isMavenExecutableSet;
    private String[] authorizedRedirectHosts;
    private boolean useWebsockets = false;
    private String atmosphereAsyncSupport;

    // this is the list of jahia.properties server disk path and context path values...
    private String server;
    private String serverVersion;
    private String serverHome;
    private String jahiaEtcDiskPath;
    private String jahiaVarDiskPath;
    private String jahiaGeneratedResourcesDiskPath;
    private String jahiaWebAppsDeployerBaseURL;
    private String jahiaImportsDiskPath;
    private String jahiaModulesDiskPath;
    private String modulesSourcesDiskPath;
    private String jahiaDatabaseScriptsPath;

    /**
     * @param   pathResolver a path resolver used to locate files on the disk.
     * @param   propertiesFilename  The jahia.properties file complete path.
     * @param   licenseFilename the name of the license file.
     * @param   buildNumber The Jahia build number.
     */
    public SettingsBean(PathResolver pathResolver, String propertiesFilename, String licenseFilename, int buildNumber) {
        this.pathResolver = pathResolver;
        this.propertiesFileName = propertiesFilename;
        this.buildNumber = buildNumber;
        this.licenseFilename = licenseFilename;
        instance = this;

    }

    public SettingsBean(PathResolver pathResolver, Properties props, List<String> licenseFileLocations) {
        this.pathResolver = pathResolver;
        this.properties = new Properties();
        properties.putAll(props);
        this.licenseFileLocations = licenseFileLocations;
        instance = this;
    }

    public static SettingsBean getInstance() {
        return instance;
    }

    private static String ensureEndSlash(String path, boolean needsEndSlash) {
        char lastChar = path.charAt(path.length() - 1);
        if (lastChar == '/' || lastChar == '\\') {
            if (!needsEndSlash) {
                path = path.length() > 1 ? path.substring(0, path.length() - 1)
                        : "";
            }
        } else if (needsEndSlash) {
            path += File.separatorChar;
        }
        return path;
    }

    /**
     * Returns the directory where the automatic error reports are generated by Digital Experience Manager.
     *
     * @return the directory where the automatic error reports are generated by Digital Experience Manager
     */
    public static File getErrorDir() {
        if (errorDir == null) {
            synchronized (SettingsBean.class) {
                if (errorDir == null) {
                    errorDir = getDirectory(new String[] {"jahia.error.dir", "jahia.log.dir", "java.io.tmpdir"}, "jahia-errors");
                }
            }
        }
        return errorDir;
    }

    private static String detectIpAddress() {
        InetAddress address = null;

        try {
            Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
            while (intfs.hasMoreElements() && address == null) {
                NetworkInterface intf = intfs.nextElement();
                try {
                    if (!intf.isUp()) {
                        continue;
                    }
                    InetAddress addr = null;
                    Enumeration<InetAddress> inetAddresses = intf.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        addr = inetAddresses.nextElement();
                        if ((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) {
                            address = addr;
                            break;
                        }
                    }
                } catch (SocketException e) {
                    // ignore
                }
            }
        } catch (SocketException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Unable to detect the network non-loobback address.", e);
            } else {
                logger.warn("Unable to detect the network non-loobback address. Cause: " + e.getMessage());
            }
        }

        return address != null ? address.getHostAddress() : null;
    }

    /**
     * Returns the directory where the automatic or triggered thread dumps are generated by Digital Experience Manager.
     *
     * @return the directory where the automatic or triggered thread dumps are generated by Digital Experience Manager
     */
    public static File getThreadDir() {
        if (threadDir == null) {
            synchronized (SettingsBean.class) {
                if (threadDir == null) {
                    threadDir = getDirectory(new String[] {"jahia.thread.dir", "jahia.log.dir", "java.io.tmpdir"}, "jahia-threads");
                }
            }
        }
        return threadDir;
    }

    /**
     * Returns the directory where the triggered heap dumps are generated by Digital Experience Manager.
     *
     * @return the directory where the triggered heap dumps are generated by Digital Experience Manager
     */
    public static File getHeapDir() {
        if (heapDir == null) {
            synchronized (SettingsBean.class) {
                if (heapDir == null) {
                    heapDir = getDirectory(new String[] {"jahia.heap.dir", "jahia.log.dir", "java.io.tmpdir"}, "jahia-heaps");
                }
            }
        }

        return heapDir;
    }

    private static File getDirectory(String[] parentDirectorySystemProperties, String childDirectoryName) {
        for (String parentDirectorySystemProperty : parentDirectorySystemProperties) {
            String parentDirectoryName = System.getProperty(parentDirectorySystemProperty);
            if (StringUtils.isNotEmpty(parentDirectoryName)) {
                return new File(parentDirectoryName, childDirectoryName);
            }
        }
        return new File(childDirectoryName);
    }

    private String interpolate(String source) {
        return source != null && source.length() > 0
                && source.contains(SystemPropertyUtils.PLACEHOLDER_PREFIX) ? SystemPropertyUtils
                .resolvePlaceholders(source, true) : source;
    }

    public String getJahiaDatabaseScriptsPath() {
        return jahiaDatabaseScriptsPath;
    }

    public long getJahiaJCRUserCountLimit() {
        return jahiaJCRUserCountLimit;
    }

    public void setJahiaJCRUserCountLimit(long jahiaJCRUserCountLimit) {
        this.jahiaJCRUserCountLimit = jahiaJCRUserCountLimit;
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
            detectServer();

            // disk path, url's and context...
            maintenanceMode = getBoolean("maintenanceMode", false);

            sessionExpiryTime = getInt("sessionExpiryTime", 60);

            initPaths();

            // files...
            jahiaFileUploadMaxSize = getLong("jahiaFileUploadMaxSize", 104857600);

            characterEncoding = getString("characterEncoding", "UTF-8");

            if (System.getProperty(ZipEntryCharsetDetector.ZIP_ENTRY_ALTERNATIVE_ENCODING) == null) {
                String zipEntryCharsets = getString(ZipEntryCharsetDetector.ZIP_ENTRY_ALTERNATIVE_ENCODING, null);
                if (StringUtils.isNotEmpty(zipEntryCharsets)) {
                    System.setProperty(ZipEntryCharsetDetector.ZIP_ENTRY_ALTERNATIVE_ENCODING, zipEntryCharsets);
                }
            }

            // Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
            useRelativeSiteURLs = getBoolean ("useRelativeSiteURLs", false);

            jahiaJCRUserCountLimit = getLong ("jahiaJCRUserCountLimit", -1);

            // base URL (schema, host, port) to call the web apps deployer service.
            jahiaWebAppsDeployerBaseURL = getString ("jahiaWebAppsDeployerBaseURL", "http://127.0.0.1:8080/manager");

            // multi language default language code property.
            defaultLanguageCode = getString ("org.jahia.multilang.default_language_code", "en");
            defaultLocale = LanguageCodeConverters.languageCodeToLocale(defaultLanguageCode);

            considerDefaultJVMLocale = getBoolean("considerDefaultJVMLocale", false);

            considerPreferredLanguageAfterLogin = getBoolean("considerPreferredLanguageAfterLogin", false);

            // mail notification settings...
            mail_maxRegroupingOfPreviousException = getInt("mail_maxRegroupingOfPreviousException", 500);

            isProcessingServer = getBoolean("processingServer", true);

            siteURLPortOverride = getInt("siteURLPortOverride", 0);

            isSiteErrorEnabled = getBoolean("site.error.enabled",false);

            operatingMode = getString("operatingMode", "development");
            autoStartNewModuleVersion = getString("autoStartNewModuleVersion", "auto");
            productionMode = !"development".equalsIgnoreCase(operatingMode);
            distantPublicationServerMode = "distantPublicationServer".equalsIgnoreCase(operatingMode);

            permanentMoveForVanityURL = getBoolean("permanentMoveForVanityURL", true);

            dumpErrorsToFiles = getBoolean("dumpErrorsToFiles", true);
            ErrorFileDumper.setFileDumpActivated(dumpErrorsToFiles);
            fileDumpMaxRegroupingOfPreviousException = getInt("fileDumpMaxRegroupingOfPreviousException", 500);
            useJstackForThreadDumps = getBoolean("useJstackForThreadDumps", false);

            urlRewriteSeoRulesEnabled = getBoolean("urlRewriteSeoRulesEnabled", false);
            urlRewriteRemoveCmsPrefix = getBoolean("urlRewriteRemoveCmsPrefix", false);
            urlRewriteUseAbsoluteUrls = getBoolean("urlRewriteUseAbsoluteUrls", true);

            disableJsessionIdParameter = getBoolean("disableJsessionIdParameter", true);
            jsessionIdParameterName = getString("jsessionIdParameterName", "jsessionid");

            guestUserResourceModuleName = getString("guestUserResourceModuleName");
            guestUserResourceKey = getString("guestUserResourceKey");

            guestGroupResourceModuleName = getString("guestGroupResourceModuleName");
            guestGroupResourceKey = getString("guestGroupResourceKey");

            fileServletStatisticsEnabled = getBoolean("jahia.fileServlet.statisticsEnabled", false);

            importMaxBatch = getInt("importMaxBatch", 500);

            maxNameSize = getInt("jahia.jcr.maxNameSize", 32);

            expandImportedFilesOnDisk = getBoolean("expandImportedFilesOnDisk",false);
            expandImportedFilesOnDiskPath = getString("expandImportedFilesOnDiskPath","/tmp");

            accessManagerPathPermissionCacheMaxSize = getInt("accessManagerPathPermissionCacheMaxSize", 100);

            queryApproxCountLimit = getInt("queryApproxCountLimit", 100);

            readOnlyMode = getBoolean("readOnlyMode", false);

            internetExplorerCompatibility = getString("internetExplorerCompatibility", "IE=10");

            atmosphereAsyncSupport = getString("atmosphere.asyncSupport", null);

            useWebsockets = getBoolean("atmosphere.useWebsockets", false);

            String authorizedRedirectHostsStr = getString("authorizedRedirectHosts", null);
            authorizedRedirectHosts = StringUtils.isBlank(authorizedRedirectHostsStr) ? new String[0] : authorizedRedirectHostsStr.trim().split("\\s*,\\s*");

            settings.put("userManagementUserNamePattern", getString(
                    "userManagementUserNamePattern", "[\\w\\{\\}\\-]+"));
            settings.put("userManagementGroupNamePattern", getString(
                    "userManagementGroupNamePattern", "[\\w\\{\\}\\-]+"));

            settings.put("default_templates_set",
                    getString("default_templates_set"));

            settings.put("legacy.import.externalLink.internationalized", getBoolean("legacy.import.externalLink.internationalized", false));
            settings.put("legacy.import.externalLink.nodeType", getString("legacy.import.externalLink.nodeType", Constants.JAHIANT_EXTERNAL_PAGE_LINK));
            settings.put("legacy.import.externalLink.urlPropertyName", getString("legacy.import.externalLink.urlPropertyName", Constants.URL));

            settings.setFast(true);
            clusterActivated = getBoolean("cluster.activated", false);
            setSystemProperty("cluster.activated", Boolean.toString(clusterActivated));
            if (System.getProperty("cluster.node.serverId") == null) {
                setSystemProperty("cluster.node.serverId", getString("cluster.node.serverId", "jahiaServer1"));
            }

            DatabaseUtils.setDatasource(dataSource);

            checkSafeBackupRestore();

            if(clusterActivated) {
                initBindAddress();
                // Expose binding port: use also cluster.tcp.ehcache.jahia.port for backward compatibility with Jahia 6.6
                String bindPort = getString("cluster.tcp.bindPort", getString("cluster.tcp.ehcache.jahia.port", null));
                if (StringUtils.isNotEmpty(bindPort)) {
                    setSystemProperty("cluster.tcp.bindPort", bindPort);
                }
                setSystemProperty("cluster.configFile.jahia", getString("cluster.configFile.jahia", "tcp.xml"));
            }

            initJcrSystemProperties();

            checkIndexConsistencyIfNeeded();

            reindexIfNeeded();

            readTldConfigJarsToSkip();

            initJerichoLogging();

            if (isProcessingServer()) {
                SqlPatcher.apply(getJahiaVarDiskPath(), applicationContext);
                GroovyPatcher.executeScripts(servletContext, "contextInitializing");
            }
        } catch (NullPointerException npe) {
            logger.error("Properties file is not valid...!", npe);
        } catch (NumberFormatException nfe) {
            logger.error("Properties file is not valid...!", nfe);
        }
    } // end load

    /**
     * Initializes the JerichoHTML parser logging.
     */
    private void initJerichoLogging() {
        // if logging for Jericho is not explicitly enabled, we disable it by default
        if (!getBoolean("jahia.jericho.logging.enabled", false)) {
            Config.LoggerProvider = LoggerProvider.DISABLED;
        }
    }


    private void checkSafeBackupRestore() {
        File marker = new File(getJahiaVarDiskPath(), JAHIA_BACKUP_RESTORE_MARKER);
        if (marker.exists()) {
            setSystemProperty(JAHIA_BACKUP_RESTORE_SYSTEM_PROP, "true");

            try {
                if (clusterActivated) {
                    logger.info("Detected safe backup restore marker, cleaning database table [JGROUPSPING] ...");
                    try {
                        DatabaseUtils.executeStatements(Collections.singletonList("DELETE FROM JGROUPSPING"));
                        logger.info("Database table [JGROUPSPING] successfully cleaned");
                    } catch (SQLException e) {
                        logger.error("Unable to clean database table: JGROUPSPING", e);
                    }
                }
            } finally {
                // delete marker
                marker.delete();
            }
        }
    }

    private void initBindAddress() {
        // First expose tcp ip binding address: use also cluster.tcp.start.ip_address for backward compatibility with Jahia 6.6
        String bindAddress = getString("cluster.tcp.bindAddress", getString("cluster.tcp.start.ip_address", null));
        if (StringUtils.isEmpty(bindAddress)) {
            bindAddress = System.getProperty("jgroups.bind_addr");
            if (bindAddress != null) {
                logger.info("Using value, supplied via jgroups.bind_addr system property, for the bind address: {}",
                        bindAddress);
            } else {
                bindAddress = detectIpAddress();
                if (bindAddress != null) {
                    logger.info("Detected non-loopback network bind address: {}", bindAddress);
                } else {
                    logger.warn("Unable to detect non-loopback network bind address."
                            + " Please configure cluster.tcp.bindAddress in jahia.node.properties explicitly.");
                }
            }
        }
        if (bindAddress != null) {
            logger.info("Setting JGroups bind address to: {}", bindAddress);
            setSystemProperty("cluster.tcp.bindAddress", bindAddress);
            setSystemProperty("jgroups.bind_addr", bindAddress);
        }
    }

    private void initJcrSystemProperties() {
        setSystemProperty("jahia.jackrabbit.consistencyCheck", String.valueOf(getBoolean("jahia.jackrabbit.consistencyCheck", false)));
        setSystemProperty("jahia.jackrabbit.consistencyFix", String.valueOf(getBoolean("jahia.jackrabbit.consistencyFix", false)));
        setSystemProperty("jahia.jackrabbit.onWorkspaceInconsistency", getString("jahia.jackrabbit.onWorkspaceInconsistency", "log"));

        setSystemProperty("jahia.jackrabbit.searchIndex.enableConsistencyCheck", getString("jahia.jackrabbit.searchIndex.enableConsistencyCheck", "false"));
        setSystemProperty("jahia.jackrabbit.searchIndex.forceConsistencyCheck", getString("jahia.jackrabbit.searchIndex.forceConsistencyCheck", "false"));
        setSystemProperty("jahia.jackrabbit.searchIndex.autoRepair", getString("jahia.jackrabbit.searchIndex.autoRepair", "false"));
        setSystemProperty(JahiaSearchIndex.SKIP_VERSION_INDEX_SYSTEM_PROPERTY, getString(JahiaSearchIndex.SKIP_VERSION_INDEX_SYSTEM_PROPERTY, "true"));

        setSystemProperty(QueryEngine.NATIVE_SORT_SYSTEM_PROPERTY, getString("jahia.jackrabbit.useNativeSort", "true"));

        setSystemProperty(StatManager.QUERY_STATS_ENABLED_PROPERTY, getString("jahia.jackrabbit.queryStatsEnabled", "true"));
        
        setSystemProperty(JahiaSearchManager.INDEX_LOCK_TYPES_SYSTEM_PROPERTY, getString("jahia.jackrabbit.searchIndex.indexLockTypesProperty", "true"));
        
        setSystemProperty("jahia.jackrabbit.ismLocking", getString("jahia.jackrabbit.ismLocking", "org.apache.jackrabbit.core.state.DefaultISMLocking"));

        try {
            File repoHome = getRepositoryHome();
            if (System.getProperty("jahia.jackrabbit.datastore.path") == null) {
                String path = getString("jackrabbit.datastore.path", getString("jahia.jackrabbit.datastore.path", null));
                if (path != null) {
                    if (path.contains("${jahia.jackrabbit.home}")) {
                        path = StringUtils.replace(path, "${jahia.jackrabbit.home}", repoHome.getAbsolutePath());
                    }
                    path = new File(interpolate(path)).getCanonicalPath();
                } else {
                    path = new File(repoHome, "datastore").getAbsolutePath();
                }
                setSystemProperty("jahia.jackrabbit.datastore.path", path);
            }
            if (System.getProperty("jahia.jackrabbit.searchIndex.workspace.config") == null) {
                setSystemProperty(
                        "jahia.jackrabbit.searchIndex.workspace.config",
                        getClass().getResource("/jahia/indexing_configuration.xml") != null ? "/jahia/indexing_configuration.xml"
                                : new File(repoHome, "indexing_configuration.xml").getAbsolutePath());
            }
            if (System.getProperty("jahia.jackrabbit.searchIndex.versioning.config") == null) {
                setSystemProperty(
                        "jahia.jackrabbit.searchIndex.versioning.config",
                        getClass().getResource("/jahia/indexing_configuration_version.xml") != null ? "/jahia/indexing_configuration_version.xml"
                                : new File(repoHome, "indexing_configuration_version.xml").getAbsolutePath());
            }
            if (System.getProperty("org.apache.jackrabbit.server.remoting.davex.batchread-config") == null
                    && getClass().getResource("/jahia/batchread.properties") != null) {
                setSystemProperty("org.apache.jackrabbit.server.remoting.davex.batchread-config",
                        "/jahia/batchread.properties");
            }
            
            if (System.getProperty("jahia.jackrabbit.bundleCacheSize.workspace") == null
                    && properties.getProperty("jahia.jackrabbit.bundleCacheSize.workspace") != null) {
                setSystemProperty("jahia.jackrabbit.bundleCacheSize.workspace",
                        properties.getProperty("jahia.jackrabbit.bundleCacheSize.workspace"));
            }
            if (System.getProperty("jahia.jackrabbit.bundleCacheSize.versioning") == null
                    && properties.getProperty("jahia.jackrabbit.bundleCacheSize.versioning") != null) {
                setSystemProperty("jahia.jackrabbit.bundleCacheSize.versioning",
                        properties.getProperty("jahia.jackrabbit.bundleCacheSize.versioning"));
            }
        } catch (IOException e) {
            logger.error("Unable to determine JCR repository home", e);
        }
    }

    private void initPaths() {

        classDiskPath = pathResolver.resolvePath("/WEB-INF/classes/");

        String jahiaDataDir = System.getProperty("jahia.data.dir");
        if (jahiaDataDir != null && jahiaDataDir.length() > 0) {
            jahiaVarDiskPath = ensureEndSlash(interpolate(jahiaDataDir), true);
        } else {
            jahiaVarDiskPath = ensureEndSlash(convertContexted(getString("jahiaVarDiskPath"), pathResolver), false);
        }
        try {
            jahiaVarDiskPath = new File(jahiaVarDiskPath).getCanonicalPath();
        } catch (IOException e) {
            jahiaVarDiskPath = new File(jahiaVarDiskPath).getAbsolutePath();
        }
        setSystemProperty("jahia.data.dir", jahiaVarDiskPath);

        jahiaEtcDiskPath = new File(convertContexted(getString("jahiaEtcDiskPath", "$context/WEB-INF/etc/"), pathResolver)).getAbsolutePath();
        tmpContentDiskPath = new File(convertContexted(getString("tmpContentDiskPath"), pathResolver)).getAbsolutePath();
        try {
            File tmpContentDisk = new File(tmpContentDiskPath);
            if (!tmpContentDisk.exists()) {
                tmpContentDisk.mkdirs();
            }
        } catch (Exception e) {
            logger.error("Provided folder for tmpContentDiskPath is not valid. Cause: " + e.getMessage(), e);
        }
        jahiaImportsDiskPath = new File(convertContexted(getString("jahiaImportsDiskPath"), pathResolver)).getAbsolutePath();
        jahiaModulesDiskPath = new File(convertContexted(getString("jahiaModulesDiskPath"), pathResolver)).getAbsolutePath();
        jahiaDatabaseScriptsPath = jahiaVarDiskPath + File.separator + "db";
        modulesSourcesDiskPath = new File(convertContexted(getString("modulesSourcesDiskPath"), pathResolver)).getAbsolutePath();
        jahiaGeneratedResourcesDiskPath = new File(convertContexted(getString("jahiaGeneratedResourcesDiskPath"), pathResolver)).getAbsolutePath();
    }

    private void detectServer() {
        server = getString("server", "");
        serverVersion = getString("serverVersion", null);
        serverHome = getString("serverHome", "");

        if (server.length() == 0 && servletContext != null) {
            logger.info("Auto-detecting server type...");
            String info = StringUtils.defaultString(
                    servletContext.getServerInfo(), "tomcat").toLowerCase();
            if (info.contains("tomcat")) {
                server = "tomcat";
            } else if (info.contains("jboss")) {
                server = "jboss";
            } else if (info.contains("websphere")) {
                server = "was";
            } else {
                server = "tomcat";
                logger.warn(
                        "Unable to auto-detect server type, based on the server info '{}'. Assuming Apache Tomcat.",
                        servletContext.getServerInfo());
            }
            logger.info("...detected server type is '{}'", server);
        }

        if (serverHome.length() == 0) {
            logger.info("Auto-detecting server home...");
            if ("tomcat".equals(server)) {
                String home = System.getProperty("catalina.home");
                if (StringUtils.isNotEmpty(home)) {
                    serverHome = home;
                } else {
                    File war = new File(
                            JahiaContextLoaderListener.getWebAppRoot());
                    File webapps = war.getParentFile();
                    if (webapps != null && webapps.getName().equals("webapps")) {
                        File tomcat = webapps.getParentFile();
                        if (tomcat != null
                                && new File(tomcat, "lib/catalina.jar")
                                        .isFile()) {
                            serverHome = tomcat.getAbsolutePath();
                        }
                    }
                }
            } else if ("jboss".equals(server)) {
                File war = new File(JahiaContextLoaderListener.getWebAppRoot());
                File ear = war.getParentFile();
                if (ear != null) {
                    File deploymentsFolder = ear.getParentFile();
                    if (deploymentsFolder != null) {
                        if ("deployments".equals(deploymentsFolder.getName())) {
                            // exploded EAR deployment on JBoss
                            File standaloneFolder = deploymentsFolder.getParentFile();
                            if (standaloneFolder != null) {
                                File jboss = standaloneFolder.getParentFile();
                                if (jboss != null && jboss.isDirectory()) {
                                    serverHome = jboss.getAbsolutePath();
                                }
                            }
                        } else if ("deployment".equals(deploymentsFolder.getName())) {
                            // packaged EAR deployment on JBoss
                            File vfsFolder = deploymentsFolder.getParentFile();
                            if (vfsFolder != null && "vfs".equals(vfsFolder.getName())) {
                                File standaloneFolder = vfsFolder.getParentFile() != null ? vfsFolder.getParentFile()
                                        .getParentFile() : null;
                                if (standaloneFolder != null) {
                                    File jboss = standaloneFolder.getParentFile();
                                    if (jboss != null && jboss.isDirectory()) {
                                        serverHome = jboss.getAbsolutePath();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (StringUtils.isEmpty(serverHome)) {
                // fallback to WAR folder
                serverHome = JahiaContextLoaderListener.getWebAppRoot();
            }

            logger.info("...detected server home is '{}'", serverHome);
        }
        if (!StringUtils.isEmpty(server) && !StringUtils.isEmpty(serverHome)) {
            serverDeployer = ServerDeploymentFactory.getImplementation(server,
                    serverVersion, new File(serverHome).getAbsoluteFile(),
                    null, null);
        }
    }

    /**
     * Returns <code>true</code> if the clustering is activated.
     *
     * @return <code>true</code> if the clustering is activated; <code>false</code> otherwise
     */
    public boolean isClusterActivated() {
        return clusterActivated;
    }

    private void readTldConfigJarsToSkip() {
        File cfgDir = new File(jahiaEtcDiskPath, "config");
        if (!cfgDir.isDirectory()) {
            return;
        }
        List<File> cfgs = new LinkedList<File>();
        File main = new File(cfgDir, "jar-scanner.conf");
        if (main.isFile()) {
            cfgs.add(main);
        }
        File[] secondary = cfgDir.listFiles((FileFilter) new WildcardFileFilter("jar-scanner.*.conf"));
        if (secondary != null && secondary.length > 0) {
            cfgs.addAll(Arrays.asList(secondary));
        }
        Set<String> jarsToSkip = new TreeSet<String>();
        try {
            for (File cfg : cfgs) {
                jarsToSkip.addAll(FileUtils.readLines(cfg));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        jarsToSkip.remove("");
        if (!jarsToSkip.isEmpty()) {
            setSystemProperty("org.jahia.TldConfig.jarsToSkip", StringUtils.join(jarsToSkip, ','));
            logger.info(
                    "Set system property (org.jahia.TldConfig.jarsToSkip) for JARs to be skipped during TLD search, including {} JARs",
                    jarsToSkip.size());
            if (logger.isDebugEnabled()) {
                logger.debug("org.jahia.TldConfig.jarsToSkip: {}", System.getProperty("org.jahia.TldConfig.jarsToSkip"));
            }
        }
    }

    public File getRepositoryHome() throws IOException {
        String path = getString("jahia.jackrabbit.home", null);
        if (path == null) {
            path = interpolate("${jahia.data.dir}/repository");
        }
        File repoHome = new File(path);
        if (!repoHome.isAbsolute()) {
            Resource r = applicationContext.getResource(path);
            if (r != null && r.exists()) {
                repoHome = r.getFile();
            }
        }
        return repoHome.exists() ? repoHome.getAbsoluteFile() : null;
    }

    public Resource getRepositoryHomeResource() throws IOException {
        return new FileSystemResource(getRepositoryHome());
    }

    private void checkIndexConsistencyIfNeeded() {
        File repoHome = null;

        try {
            repoHome = getRepositoryHome();
            if (repoHome != null) {
                File check = new File(repoHome, "index-check");
                File repair = new File(repoHome, "index-fix");
                if (check.exists()) {
                    setSystemProperty("jahia.jackrabbit.searchIndex.enableConsistencyCheck", "true");
                    setSystemProperty("jahia.jackrabbit.searchIndex.forceConsistencyCheck", "true");
                    setSystemProperty("jahia.jackrabbit.searchIndex.autoRepair", "false");
                    FileUtils.deleteQuietly(check);
                }
                if (repair.exists()) {
                    setSystemProperty("jahia.jackrabbit.searchIndex.enableConsistencyCheck", "true");
                    setSystemProperty("jahia.jackrabbit.searchIndex.forceConsistencyCheck", "true");
                    setSystemProperty("jahia.jackrabbit.searchIndex.autoRepair", "true");
                    FileUtils.deleteQuietly(repair);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to delete JCR repository index folders in home " + repoHome, e);
        }
    }

    private void reindexIfNeeded() {
        File repoHome = null;

        try {
            repoHome = getRepositoryHome();
            if (repoHome != null) {
                boolean doReindex = getBoolean("jahia.jackrabbit.reindexOnStartup", false);
                if (!doReindex) {
                    File reindex = new File(repoHome, "reindex");
                    if (reindex.exists()) {
                        doReindex = true;
                        FileUtils.deleteQuietly(reindex);
                    }
                }
                if (doReindex) {
                    JCRContentUtils.deleteJackrabbitIndexes(repoHome);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to delete JCR repository index folders in home " + repoHome, e);
        }
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    private boolean getBoolean (String propertyName, boolean defaultValue) {
        boolean result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            if (!"".equals(curProperty)) {
                result = Boolean.valueOf(curProperty).booleanValue();
            }
        }
        return result;
    }

    private String getString (String propertyName) throws NoSuchElementException {
        String result;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            result = curProperty.trim();
            return result;
        } else {
            throw new NoSuchElementException("No String found for property : " +
                                             propertyName);
        }
    }

    private String getString(String propertyName, String defaultValue) {
        String result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            result = curProperty.trim();
        }
        return result;
    }

    private int getInt(String propertyName, int defaultValue) {
        int result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Integer.parseInt(curProperty);
        }
        return result;
    }

    private long getLong(String propertyName, long defaultValue) {
        long result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Long.parseLong(curProperty);
        }
        return result;
    }

    private String getPropertyValue(String propertyName) {
        String result = properties.getProperty(propertyName);
        if (result != null && result.length() > 0 && result.contains(SystemPropertyUtils.PLACEHOLDER_PREFIX)) {
            result = SystemPropertyUtils.resolvePlaceholders(result, true);
        }

        return result;
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
     * @return The generated resources disk path.
     */
    public String getJahiaGeneratedResourcesDiskPath() {
        return jahiaGeneratedResourcesDiskPath;
    }

    /**
     * Used to get the shared templates disk path.
     *
     * @return  The shared templates disk path.
     */
    public String getJahiaModulesDiskPath() {
        return jahiaModulesDiskPath;
    }


    public String getClassDiskPath() {
        return classDiskPath;
    }
    public long getJahiaFileUploadMaxSize() {
        return jahiaFileUploadMaxSize;
    }
    /**
     * @deprecated since 7.0.0.2
     */
    @Deprecated
    public String getJahiaHomeDiskPath() {
        return servletContext.getRealPath("./");
    }

    public String getJahiaImportsDiskPath() {
        return jahiaImportsDiskPath;
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

    public String getModulesSourcesDiskPath() {
        return modulesSourcesDiskPath;
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

    public boolean isPermanentMoveForVanityURL() {
        return permanentMoveForVanityURL;
    }

    public boolean isDumpErrorsToFiles() {
        return dumpErrorsToFiles;
    }

    public void setDumpErrorsToFiles(boolean dumpErrorsToFiles) {
        this.dumpErrorsToFiles = dumpErrorsToFiles;
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

    public int getSessionExpiryTime() {
        return sessionExpiryTime;
    }

    public void setSessionExpiryTime(int sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
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
    }

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
        // init build number
        setBuildNumber(Jahia.getBuildNumber());

        // set maintenance mode state
        Jahia.setMaintenance(isMaintenanceMode());

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

    public String getAutoStartNewModuleVersion() {
        return autoStartNewModuleVersion;
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

    public boolean isUrlRewriteUseAbsoluteUrls() {
        return urlRewriteUseAbsoluteUrls;
    }

    public boolean isUrlRewriteRemoveCmsPrefix() {
        return urlRewriteRemoveCmsPrefix;
    }

    public int getImportMaxBatch() {
        return importMaxBatch;
    }

    public int getMaxNameSize() {
        return maxNameSize;
    }

    public boolean isExpandImportedFilesOnDisk() {
        return expandImportedFilesOnDisk;
    }

    public String getExpandImportedFilesOnDiskPath() {
        return expandImportedFilesOnDiskPath;
    }

    public int getAccessManagerPathPermissionCacheMaxSize() {
        return accessManagerPathPermissionCacheMaxSize;
    }

    public int getQueryApproxCountLimit() {
        return queryApproxCountLimit;
    }

    /**
     * Returns <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes is
     * not allowed.
     *
     * @return <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes is
     *         not allowed; otherwise returns <code>false</code>
     */
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    /**
     * If set to <code>true</code>, access to the edit/studio/administration modes is disabled on this Jahia instance.
     *
     * @param readOnlyMode
     *            set to <code>true</code> to disable access to the edit/studio/administration modes on this Jahia instance
     */
    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getInternetExplorerCompatibility() {
        return internetExplorerCompatibility;
    }

    /**
     * @return true if maven is available
     */
    public boolean isMavenExecutableSet() {
        return isMavenExecutableSet;
    }

    public void setMavenExecutableSet(boolean isMavenExecutableSet) {
        this.isMavenExecutableSet = isMavenExecutableSet;
    }

    public String[] getAuthorizedRedirectHosts() {
        return authorizedRedirectHosts;
    }

    public boolean isUseWebsockets() {
        return useWebsockets;
    }

    public void setUseWebsockets(boolean useWebsockets) {
        this.useWebsockets = useWebsockets;
    }

    public String getAtmosphereAsyncSupport() {
        return atmosphereAsyncSupport;
    }

    public void setAtmosphereAsyncSupport(String atmosphereAsyncSupport) {
        this.atmosphereAsyncSupport = atmosphereAsyncSupport;
    }
}