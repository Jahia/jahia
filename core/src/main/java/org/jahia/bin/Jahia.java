/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.commons.Version;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(Jahia.class);

    static private final String INIT_PARAM_SUPPORTED_JDK_VERSIONS =
        "supported_jdk_versions";

    static public final String COPYRIGHT =
            "&copy; Copyright 2002-2013  <a href=\"http://www.jahia.com\" target=\"newJahia\">Jahia Solutions Group SA</a> -";

    public final static String COPYRIGHT_TXT = "2013 Jahia Solutions Group SA" ;

    static private boolean maintenance = false;

    private static SettingsBean jSettings;

    static private String jahiaServletPath;
    static private String jahiaContextPath;

    private static int BUILD_NUMBER = -1;
    private static int EE_BUILD_NUMBER = -1;
    
    private static String EDITION;

    private static final Version JAHIA_VERSION = new Version(Constants.JAHIA_PROJECT_VERSION);

    /** Jahia server release number */
    private static double RELEASE_NUMBER = -1.0;

    public final static String VERSION = JAHIA_VERSION.getMajorVersion() + "." +
            JAHIA_VERSION.getMinorVersion() + "." +
            JAHIA_VERSION.getServicePackVersion() + "." +
            JAHIA_VERSION.getPatchVersion();

    private static final int SERVICEPACK_NUMBER = JAHIA_VERSION.getServicePackVersion();

    /** Jahia server patch number */
    private static final int PATCH_NUMBER = JAHIA_VERSION.getPatchVersion();
    

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

    public static int getEEBuildNumber() {
        if (EE_BUILD_NUMBER == -1) {
            try {
                InputStream in = Jahia.class.getResourceAsStream("/META-INF/jahia-ee-impl-marker.txt");
                if (in != null) {
                    try {
                        String buildNumber = IOUtils.toString(in);
                        EE_BUILD_NUMBER = Integer.parseInt(buildNumber);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                } else {
                    EE_BUILD_NUMBER = 0;
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                EE_BUILD_NUMBER = 0;
            } catch (NumberFormatException nfe) {
                logger.error(nfe.getMessage(), nfe);
                EE_BUILD_NUMBER = 0;
            }
        }

        return EE_BUILD_NUMBER;
    }

    public static double getReleaseNumber() {
        if (RELEASE_NUMBER == -1.0) {
            String releaseNumberStr = JAHIA_VERSION.getMajorVersion() + "." + JAHIA_VERSION.getMinorVersion();
            try {
                RELEASE_NUMBER = Double.parseDouble(releaseNumberStr);
            } catch (NumberFormatException nfe) {
                RELEASE_NUMBER = 0.0;
            }
        }
        return RELEASE_NUMBER;
    }

    public static String getLicenseText() {
        InputStream in = null;
        String txt;
        try {
            in = new FileInputStream(new File(SettingsBean.getInstance().getJahiaHomeDiskPath() + "/LICENSE"));
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
        return PATCH_NUMBER;
    }

    public static int getServicePackNumber() {
        return SERVICEPACK_NUMBER;
    }


    public static void verifyJavaVersion(String supportedJDKVersions) throws JahiaInitializationException {
        if (supportedJDKVersions != null) {
                Version currentJDKVersion;
                try {
                    currentJDKVersion = new Version(System.getProperty("java.version"));
                    if (!isSupportedJDKVersion(currentJDKVersion, supportedJDKVersions)) {
                        StringBuffer jemsg = new StringBuffer();
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
        if (jahiaContextPath == null) {
            initContextData();
        }
        return jahiaContextPath;
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

    public static void initContextData() {
        String ctxPath = ""; // assume ROOT context by default
        ctxPath = SettingsBean.getInstance().getPropertiesFile().getProperty("jahia.contextPath");
        if (ctxPath == null || ctxPath.length() > 0 && !ctxPath.startsWith("/")) {
            logger.error("Invalid value for the jahia.contextPath in the "
                    + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + " resource. Unable to initialize Web application.");
            throw new IllegalArgumentException("Invalid value for the jahia.contextPath in the "
                    + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + " resource. Unable to initialize Web application.");
        }

        Jahia.jahiaContextPath = ctxPath.equals("/") ? "" : ctxPath;
    }

    public static String getEdition() {
        if (EDITION == null) {
            EDITION = Jahia.class.getResource("/META-INF/jahia-ee-impl-marker.txt") != null ? "EE"
                    : "CE";
        }

        return EDITION;
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
        StringBuilder version = new StringBuilder(32);

        version.append("Jahia ").append(Jahia.VERSION).append(" r").append(Jahia.getBuildNumber());
        if (isEnterpriseEdition()) {
            version.append(".").append(Jahia.getEEBuildNumber());
        }

        return version.toString();
    }
}