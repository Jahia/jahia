/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
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
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.tools.patches.Patcher;
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
import java.util.*;

import static org.jahia.bin.listeners.JahiaContextLoaderListener.setSystemProperty;
import static org.jahia.settings.StartupOptions.*;

public class SettingsBean implements ServletContextAware, InitializingBean, ApplicationContextAware, ReadOnlyModeCapable, org.jahia.api.settings.SettingsBean {

    public static final String JAHIA_PROPERTIES_FILE_PATH = "/WEB-INF/etc/config/jahia.properties";
    /**
     * @deprecated Use {@link #isStartupOptionSet(String)} instead
     */
    @Deprecated
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
    private String buildNumber; // this is the famous build number...
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
    private int nodesCachePerSessionMaxSize = 100;
    private int queryApproxCountLimit;
    private boolean readOnlyMode;
    private DataSource dataSource;
    private ClusterSettingsInitializer clusterSettingsInitializer;
    private String internetExplorerCompatibility;
    private boolean clusterActivated;
    private boolean isMavenExecutableSet;
    private String[] authorizedRedirectHosts;
    private boolean useWebsockets = false;
    private String atmosphereAsyncSupport;
    private boolean areaAutoActivated;
    private int jahiaSiteImportScannerInterval;
    private long dbJournalJanitorBatchLimit;
    private int dbJournalJanitorHourOfDay;
    private int maxSearchLimit;

    // Timeout (in seconds) waiting for a bean to be available when using SpringContextSingleton.
    // Mostly used during startup when a module needs to access beans from another module starting independently.
    // 0 (or any negative value) to not wait
    private int moduleSpringBeansWaitingTimeout;

    // this is the list of jahia.properties server disk path and context path values...
    private String server;
    private String serverVersion;
    private String serverHome;
    private String jahiaEtcDiskPath;
    private String jahiaVarDiskPath;
    private String jahiaWebAppsDeployerBaseURL;
    private String jahiaImportsDiskPath;
    private String jahiaExportsDiskPath;
    private String jahiaModulesDiskPath;
    private String modulesSourcesDiskPath;
    private String jahiaDatabaseScriptsPath;
    private String  jahiaGeneratedResourcesDiskPath;

    private int moduleStartLevel;

    private StartupOptions startupOptions;
    private Map<String, Set<String>> startupOptionsMapping;
    private long studioMaxDisplayableFileSize;

    private CookieAuthConfig cookieAuthConfig;

    private String atmosphereHeartbeatFrequency;
    private StringSubstitutor stringSubstitutor;

    /**
     * @param   pathResolver a path resolver used to locate files on the disk.
     * @param   propertiesFilename  The jahia.properties file complete path.
     * @param   licenseFilename the name of the license file.
     * @param   buildNumber The Jahia build number.
     */
    public SettingsBean(PathResolver pathResolver, String propertiesFilename, String licenseFilename, String buildNumber) {
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
     * Returns the directory where the automatic error reports are generated by Jahia.
     *
     * @return the directory where the automatic error reports are generated by Jahia
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

    /**
     * Returns the directory where the automatic or triggered thread dumps are generated by Jahia.
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

    @Override
    public String getJahiaDatabaseScriptsPath() {
        return jahiaDatabaseScriptsPath;
    }

    @Override
    public long getJahiaJCRUserCountLimit() {
        return jahiaJCRUserCountLimit;
    }

    public String getAtmosphereHeartbeatFrequency() {
        return atmosphereHeartbeatFrequency;
    }

    public void setJahiaJCRUserCountLimit(long jahiaJCRUserCountLimit) {
        this.jahiaJCRUserCountLimit = jahiaJCRUserCountLimit;
    }

    /**
     * This method load and convert properties from the jahia.properties file,
     * and set some variables used by the SettingsBean class.
     */
    @Override
    public void load() {

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

            studioMaxDisplayableFileSize = getLong("studioMaxDisplayableFileSize", 1048576);

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

            isSiteErrorEnabled = getBoolean("site.error.enabled", false);

            operatingMode = getString("operatingMode", "development");
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

            expandImportedFilesOnDisk = getBoolean("expandImportedFilesOnDisk", false);
            expandImportedFilesOnDiskPath = getString("expandImportedFilesOnDiskPath", "/tmp");

            accessManagerPathPermissionCacheMaxSize = getInt("accessManagerPathPermissionCacheMaxSize", 100);

            nodesCachePerSessionMaxSize = getInt("jahia.jcr.nodesCachePerSessionMaxSize", 100);

            queryApproxCountLimit = getInt("queryApproxCountLimit", 100);

            readOnlyMode = getBoolean("readOnlyMode", false);

            internetExplorerCompatibility = getString("internetExplorerCompatibility", "IE=10");

            atmosphereAsyncSupport = getString("atmosphere.asyncSupport", null);

            useWebsockets = getBoolean("atmosphere.useWebsockets", false);

            areaAutoActivated = getBoolean("area.auto.activated", true);

            moduleSpringBeansWaitingTimeout = getInt("jahia.moduleSpringBeansWaitingTimeout", 5 * 60);

            moduleStartLevel = getInt("jahia.moduleStartLevel", 90);

            jahiaSiteImportScannerInterval = getInt("jahia.site.import.scanner.interval", 1000);

            atmosphereHeartbeatFrequency = getString("jahia.atmosphere.heartbeat", "60");

            dbJournalJanitorBatchLimit = getLong("jahia.jackrabbit.dbJournal.janitorBatchLimit", 10000L);
            dbJournalJanitorHourOfDay = getInt("jahia.jackrabbit.dbJournal.janitorHourOfDay", 3);
            maxSearchLimit = getInt("search.maxLimit", 5000);

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
            setSystemProperty("processingServer", Boolean.toString(isProcessingServer));
            if (System.getProperty("cluster.node.serverId") == null) {
                setSystemProperty("cluster.node.serverId", getString("cluster.node.serverId", "jahiaServer1"));
            }

            DatabaseUtils.setDatasource(dataSource);

            initJcrSystemProperties();

            initStartupOptions();

            if (clusterActivated) {
                clusterSettingsInitializer.initClusterSettings(this);
            }

            checkIndexConsistencyIfNeeded();

            reindexIfNeeded();

            readTldConfigJarsToSkip();

            initJerichoLogging();

            initDatabaseIfNeeded();

            if (isProcessingServer()) {
                Patcher.getInstance().executeScripts("contextInitializing");
            }

            // Init String substitutor

            Map<String, StringLookup> l = new HashMap<>();
            l.put("jahia", this::getPropertyValue);
            stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.interpolatorStringLookup(l, null, true));
            stringSubstitutor.setEnableSubstitutionInVariables(true);
        } catch (NullPointerException | NumberFormatException e) {
            logger.error("Properties file is not valid...!", e);
        }
    }

    public String replaceBySubsitutor(String source) {
        return stringSubstitutor.replace(source);
    }

    private void initDatabaseIfNeeded() throws JahiaRuntimeException {
        if (DatabaseUtils.isDatabaseStructureInitialized()) {
            logger.info("Database structure is initialized");
        } else {
            if (isProcessingServer() && getBoolean("db.init.auto", false)) {
                logger.info("Database structure is not initialized. Initalizing...");
                try {
                    DatabaseUtils.initializeDatabaseStructure(getJahiaVarDiskPath(), applicationContext);
                } catch (Exception e) {
                    logger.error("Error initializing database structure", e);
                    throw new JahiaRuntimeException("Error initializing database structure", e);
                }
                logger.info("Finished initializing database structure");
            } else {
                logger.error("Database structure is not initialized. Leaving...");
                throw new JahiaRuntimeException("Database structure is not initialized");
            }
        }
    }

    private void initStartupOptions() {
        startupOptions = new StartupOptions(this, startupOptionsMapping);
    }

    @Override
    public int getModuleStartLevel() {
        return moduleStartLevel;
    }

    /**
     * Initializes the JerichoHTML parser logging.
     */
    private void initJerichoLogging() {
        // if logging for Jericho is not explicitly enabled, we disable it by default
        if (!getBoolean("jahia.jericho.logging.enabled", false)) {
            Config.LoggerProvider = LoggerProvider.DISABLED;
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

            setJackrabbitBundleCacheSize("jahia.jackrabbit.bundleCacheSize.workspace", null);

            // if size for default workspace is not defined explicitly we take the "global" value for workspace, if defined
            setJackrabbitBundleCacheSize("jahia.jackrabbit.bundleCacheSize.workspace.default",
                    properties.getProperty("jahia.jackrabbit.bundleCacheSize.workspace"));

            // if size for live workspace is not defined explicitly we take the "global" value for workspace, if defined
            setJackrabbitBundleCacheSize("jahia.jackrabbit.bundleCacheSize.workspace.live",
                    properties.getProperty("jahia.jackrabbit.bundleCacheSize.workspace"));

            setJackrabbitBundleCacheSize("jahia.jackrabbit.bundleCacheSize.versioning", null);
        } catch (IOException e) {
            logger.error("Unable to determine JCR repository home", e);
        }
    }

    private void setJackrabbitBundleCacheSize(String key, String defaultValue) {
        if (System.getProperty(key) == null && properties.getProperty(key, defaultValue) != null) {
            setSystemProperty(key, properties.getProperty(key, defaultValue));
        }
    }

    public void initPaths() {

        classDiskPath = pathResolver.resolvePath ("/WEB-INF/classes/");

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
        jahiaExportsDiskPath = new File(convertContexted(getString("jahiaExportsDiskPath"), pathResolver)).getAbsolutePath();
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
            if (!servletContext.getServerInfo().toLowerCase().contains("tomcat")) {
                logger.warn("Unable to auto-detect server type, based on the server info '{}'. Assuming Apache Tomcat.", servletContext.getServerInfo());
            }
            server = "tomcat";
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
    @Override
    public boolean isClusterActivated() {
        return clusterActivated;
    }

    private void readTldConfigJarsToSkip() {
        File cfgDir = new File(jahiaEtcDiskPath, "config");
        if (!cfgDir.isDirectory()) {
            return;
        }
        Set<File> cfgs = new LinkedHashSet<>();
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

    @Override
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
        if (isStartupOptionSet(OPTION_INDEX_CHECK)) {
            setSystemProperty("jahia.jackrabbit.searchIndex.enableConsistencyCheck", "true");
            setSystemProperty("jahia.jackrabbit.searchIndex.forceConsistencyCheck", "true");
            setSystemProperty("jahia.jackrabbit.searchIndex.autoRepair", "false");
        }
        if (isStartupOptionSet(OPTION_INDEX_FIX)) {
            setSystemProperty("jahia.jackrabbit.searchIndex.enableConsistencyCheck", "true");
            setSystemProperty("jahia.jackrabbit.searchIndex.forceConsistencyCheck", "true");
            setSystemProperty("jahia.jackrabbit.searchIndex.autoRepair", "true");
        }
    }

    private void reindexIfNeeded() {
        File repoHome = null;
        try {
            repoHome = getRepositoryHome();
            if (repoHome != null) {
                if (getBoolean("jahia.jackrabbit.reindexOnStartup", false) || isStartupOptionSet(OPTION_REINDEX)) {
                    JCRContentUtils.deleteJackrabbitIndexes(repoHome);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to delete JCR repository index folders in home " + repoHome, e);
        }
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public boolean getBoolean(String propertyName, boolean defaultValue) {
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

    @Override
    public String getString(String propertyName) throws NoSuchElementException {
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

    @Override
    public String getString(String propertyName, String defaultValue) {
        String result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            result = curProperty.trim();
        }
        return result;
    }

    @Override
    public int getInt(String propertyName, int defaultValue) {
        int result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Integer.parseInt(curProperty);
        }
        return result;
    }

    @Override
    public long getLong(String propertyName, long defaultValue) {
        long result = defaultValue;
        String curProperty = getPropertyValue(propertyName);
        if (curProperty != null) {
            curProperty = curProperty.trim();
            result = Long.parseLong(curProperty);
        }
        return result;
    }

    @Override
    public String getPropertyValue(String propertyName) {
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
    @Override
    public String lookupString(String key) {
        Object param = settings.get (key);
        if (param instanceof String) {
            return (String) param;
        }
        return null;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>boolean</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>false</code> when the
     *               parameter could not be found.
     */
    @Override
    public boolean lookupBoolean(String key) {
        Object param = settings.get (key);
        if (param instanceof Boolean) {
            return ((Boolean) param).booleanValue();
        }
        return false;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    @Override
    public long lookupLong(String key) {
        Object param = settings.get (key);
        if (param instanceof Long) {
            return ((Long) param).longValue();
        }
        return Long.MIN_VALUE;
    }


    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    @Override
    public int lookupInt(String key) {
        Object param = settings.get (key);
        if (param instanceof Integer) {
            return ((Integer) param).intValue();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Get the principal properties object.
     *
     * @return  Properties object containing all properties from jahia.properties file.
     */
    @Override
    public Properties getPropertiesFile() {
        return this.properties;
    }


    @Override
    public String getLicenseFileName() {
        return licenseFilename;
    }

    /**
     * Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    */
    @Override
    public boolean isUseRelativeSiteURLs() {
        return useRelativeSiteURLs;
    }

    public void setUseRelativeSiteURLs(boolean val) {
        this.useRelativeSiteURLs = val;
    }

    @Override
    public String getJahiaWebAppsDeployerBaseURL() {
        return jahiaWebAppsDeployerBaseURL;
    }

    @Override
    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    /**
     * Used to get the build number.
     *
     * @return  The build number.
     */
    @Override
    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * Used to get the server name (tomcat, orion, etc).
     *
     * @return  The server name.
     */
    @Override
    public String getServer() {
        return server;
    }

    /**
     * Used to get the server home filesystem disk path.
     *
     * @return  The server home filesystem disk path.
     */
    @Override
    public String getServerHome() {
        return serverHome;
    }

    /**
     * Used to get the jahiafiles /etc disk path.
     *
     * @return  The jahiafiles /etc disk path.
     */
    @Override
    public String getJahiaEtcDiskPath() {
        return jahiaEtcDiskPath;
    }

    /**
     * Used to get the jahiafiles /var disk path.
     *
     * @return  The jahiafiles /var disk path.
     */
    @Override
    public String getJahiaVarDiskPath() {
        return jahiaVarDiskPath;
    }

    /**
     * Used to get the shared templates disk path.
     *
     * @return  The shared templates disk path.
     */
    @Override
    public String getJahiaModulesDiskPath() {
        return jahiaModulesDiskPath;
    }

    /**
     * @return The generated resources disk path.
     */
    @Override
    public String getJahiaGeneratedResourcesDiskPath() {
        return jahiaGeneratedResourcesDiskPath;
    }

    @Override
    public String getClassDiskPath() {
        return classDiskPath;
    }

    @Override
    public long getJahiaFileUploadMaxSize() {
        return jahiaFileUploadMaxSize;
    }
    /**
     * @deprecated since 7.0.0.2
     */
    @Override
    @Deprecated
    public String getJahiaHomeDiskPath() {
        return servletContext.getRealPath("./");
    }

    @Override
    public String getJahiaImportsDiskPath() {
        return jahiaImportsDiskPath;
    }

    @Override
    public String getJahiaExportsDiskPath() {
        return jahiaExportsDiskPath;
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public PathResolver getPathResolver() {
        return pathResolver;
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public String getTmpContentDiskPath() {
        return tmpContentDiskPath;
    }

    @Override
    public String getModulesSourcesDiskPath() {
        return modulesSourcesDiskPath;
    }

    @Override
    public boolean isProcessingServer() {
        return isProcessingServer;
    }

    @Override
    public int getSiteURLPortOverride() {
        return siteURLPortOverride;
    }

    @Override
    public void setSiteURLPortOverride(int siteURLPortOverride) {
        this.siteURLPortOverride = siteURLPortOverride;
    }

    public int getMail_maxRegroupingOfPreviousException() {
        return mail_maxRegroupingOfPreviousException;
    }

    public void setMail_maxRegroupingOfPreviousException(int mail_maxRegroupingOfPreviousException) {
        this.mail_maxRegroupingOfPreviousException = mail_maxRegroupingOfPreviousException;
    }

    @Override
    public boolean isDevelopmentMode() {
        return !productionMode;
    }

    /**
     * to get the Site errors page behavior
     * @return a boolean
     */
    @Override
    public boolean getSiteErrorEnabled() {
        return isSiteErrorEnabled;
    }

    @Override
    public boolean isConsiderDefaultJVMLocale() {
        return considerDefaultJVMLocale;
    }

    public void setConsiderDefaultJVMLocale(boolean considerDefaultJVMLocale) {
        this.considerDefaultJVMLocale = considerDefaultJVMLocale;
    }

    @Override
    public boolean isConsiderPreferredLanguageAfterLogin() {
        return considerPreferredLanguageAfterLogin;
    }

    public void setConsiderPreferredLanguageAfterLogin(
            boolean considerPreferredLanguageAfterLogin) {
        this.considerPreferredLanguageAfterLogin = considerPreferredLanguageAfterLogin;
    }

    @Override
    public boolean isPermanentMoveForVanityURL() {
        return permanentMoveForVanityURL;
    }

    @Override
    public boolean isDumpErrorsToFiles() {
        return dumpErrorsToFiles;
    }

    public void setDumpErrorsToFiles(boolean dumpErrorsToFiles) {
        this.dumpErrorsToFiles = dumpErrorsToFiles;
    }

    @Override
    public int getFileDumpMaxRegroupingOfPreviousException() {
        return fileDumpMaxRegroupingOfPreviousException;
    }

    /**
     * @return the serverVersion
     */
    @Override
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * @return the serverDeployer
     */
    @Override
    public ServerDeploymentInterface getServerDeployer() {
        return serverDeployer;
    }

    @Override
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    @Override
    public int getSessionExpiryTime() {
        return sessionExpiryTime;
    }

    public void setSessionExpiryTime(int sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }

    @Override
    public boolean isDisableJsessionIdParameter() {
        return disableJsessionIdParameter;
    }

    public void setDisableJsessionIdParameter(boolean disableJsessionIdParameter) {
        this.disableJsessionIdParameter = disableJsessionIdParameter;
    }

    @Override
    public String getJsessionIdParameterName() {
        return jsessionIdParameterName;
    }

    public void setJsessionIdParameterName(String jsessionIdParameterName) {
        this.jsessionIdParameterName = jsessionIdParameterName;
    }

    @Override
    public String getGuestUserResourceKey() {
        return guestUserResourceKey;
    }

    @Override
    public String getGuestUserResourceModuleName() {
        return guestUserResourceModuleName;
    }

    @Override
    public String getGuestGroupResourceModuleName() {
        return guestGroupResourceModuleName;
    }

    public void setGuestGroupResourceModuleName(String guestGroupResourceModuleName) {
        this.guestGroupResourceModuleName = guestGroupResourceModuleName;
    }

    @Override
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

    @Override
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
    @Override
    public String getTemplatesContext() {
        return "/modules/";
    }

    public Resource getLicenseFile() {
        return licenseFile;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
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

    @Override
    public void switchReadOnlyMode(boolean enable) {
        setReadOnlyMode(enable);
    }

    @Override
    public int getReadOnlyModePriority() {
        return 1000;
    }

    @Override
    public String getOperatingMode() {
        return operatingMode;
    }

    @Override
    public boolean isProductionMode() {
        return productionMode;
    }

    @Override
    public boolean isDistantPublicationServerMode() {
        return distantPublicationServerMode;
    }

    @Override
    public boolean isUseJstackForThreadDumps() {
        return useJstackForThreadDumps;
    }

    @Override
    public boolean isUrlRewriteSeoRulesEnabled() {
        return urlRewriteSeoRulesEnabled;
    }

    @Override
    public boolean isFileServletStatisticsEnabled() {
        return fileServletStatisticsEnabled;
    }

    @Override
    public boolean isUrlRewriteUseAbsoluteUrls() {
        return urlRewriteUseAbsoluteUrls;
    }

    @Override
    public boolean isUrlRewriteRemoveCmsPrefix() {
        return urlRewriteRemoveCmsPrefix;
    }

    @Override
    public int getImportMaxBatch() {
        return importMaxBatch;
    }

    @Override
    public int getMaxNameSize() {
        return maxNameSize;
    }

    @Override
    public boolean isExpandImportedFilesOnDisk() {
        return expandImportedFilesOnDisk;
    }

    @Override
    public String getExpandImportedFilesOnDiskPath() {
        return expandImportedFilesOnDiskPath;
    }

    @Override
    public int getAccessManagerPathPermissionCacheMaxSize() {
        return accessManagerPathPermissionCacheMaxSize;
    }

    @Override
    public int getNodesCachePerSessionMaxSize() {
        return nodesCachePerSessionMaxSize;
    }

    public void setNodesCachePerSessionMaxSize(int nodesCachePerSessionMaxSize) {
        this.nodesCachePerSessionMaxSize = nodesCachePerSessionMaxSize;
    }

    @Override
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
    @Override
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    /**
     * Returns <code>true</code> if this Jahia instance operates in "full-read-only" mode, i.e. access to the edit/studio/administration modes and
     * saving in the JCR are not allowed.
     *
     * @return <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes and
     * saving in the JCR are not allowed.; otherwise returns <code>false</code>
     */
    @Override
    public boolean isFullReadOnlyMode() {
        return ReadOnlyModeController.getInstance().getReadOnlyStatus() != ReadOnlyModeController.ReadOnlyModeStatus.OFF;
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

    public void setClusterSettingsInitializer(ClusterSettingsInitializer clusterSettingsInitializer) {
        this.clusterSettingsInitializer = clusterSettingsInitializer;
    }

    @Override
    public String getInternetExplorerCompatibility() {
        return internetExplorerCompatibility;
    }

    /**
     * @return true if maven is available
     */
    @Override
    public boolean isMavenExecutableSet() {
        return isMavenExecutableSet;
    }

    public void setMavenExecutableSet(boolean isMavenExecutableSet) {
        this.isMavenExecutableSet = isMavenExecutableSet;
    }

    @Override
    public String[] getAuthorizedRedirectHosts() {
        return authorizedRedirectHosts;
    }

    @Override
    public boolean isUseWebsockets() {
        return useWebsockets;
    }

    public void setUseWebsockets(boolean useWebsockets) {
        this.useWebsockets = useWebsockets;
    }

    @Override
    public String getAtmosphereAsyncSupport() {
        return atmosphereAsyncSupport;
    }

    public void setAtmosphereAsyncSupport(String atmosphereAsyncSupport) {
        this.atmosphereAsyncSupport = atmosphereAsyncSupport;
    }

    @Override
    public boolean isAreaAutoActivated() {
        return areaAutoActivated;
    }

    @Override
    public int getModuleSpringBeansWaitingTimeout() {
        return moduleSpringBeansWaitingTimeout;
    }

    public void setModuleSpringBeansWaitingTimeout(int moduleSpringBeansWaitingTimeout) {
        this.moduleSpringBeansWaitingTimeout = moduleSpringBeansWaitingTimeout;
    }

    public void setLicenseFile(Resource licenseFile) {
        this.licenseFile = licenseFile;
    }

    /**
     * Returns the startup options, which are set.
     *
     * @return the startup options, which are set
     */
    @Override
    public StartupOptions getStartupOptions() {
        return startupOptions;
    }

    /**
     * Checks if the specified startup option is set.
     *
     * @param option the option key
     * @return <code>true</code> if the specified option is set; <code>false</code> otherwise
     */
    @Override
    public boolean isStartupOptionSet(String option) {
        return startupOptions.isSet(option);
    }

    public void setStartupOptionsMapping(Map<String, Set<String>> startupOptionsMapping) {
        this.startupOptionsMapping = startupOptionsMapping;
    }

    @Override
    public int getJahiaSiteImportScannerInterval() { return jahiaSiteImportScannerInterval; }

    @Override
    public long getStudioMaxDisplayableFileSize() {
        return studioMaxDisplayableFileSize;
    }

    @Override
    public CookieAuthConfig getCookieAuthConfig() {
        return cookieAuthConfig;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public long getDbJournalJanitorBatchLimit() {
        return dbJournalJanitorBatchLimit;
    }

    public int getDbJournalJanitorHourOfDay() {
        return dbJournalJanitorHourOfDay;
    }

    public int getMaxSearchLimit() {
        return maxSearchLimit;
    }
}
