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
//
//  LicenseConstants
//
//  NK      11.06.2001
//
//

package org.jahia.security.license;

/**
 * LicenseConstants
 *
 * @author NK
 */

public final class LicenseConstants {

    /**
     * Constants defined for Jahia License
     *
     * @author Khue Nguyen
     */

    /** OpenJoda License Type **/
    public static final int OPENJODA_LICENSE_TYPE 		= 1;

    /** ProJoda License Type **/
    public static final int PROJODA_LICENSE_TYPE 		= 2;



    /** License Type Relational Comparator "<" **/
    public static final int RELCOMP_SMALLER 			= 1;

    /** License Type Relational Comparator "<=" **/
    public static final int RELCOMP_SMALLER_OR_EQUAL 	= 2;

    /** License Type Relational Comparator "=" **/
    public static final int RELCOMP_EQUAL 				= 3;

    /** License Type Relational Comparator ">=" **/
    public static final int RELCOMP_BIGGER_OR_EQUAL 	= 4;

    /** License Type Relational Comparator ">" **/
    public static final int RELCOMP_BIGGER				= 5;



    /** License Type parameter **/
    public static final String LICENSE_TYPE_OPTION				= "licenseType";

    /** License Type Relational Comparator **/
    public static final String LICENSE_TYPE_RELCOMP_OPTION		= "relComp";

    /** Identifier for a licenseID that disables check of hostname/host ip address */
    public static final String LICENSEID_NULL                   = "-1";

    public static final String CORE_COMPONENT = "org.jahia.core";

    public static final String JAHIA_PRODUCT_NAME = "Jahia";

    public static final String USER_LIMIT_NAME = "users";
    public static final String PAGE_LIMIT_NAME = "pages";
    public static final String SITE_LIMIT_NAME = "sites";
    public static final String TEMPLATE_LIMIT_NAME = "templates";
    public static final String BUILDNUMBER_LIMIT_NAME = "build";
    public static final String RELEASE_LIMIT_NAME = "release";

}
