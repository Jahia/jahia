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
//  Jahia
//
//  30.10.2000  EV  added in jahia.
//  17.01.2001  AK  change dispatcher method.
//  19.01.2001  AK  replace methods doGet and doPost by the method service.
//  29.01.2001  AK  change re-init way, remove sets methods.
//  10.02.2001  AK  pseudo-bypass the login by forwarding request attributes.
//  27.03.2001  AK  javadoc and change the access to JahiaPrivateSettings.load().
//  28.03.2001  AK  add some jahia path variables.
//  29.03.2001  AK  rename jahia.basic file in jahia.properties.
//  20.04.2001  AK  bugfix request uri.
//  17.05.2001  AK  tomcat users check during init.
//  23.05.2001  NK  bug two same parameter in url resolved by removing pathinfo data from request uri
//

package org.jahia.bin;

import com.ibm.icu.text.DateFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.commons.Version;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Jahia version and support utilities.
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
public final class Jahia {

    public static final String YEAR = "2024";
    public static final String CODE_NAME = "Indigo";
    public static final String PRODUCT_NAME = "Jahia";
    public static final String VENDOR_NAME = "Jahia Solutions Group SA";
    public static final String COPYRIGHT = "&copy; Copyright 2002-" + YEAR + "  <a href=\"https://www.jahia.com\" target=\"newJahia\">"
            + VENDOR_NAME + "</a> -";
    public static final String COPYRIGHT_TXT = YEAR + " " + VENDOR_NAME;
    private static final Logger logger = LoggerFactory.getLogger(Jahia.class);

    private static final Version JAHIA_VERSION;
    public static final String LOGGER_INVALID_SUPPORTED_JDK_VERSIONS = "Invalid supported_jdk_versions initialization parameter in web.xml, it MUST be in the ";

    private static final DateTimeFormatter BUILD_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.of("UTC"));
    private static final String GIT_PROPERTIES = "git.properties";
    private static final String GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev";

    static {
        Version v = null;
        try {
            v = new Version(Constants.JAHIA_PROJECT_VERSION);
        } catch (NumberFormatException e) {
            logger.error("Version number is not correct", e);
        }
        JAHIA_VERSION = v != null ? v : new Version("8.0.0.0");
    }

    public static final String VERSION = JAHIA_VERSION.getMajorVersion() + "." +
            JAHIA_VERSION.getMinorVersion() + "." +
            JAHIA_VERSION.getServicePackVersion() + "." +
            JAHIA_VERSION.getPatchVersion();

    private static final String INIT_PARAM_SUPPORTED_JDK_VERSIONS = "supported_jdk_versions";

    private static String jahiaServletPath;
    private static String jahiaContextPath;
    private static boolean maintenance = false;
    private static volatile int eeBuildNumber = -1;
    private static volatile String edition;
    private static volatile String buildDate;

    public static String getBuildNumber() {
        Properties properties = new Properties();
        synchronized (Jahia.class) {
            try {
                properties.load(Jahia.class.getClassLoader().getResourceAsStream(GIT_PROPERTIES));
            } catch (IOException e) {
                logger.error("Properties file wasn't read properly", e);
            }
        }
        return properties.getProperty(GIT_COMMIT_ID_ABBREV);
    }

    public static String getBuildDate() {
        if (buildDate == null) {
            synchronized (Jahia.class) {
                if (buildDate == null) {
                    try {
                        URL urlToVersionMarker = Jahia.class.getResource("/META-INF/jahia-impl-marker.txt");
                        if (urlToVersionMarker != null) {
                            URLConnection conn = urlToVersionMarker.openConnection();
                            long lastModified = (conn instanceof JarURLConnection) ? ((JarURLConnection) conn).getJarEntry().getTime() : conn.getLastModified();
                            buildDate = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ENGLISH).format(new Date(lastModified));
                        } else {
                            buildDate = "";
                        }
                    } catch (IOException ioe) {
                        logger.error(ioe.getMessage(), ioe);
                        buildDate = "";
                    }
                }
            }
        }
        return buildDate;
    }

    public static int getEEBuildNumber() {
        if (eeBuildNumber == -1) {
            synchronized (Jahia.class) {
                if (eeBuildNumber == -1) {
                    eeBuildNumber = getBuildNumber("/META-INF/jahia-ee-impl-marker.txt");
                }
            }
        }
        return eeBuildNumber;
    }

    /**
     * This method return the build number
     *
     * @param markerFilePathName path to the marker file that will be used to calculate the build number
     * @return an integer representing the build number
     */
    public static int getBuildNumber(String markerFilePathName) {
        int buildNumber = 0;
        try (InputStream in = Jahia.class.getResourceAsStream(markerFilePathName)) {
            String number = IOUtils.toString(in, StandardCharsets.UTF_8);
            buildNumber = Integer.parseInt(number);
        } catch (IOException | NumberFormatException e) {
            logger.error(e.getMessage(), e);
        }
        return buildNumber;
    }

    public static String getReleaseNumber() {
        return (JAHIA_VERSION.getMajorVersion() + "." + JAHIA_VERSION.getMinorVersion());
    }

    public static String getLicenseText() {
        String txt;
        try (InputStream in = JahiaContextLoaderListener.getServletContext().getResourceAsStream("/LICENSE")) {
            txt = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            txt = "Unable to parse licence file";
        }
        return txt;
    }

    public static int getPatchNumber() {
        return JAHIA_VERSION.getPatchVersion();
    }

    public static int getServicePackNumber() {
        return JAHIA_VERSION.getServicePackVersion();
    }

    /**
     * This method check if the Java version used to run the application is supported
     *
     * @param supportedJDKVersions list as a string of the supported JDK versions
     * @throws JahiaInitializationException if the version used is not supported
     */
    public static void verifyJavaVersion(String supportedJDKVersions) throws JahiaInitializationException {
        if (supportedJDKVersions != null) {
                Version currentJDKVersion;
                try {
                    currentJDKVersion = new Version(System.getProperty("java.version"));
                    if (Arrays.stream(StringUtils.split(supportedJDKVersions,',')).noneMatch(v -> isSupportedJDKVersion(currentJDKVersion, v.trim()))) {
                        String jemsg = "WARNING\n\n" +
                                "You are using an unsupported JDK version\n" +
                                "or have an invalid " +
                                INIT_PARAM_SUPPORTED_JDK_VERSIONS +
                                " parameter string in \n" +
                                "the deployment descriptor file web.xml.\n" +
                                "\n\nHere is the range specified in the web.xml file : " +
                                supportedJDKVersions + ".\n" +
                                "\nIf you want to disable this warning, remove the " +
                                INIT_PARAM_SUPPORTED_JDK_VERSIONS + "\n" +
                                "\ninitialization parameter in the WEB-INF/web.xml\n\n" +
                                "\n\nPlease note that if you deactivate this check or use unsupported versions\n\n" +
                                "\nYou might run into serious problems and we cannot offer support for these.\n\n" +
                                "\nYou may download a supported JDK from Oracle site: http://www.oracle.com/technetwork/java/javase/downloads/index.html" +
                                "\n\n&nbsp;\n";
                        JahiaInitializationException e = new JahiaInitializationException(jemsg);
                        logger.error("Invalid JDK version", e);
                        throw e;
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Couldn't convert JDK version to internal version testing system, ignoring JDK version test...", nfe);
                }
            }
    }

    public static String getServletPath () {
        return jahiaServletPath;
    }

    public static String getContextPath () {
        return jahiaContextPath;
    }

    public static void setContextPath(String contextPath) {
        jahiaContextPath = contextPath;
    }

    /**
     * Return the private settings
     *
     * @return JahiaPrivateSettings
     * @deprecated use {@link SettingsBean#getInstance()} instead
     */
    @Deprecated
    public static SettingsBean getSettings () {
        return SettingsBean.getInstance();
    }

    public static boolean isMaintenance() {
        return maintenance || SettingsBean.getInstance().isMaintenanceMode();
    }

    public static void setMaintenance(boolean maintenance) {
        if (SettingsBean.getInstance().isMaintenanceMode()) {
            logger.info("Maintenance mode has been enforced in Jahia properties, impossible to change at runtime level.");
        }
        Jahia.maintenance = maintenance;
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
    private static boolean isSupportedJDKVersion (final Version currentJDKVersion,
                                           final String supportedJDKString) {
        if (supportedJDKString == null) {
            // we deactivate the check if we specify no supported JDKs
            return true;
        }

        final String workString = supportedJDKString.toLowerCase();
        int xPos = workString.indexOf('x');

        if (xPos == -1) {
            logger.debug("{} following format : 1.2 < x <= 1.3 (the 'x' character is mandatory and was missing in this case : [{}] )",
                    LOGGER_INVALID_SUPPORTED_JDK_VERSIONS, supportedJDKString);
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
            logger.error("{} following format : 1.2 < x <= 1.3. Current string : [{}] )", LOGGER_INVALID_SUPPORTED_JDK_VERSIONS, supportedJDKString);
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
            logger.error("{} following format : 1.2 < x <= 1.3. Current string : [{}] )", LOGGER_INVALID_SUPPORTED_JDK_VERSIONS, supportedJDKString);
            return false;
        }

        return true;
    }

    public static String getEdition() {
        if (edition == null) {
            synchronized (Jahia.class) {
                if (edition == null) {
                    edition = (Jahia.class.getResource("/META-INF/jahia-ee-impl-marker.txt") != null ? "EE" : "CE");
                }
            }
        }
        return edition;
    }

    public static boolean isEnterpriseEdition() {
        return "EE".equals(getEdition());
    }

    /**
     * Returns full product version string.
     *
     * @return full product version string
     */
    public static String getFullProductVersion() {
        StringBuilder version = new StringBuilder();

        if (Jahia.JAHIA_VERSION.toString().endsWith("SNAPSHOT")) {
            try {
                Properties properties = new Properties();
                properties.load(Jahia.class.getClassLoader().getResourceAsStream(GIT_PROPERTIES));
                version.append(Jahia.PRODUCT_NAME).append(" ").append(Jahia.VERSION)
                        .append(" [" + CODE_NAME + "] - Build: ")
                        .append(properties.getProperty(GIT_COMMIT_ID_ABBREV))
                        .append(" - Built on: ")
                        .append(ZonedDateTime.parse(properties.getProperty("git.build.time"), BUILD_DATE_PATTERN).format(UTC_FORMATTER));
            } catch (IOException e) {
                logger.error("Properties file wasn't read properly", e);
            }
        } else {
            version.append(Jahia.PRODUCT_NAME).append(" ").append(Jahia.VERSION);
        }
        return version.toString();
    }
}
