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
    public static final String MAX_USAGE_DAYS_LIMIT_NAME = "maxUsageDays";
    public static final String DATE_LIMIT_NAME = "date";

}
