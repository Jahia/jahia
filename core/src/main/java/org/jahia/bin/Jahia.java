/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.commons.Version;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.DateFormat;

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

    public static final String CODE_NAME = "Firestar";
    public static final String COPYRIGHT = "&copy; Copyright 2002-2017  <a href=\"http://www.jahia.com\" target=\"newJahia\">Jahia Solutions Group SA</a> -";
    public static final String COPYRIGHT_TXT = "2017 Jahia Solutions Group SA" ;

    private static final Version JAHIA_VERSION;
    static {
        Version v = null;
        try {
            v = new Version(Constants.JAHIA_PROJECT_VERSION);
        } catch (NumberFormatException e) {
        }
        JAHIA_VERSION = v != null ? v : new Version("7.0.0.0");
    }

    public static final String VERSION = JAHIA_VERSION.getMajorVersion() + "." +
            JAHIA_VERSION.getMinorVersion() + "." +
            JAHIA_VERSION.getServicePackVersion() + "." +
            JAHIA_VERSION.getPatchVersion();

    private static final String INIT_PARAM_SUPPORTED_JDK_VERSIONS = "supported_jdk_versions";
    private static final Logger logger = LoggerFactory.getLogger(Jahia.class);

    private static String jahiaServletPath;
    private static String jahiaContextPath;
    private static boolean maintenance = false;
    private static volatile int buildNumber = -1;
    private static volatile int eeBuildNumber = -1;
    private static volatile String edition;
    private static volatile String buildDate;

    public static int getBuildNumber() {
        if (buildNumber == -1) {
            synchronized (Jahia.class) {
                if (buildNumber == -1) {
                    buildNumber = getBuildNumber("/META-INF/jahia-impl-marker.txt");
                }
            }
        }
        return buildNumber;
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

    public static int getBuildNumber(String markerFilePathName) {
        int buildNumber = 0;
        try {
            InputStream in = Jahia.class.getResourceAsStream(markerFilePathName);
            if (in != null) {
                try {
                    String number = IOUtils.toString(in);
                    buildNumber = Integer.parseInt(number);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (NumberFormatException nfe) {
            logger.warn(nfe.getMessage());
        }
        return buildNumber;
    }

    public static String getReleaseNumber() {
        return (JAHIA_VERSION.getMajorVersion() + "." + JAHIA_VERSION.getMinorVersion());
    }

    public static String getLicenseText() {
        InputStream in = null;
        String txt;
        try {
            in = JahiaContextLoaderListener.getServletContext().getResourceAsStream("/LICENSE");
            txt = IOUtils.toString(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            txt = "Unable to parse licence file";
        } finally {
            IOUtils.closeQuietly(in);
        }
        return txt;
    }

    public static int getPatchNumber() {
        return JAHIA_VERSION.getPatchVersion();
    }

    public static int getServicePackNumber() {
        return JAHIA_VERSION.getServicePackVersion();
    }

    public static void verifyJavaVersion(String supportedJDKVersions) throws JahiaInitializationException {
        if (supportedJDKVersions != null) {
                Version currentJDKVersion;
                try {
                    currentJDKVersion = new Version(System.getProperty("java.version"));
                    if (!isSupportedJDKVersion(currentJDKVersion, supportedJDKVersions)) {
                        StringBuilder jemsg = new StringBuilder(256);
                        jemsg.append("WARNING\n\n");
                        jemsg.append(
                            "You are using an unsupported JDK version\n");
                        jemsg.append("or have an invalid ").append(
                                     INIT_PARAM_SUPPORTED_JDK_VERSIONS).append(
                                     " parameter string in \n");
                        jemsg.append(
                            "the deployment descriptor file web.xml.\n");
                        jemsg.append(
                            "\n\nHere is the range specified in the web.xml file : ").
                                append(supportedJDKVersions).append(".\n");
                        jemsg.append(
                                "\nIf you want to disable this warning, remove the ");
                        jemsg.append(INIT_PARAM_SUPPORTED_JDK_VERSIONS);
                        jemsg.append("\n");
                        jemsg.append(
                            "\ninitialization parameter in the WEB-INF/web.xml\n\n");
                        jemsg.append("\n\nPlease note that if you deactivate this check or use unsupported versions\n\n");
                        jemsg.append("\nYou might run into serious problems and we cannot offer support for these.\n\n");
                        jemsg.append("\nYou may download a supported JDK from Oracle site: http://www.oracle.com/technetwork/java/javase/downloads/index.html");
                        jemsg.append("\n\n&nbsp;\n");
                        JahiaInitializationException e = new JahiaInitializationException(jemsg.toString());
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
        return maintenance;
    }

    public static void setMaintenance(boolean maintenance) {
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
        version.append("Digital Experience Manager ").append(Jahia.VERSION).append(" [" + CODE_NAME + "] - ")
                .append(isEnterpriseEdition() ? "Enterprise" : "Community").append(" Distribution - Build ")
                .append(Jahia.getBuildNumber());
        if (isEnterpriseEdition()) {
            version.append(".").append(Jahia.getEEBuildNumber());
        }
        return version.toString();
    }
}