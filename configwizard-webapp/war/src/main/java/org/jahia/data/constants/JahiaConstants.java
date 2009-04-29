/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.data.constants;

/**
 * constants
 *
 * @author NK
 */

public final class JahiaConstants {

    /**
     * DTD resources
     *
     */
    public static final String WEB_DTD_PUBLICID_22 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WEB_DTD_RESOURCE_PATH_22 =
        "xml_dtd/web_22.dtd";

    public static final String WEB_DTD_PUBLICID_23 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WEB_DTD_RESOURCE_PATH_23 =
        "xml_dtd/web_23.dtd";

    public static final String J2EE_APP_DTD_PUBLICID_12 =
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN";
    public static final String J2EE_APP_DTD_RESOURCE_PATH_12 =
        "xml_dtd/application_1_2.dtd";

    public static final String TEMPLATES_DESCRIPTOR_20_PATH = 
        "xml_dtd/templates_2_0.xsd";

    public static final String TEMPLATES_DESCRIPTOR_20_URI = "http://www.jahia.org/shemas/templates_2_0.xsd";

    /**
     * DTD URI
     */
    public static final String WEB_DTD_22_HREF =
        "http://java.sun.com/j2ee/dtds/web-app_2_3.dtd";

    public static final String WEB_DTD_23_HREF =
        "http://java.sun.com/j2ee/dtds/web-app_2_3.dtd";

    public static final String J2EE_APP_DTD_12_HREF =
        "http://java.sun.com/j2ee/dtds/application_1_2.dtd";



    /**
     * Server Types
     *
     */
    public static final String SERVER_TOMCAT = "Tomcat";

    public static final String SERVER_TOMCAT4_BETA1 = "4.0-b1";

    public static final String SERVER_TOMCAT4_BETA2 = "4.0-b2";

    public static final String SERVER_TOMCAT4_BETA3 = "4.0-b3";

    public static final String SERVER_TOMCAT4_BETA5 = "4.0-b5";

    public static final String SERVER_TOMCAT4_BETA6 = "4.0-b6";

    public static final String SERVER_TOMCAT4_BETA7 = "4.0-b7";

    public static final String SERVER_JBOSS = "JBoss";

    public static final String SERVER_WEBLOGIC = "Weblogic";

    /**
     * Server directory
     *
     */
    public static final String TOMCAT_CONFIG_DIR = "conf";


    /**
     * Jahia folders
     *
     */
    public static final String JAHIA_CONFIG_DIR = "config";


    public static final String NULL_STRING_MARKER = "<empty>";
}
