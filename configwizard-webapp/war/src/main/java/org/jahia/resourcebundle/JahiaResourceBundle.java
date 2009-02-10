/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.resourcebundle;

import java.util.Locale;
import java.util.ResourceBundle;



/**
 * Tools to handles resource bundle within Jahia.
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaResourceBundle
{

    public static final String ENGINE_DEFAULT_RESOURCE_BUNDLE = "JahiaEnginesResources";
    public static final String ADMIN_DEFAULT_RESOURCE_BUNDLE = "JahiaAdministrationResources";
    public static final String MESSAGE_DEFAULT_RESOURCE_BUNDLE = "JahiaMessageResources";



    public static String getMessageResource( final String resourceName,
                                             final Locale locale ){

        final String resValue;

        if ( resourceName == null || resourceName.trim().equals("") )
            return null;

        final ResourceBundle res = ResourceBundle.getBundle(
                MESSAGE_DEFAULT_RESOURCE_BUNDLE,locale);

        if ( res != null ){
            resValue = getString(res, resourceName, locale);
            return resValue;
        } else {
            resValue = null;
        }

        logger.warn("Resource [" + resourceName +
                "] not found in message resource bundles using locale [" +
                locale + "]");
        return resValue;
    }


    public static String getMessageResource( final String resourceName,
                                             final Locale locale,
                                             final String fileName){

        final String resValue;

        if ( resourceName == null || resourceName.trim().equals("") )
            return null;

        final ResourceBundle res = ResourceBundle.getBundle(
                fileName, locale);

        if ( res != null ){
            resValue = getString(res, resourceName, locale);
            return resValue;
        } else {
            resValue = null;
        }

        logger.warn("Resource [" + resourceName +
                "] not found in message resource bundles using locale [" +
                locale + "]");
        return resValue;
    }







    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaResourceBundle.class);

    //--------------------------------------------------------------------------
    /**
     * Returns the resource string.
     * This is a convenience way to used such as Chinese to translate from encoding.
     *
     * @param res
     * @param resName
     * @param locale
     * @return String
     */
    public static String getString( final ResourceBundle res,
                                    final String resName, final Locale locale) {
        return res.getString(resName);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns Jahia admin' default resource bundle
     * This resource bundle's name is "JahiaAdministrationResources"
     *
     *
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the Jahia engines' default resource bundle or null if not found
     */
    public static ResourceBundle getAdminDefaultResourceBundle(Locale locale ){

        if ( locale == null ){
            locale= Locale.getDefault();
        }
        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle(ADMIN_DEFAULT_RESOURCE_BUNDLE,locale);
        } catch ( Throwable t ){
            logger.debug("Error while retrieving administration default resource bundle " +
                    ADMIN_DEFAULT_RESOURCE_BUNDLE + " with locale " + locale, t);
        }

        return res;
    }

}

