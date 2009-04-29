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
package org.jahia.resourcebundle;

import java.util.Locale;
import java.util.MissingResourceException;
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

    /**
     * Returns the resource string.
     *
     * @param res
     * @param resName
     * @param defaultValue
     * @return String
     */
    public static String getString(final ResourceBundle res,
            final String resName, final String defaultValue) {
        String resource = null;
        try {
            resource = res.getString(resName);
        } catch (MissingResourceException ex) {
            resource = defaultValue;
        }
        return resource;
    }
    
}