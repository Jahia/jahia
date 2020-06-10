package org.jahia.api.settings;

import org.jahia.configuration.deployers.ServerDeploymentInterface;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.settings.StartupOptions;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Commons settings
 */
public interface SettingsBean {

    String getJahiaDatabaseScriptsPath();

    long getJahiaJCRUserCountLimit();

    /**
     * This method load and convert properties from the jahia.properties file,
     * and set some variables used by the SettingsBean class.
     */
    void load();

    int getModuleStartLevel();

    /**
     * Returns <code>true</code> if the clustering is activated.
     *
     * @return <code>true</code> if the clustering is activated; <code>false</code> otherwise
     */
    boolean isClusterActivated();

    File getRepositoryHome() throws IOException;

    Locale getDefaultLocale();

    boolean getBoolean(String propertyName, boolean defaultValue);

    String getString(String propertyName) throws NoSuchElementException;

    String getString(String propertyName, String defaultValue);

    int getInt(String propertyName, int defaultValue);

    long getLong(String propertyName, long defaultValue);

    String getPropertyValue(String propertyName);

    /** Looks up the specified <code>key</code> parameter as a <code>String</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Returns <code>null</code> when the
     *               parameter could not be found.
     */
    String lookupString(String key);

    /** Looks up the specified <code>key</code> parameter as a <code>boolean</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>false</code> when the
     *               parameter could not be found.
     */
    boolean lookupBoolean(String key);

    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    long lookupLong(String key);

    /** Looks up the specified <code>key</code> parameter as a <code>long</code> result.
     *
     * @param key   the parameter key to lookup
     * @return      the requested parameter value. Return <code>Long.MIN_VALUE</code> when the
     *               parameter could not be found.
     */
    int lookupInt(String key);

    /**
     * Get the principal properties object.
     *
     * @return  Properties object containing all properties from jahia.properties file.
     */
    Properties getPropertiesFile();

    String getLicenseFileName();

    /**
     * Activation / deactivation of relative URLs, instead of absolute URLs, when generating URL to exit the Admin Menu for example
    */
    boolean isUseRelativeSiteURLs();

    String getJahiaWebAppsDeployerBaseURL();

    String getDefaultLanguageCode();

    /**
     * Used to get the build number.
     *
     * @return  The build number.
     */
    int getBuildNumber();

    /**
     * Used to get the server name (tomcat, orion, etc).
     *
     * @return  The server name.
     */
    String getServer();

    /**
     * Used to get the server home filesystem disk path.
     *
     * @return  The server home filesystem disk path.
     */
    String getServerHome();

    /**
     * Used to get the jahiafiles /etc disk path.
     *
     * @return  The jahiafiles /etc disk path.
     */
    String getJahiaEtcDiskPath();

    /**
     * Used to get the jahiafiles /var disk path.
     *
     * @return  The jahiafiles /var disk path.
     */
    String getJahiaVarDiskPath();

    /**
     * Used to get the shared templates disk path.
     *
     * @return  The shared templates disk path.
     */
    String getJahiaModulesDiskPath();

    /**
     * @return The generated resources disk path.
     */
    String getJahiaGeneratedResourcesDiskPath();

    String getClassDiskPath();

    long getJahiaFileUploadMaxSize();

    /**
     * @deprecated since 7.0.0.2
     */
    @Deprecated
    String getJahiaHomeDiskPath();

    String getJahiaImportsDiskPath();

    String getCharacterEncoding();

    String getTmpContentDiskPath();

    String getModulesSourcesDiskPath();

    boolean isProcessingServer();

    int getSiteURLPortOverride();

    void setSiteURLPortOverride(int siteURLPortOverride);

    boolean isDevelopmentMode();

    /**
     * to get the Site errors page behavior
     * @return a boolean
     */
    boolean getSiteErrorEnabled();

    boolean isConsiderDefaultJVMLocale();

    boolean isConsiderPreferredLanguageAfterLogin();

    boolean isPermanentMoveForVanityURL();

    boolean isDumpErrorsToFiles();

    int getFileDumpMaxRegroupingOfPreviousException();

    /**
     * @return the serverVersion
     */
    String getServerVersion();

    /**
     * @return the serverDeployer
     */
    ServerDeploymentInterface getServerDeployer();

    boolean isMaintenanceMode();

    int getSessionExpiryTime();

    boolean isDisableJsessionIdParameter();

    String getJsessionIdParameterName();

    String getGuestUserResourceKey();

    String getGuestUserResourceModuleName();

    String getGuestGroupResourceModuleName();

    String getGuestGroupResourceKey();

    /**
     * Used to get the templates context.
     *
     * @return  The templates context.
     */
    String getTemplatesContext();

    void switchReadOnlyMode(boolean enable);

    int getReadOnlyModePriority();

    String getOperatingMode();

    boolean isProductionMode();

    boolean isDistantPublicationServerMode();

    boolean isUseJstackForThreadDumps();

    boolean isUrlRewriteSeoRulesEnabled();

    boolean isFileServletStatisticsEnabled();

    boolean isUrlRewriteUseAbsoluteUrls();

    boolean isUrlRewriteRemoveCmsPrefix();

    int getImportMaxBatch();

    int getMaxNameSize();

    boolean isExpandImportedFilesOnDisk();

    String getExpandImportedFilesOnDiskPath();

    int getAccessManagerPathPermissionCacheMaxSize();

    int getQueryApproxCountLimit();

    /**
     * Returns <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes is
     * not allowed.
     *
     * @return <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes is
     *         not allowed; otherwise returns <code>false</code>
     */
    boolean isReadOnlyMode();

    /**
     * Returns <code>true</code> if this Jahia instance operates in "full-read-only" mode, i.e. access to the edit/studio/administration modes and
     * saving in the JCR are not allowed.
     *
     * @return <code>true</code> if this Jahia instance operates in "read-only" mode, i.e. access to the edit/studio/administration modes and
     * saving in the JCR are not allowed.; otherwise returns <code>false</code>
     */
    boolean isFullReadOnlyMode();

    String getInternetExplorerCompatibility();

    boolean isMavenExecutableSet();

    String[] getAuthorizedRedirectHosts();

    boolean isUseWebsockets();

    String getAtmosphereAsyncSupport();

    boolean isAreaAutoActivated();

    int getModuleSpringBeansWaitingTimeout();

    boolean isStartupOptionSet(String option);

    /**
     * Returns the startup options, which are set.
     *
     * @return the startup options, which are set
     */
    StartupOptions getStartupOptions();

    int getJahiaSiteImportScannerInterval();

    long getStudioMaxDisplayableFileSize();

    CookieAuthConfig getCookieAuthConfig();
}
