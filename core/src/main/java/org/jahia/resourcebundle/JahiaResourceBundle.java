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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
// NK 18.02.2002 - added in Jahia
//

package org.jahia.resourcebundle;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

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


    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource.
     *
     * If the requested resource bundle is missing and useDefault is true,
     * Jahia will look for another engine resource bundle in that order :
     *
     *     *		1. 	Look for the engine resource bundle of the current user.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetUsrEngineResourceBundleTag,SetGrpEngineResourceBundleTag.
     *
     *     *		2. 	Look for the engine resource bundle of the page.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetEngineResourceBundleTag.
     *
     *     *		3.      Look for the site's default engine resource bundle.
     *                          Each site can have a default engine resource bundle. It's name
     *         			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
     *         			where MYJAHIASITE is the virtual site's sitekey in uppercase.
     *
     *     *		4.      Finally if none of the previous resource bundle are available,
     *         			Jahia will return the internal engine's default resource bundle
     *         			named "JahiaEnginesResources".
     *
     *
     * @param resourceName the resource name
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.EngineResourceBundleTag
     * @see org.jahia.taglibs.resourcebundle.SetEngineResourceBundleTag
     *
     */
    public static String getEngineResource( final String resourceName,
                                            final ProcessingContext jParams,
                                            final Locale locale ){
        return getEngineResource(resourceName,
                                 jParams,
                                 locale,
                                 null);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource.
     *
     * If the requested resource bundle is missing and useDefault is true,
     * Jahia will look for another engine resource bundle in that order :
     *
     *     *		1. 	Look for the engine resource bundle of the current user.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetUsrEngineResourceBundleTag,SetGrpEngineResourceBundleTag.
     *
     *     *		2. 	Look for the engine resource bundle of the page.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetEngineResourceBundleTag.
     *
     *     *		3.      Look for the site's default engine resource bundle.
     *                          Each site can have a default engine resource bundle. It's name
     *         			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
     *         			where MYJAHIASITE is the virtual site's sitekey in uppercase.
     *
     *     *		4.      Finally if none of the previous resource bundle are available,
     *         			Jahia will return the internal engine's default resource bundle
     *         			named "JahiaEnginesResources".
     *
     *
     * @param resourceName the resource name
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     * @param defaultValue
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.EngineResourceBundleTag
     * @see org.jahia.taglibs.resourcebundle.SetEngineResourceBundleTag
     *
     */
    public static String getEngineResource( final String resourceName,
                                            final ProcessingContext jParams,
                                            final Locale locale,
                                            final String defaultValue ){

        ResourceBundle res = null;
        String resValue = null;

        if ( resourceName == null || resourceName.trim().equals("") )
            return defaultValue;

        final Locale loc = checkLocale(locale,jParams);

        boolean adminMode = false;
        try {
            adminMode = jParams.isInAdminMode();
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
        }

        if ( !adminMode ) {
            // first look for user's engine resource bundle
            res = getGrpUsrEngineResourceBundle(jParams.getPageID(),jParams.getUser());
            if ( res != null ){
                try {
                    resValue = res.getString(resourceName);
                    return resValue;
                } catch ( Exception t ){
                }
            }

            // second look for page's engine resource bundle
            res = getPageEngineResourceBundle(jParams.getPageID(),jParams);
            if ( res != null ){
                try {
                    resValue = res.getString(resourceName);
                    return resValue;
                } catch ( Exception t ){
                }
            }

            // third look for site's engine resource bundle
            res = getSiteEngineResourceBundle(jParams.getSite(),jParams,loc);
            if ( res != null ){
                try {
                    resValue = getString(res, resourceName, loc);
                    return resValue;
                } catch ( Exception t ){
                }
            }
        }

        // fourth look for jahia's engine default resource bundle
        res = getEngineDefaultResourceBundle(jParams,loc);
        if ( res != null ){
            try {
                resValue = getString(res, resourceName, loc);
                if (resValue != null) {
                    return resValue;
                }
            } catch ( Exception t ){
            }
        }
        logger.warn("Resource [" + resourceName +
                    "] not found in engine resource bundles using locale [" +
                    locale + "]");
        return defaultValue;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource for Administration Templates.
     *
     * If the requested resource bundle is missing and useDefault is true,
     * Jahia will look for another resource bundle in that order :
     *
     *     *		1.      Look for the site's default engine resource bundle.
     *                          Each site can have a default engine resource bundle. It's name
     *         			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
     *         			where MYJAHIASITE is the virtual site's sitekey in uppercase.
     *
     *     *		2.      Finally if none of the previous resource bundle are available,
     *         			Jahia will return the internal engine's default resource bundle
     *         			named "JahiaAdministrationResources".
     *
     *
     * @param resourceName the resource name
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.AdminResourceBundleTag
     *
     */
    public static String getAdminResource( final String resourceName,
                                           final ProcessingContext jParams,
                                           final Locale locale ){

        ResourceBundle res = null;
        String resValue = null;

        if ( resourceName == null || resourceName.trim().equals("") )
            return null;

        final Locale loc = checkLocale(locale,jParams);

        // first look for site's engine resource bundle
        res = getSiteAdminResourceBundle(jParams.getSite(),jParams,loc);
        if ( res != null ){
            try {
                resValue = getString(res, resourceName, loc);
                return resValue;
            } catch ( Exception t ){
            }
        }

        // second look for jahia's engine default resource bundle
        res = getAdminDefaultResourceBundle(jParams,loc);
        if ( res != null ){
            try {
                resValue = getString(res, resourceName, loc);
                return resValue;
            } catch ( Exception t ){
            }
        }
        logger.warn("Resource [" + resourceName +
                    "] not found in administration resource bundles using locale [" +
                    loc + "]");
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource for Message Templates.
     *
     * @param resourceName the resource name
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.MessageTag
     *
     */
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

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource for Message Templates.
     *
     * @param resourceName the resource name
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     * @param args arguments to use in the value instead of place-holders
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.MessageTag
     *
     */
    public static String getMessageResource( final String resourceName,
                                             final Locale locale, Object[] args ){
        
        String resValue = getMessageResource(resourceName, locale);
        
        if ( null != resValue && args != null && args.length > 0) {
            resValue = new MessageFormat(resValue, locale).format(args);
        }
        
        return resValue;
    }

        /**
     * Returns the requested resource for Message Templates.
     *
     * @param resourceName the resource name
     * @param locale if null, uses the default locale
     * @param fileName The file were to look for resources
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.MessageTag
     *
     */
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

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource. The resource is prefixed with the Application Context
     *
     * If the requested resource bundle is missing and useDefault is true,
     * Jahia will look for another engine resource bundle in that order :
     *
     *     *		1. 	Look for the engine resource bundle of the current user.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetUsrEngineResourceBundleTag,SetGrpEngineResourceBundleTag.
     *
     *     *		2. 	Look for the engine resource bundle of the page.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetEngineResourceBundleTag.
     *
     *     *		3.      Look for the site's default engine resource bundle.
     *                          Each site can have a default engine resource bundle. It's name
     *         			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
     *         			where MYJAHIASITE is the virtual site's sitekey in uppercase.
     *
     *     *		4.      Finally if none of the previous resource bundle are available,
     *         			Jahia will return the internal engine's default resource bundle
     *         			named "JahiaEnginesResources".
     *
     *
     * @param resourceName the resource name
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the requested resource bundle
     *
     * @see org.jahia.taglibs.resourcebundle.EngineResourceBundleTag
     * @see org.jahia.taglibs.resourcebundle.SetEngineResourceBundleTag
     *
     */
    public static String getUrlPathResourceEngineResource( final String resourceName,
                                                           final ProcessingContext jParams,
                                                           final Locale locale )
    {
        final String res = getEngineResource(resourceName, jParams, locale);
        if ( res != null ){
            final StringBuffer buff = new StringBuffer();
            buff.append(jParams.getContextPath()).append(res);
            return buff.toString();
        }
        return res;
    }


    /**
     * Resolves a resource value by looking at the bundle code if specified, or
     * using the default resource bundle if not specified. The bundle code is
     * currently a hardcoded resolver but in the future we might change this
     * to allow the resolver to be more dynamic, notably making by specifying
     * resolver classes or something fancy like that :)
     *
     * @param bundleCode a String containing a bundle code such as "administration",
     * "engine", "configuration" that will indicate which resolver to use for
     * finding the resource. If the bundleCode is null, the default resolver
     * (engine) will be used.
     * @param resourceKey the key for the resource within the bundle to be found.
     * @param locale the locale to use for resolving the bundle file, or if it
     * is null, the jParams parameter is used accessing it's jParams.getLocale()
     * method
     * @param jParams used in case the locale parameter is null. the
     * jParams.getLocale method is then called.
     *
     * @return a String containing the value associated with the key in the
     * bundle specified by the bundle code, or null if the key couldn't be
     * found.
     */
    public static String getResource(final String bundleCode, 
                                     final String resourceKey, 
                                     final Locale locale, 
                                     final ProcessingContext jParams) {
        final String result;

        if ("administration".equals(bundleCode)) {
            result = getAdminResource(resourceKey, jParams, locale);
        } else if ("configuration".equals(bundleCode)) {
            result = getMessageResource(resourceKey, locale);
        } else {
            result = getEngineResource(resourceKey, jParams, locale);
        }

        return result;
    }


    //--------------------------------------------------------------------------
    /**
     * Get a Jahia common resource defined in ENGINE_DEFAULT_RESOURCE_BUNDLE.
     *
     * @param resourceName The name(key) of resource bundle.
     * @param jParams ;)
     * @return
     *    Null if no resource found.
     */
    public static String getCommonResource(final String resourceName, 
            final ProcessingContext jParams) {

        final ResourceBundle res = ResourceBundle.getBundle(ENGINE_DEFAULT_RESOURCE_BUNDLE);
        try {
            if (res != null) {
                return res.getString(resourceName);
            }
        } catch (java.util.MissingResourceException mre) {
            logger.warn("Resource [" + resourceName + "] not found in [" +
                        ENGINE_DEFAULT_RESOURCE_BUNDLE + "] !");
        }
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Get a Jahia common resource defined in ENGINE_DEFAULT_RESOURCE_BUNDLE.
     * The returned value is prefixed with the Application Context Path.
     *
     * @param resourceName The name(key) of resource bundle.
     * @param jParams
     * @return
     *    Null if no resource found.
     */
    public static String getUrlPathCommonResource(final String resourceName, 
            final ProcessingContext jParams) {

        final String res = getCommonResource(resourceName,jParams);
        if ( res != null ){
            final StringBuffer buff = new StringBuffer();
            buff.append(jParams.getContextPath()).append(res);
            return buff.toString();
        }
        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the requested resource bundle.
     *
     * If the requested resource bundle is missing and useDefault is true,
     * Jahia will look for another engine resource bundle in that order :
     *
     *     *		1. 	Look for the engine resource bundle of the current user.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetUsrEngineResourceBundleTag,SetGrpEngineResourceBundleTag.
     *
     *     *		2. 	Look for the engine resource bundle of the page.
     *         			This resource bundle can be set in the template used by the page
     *         			with the SetEngineResourceBundleTag.
     *
     *     *		3.  Look for the site's default engine resource bundle.
     *            Each site can have a default engine resource bundle. It's name
     *         			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
     *         			where MYJAHIASITE is the virtual site's sitekey in uppercase.
     *
     *     *		4.  Finally if none of the previous resource bundle are available,
     *         			Jahia will return the internal engine's default resource bundle
     *         			named "JahiaEnginesResources".
     *
     *
     * @param resourceBundle the resource bundle name
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     * @param useDefault when true , return Jahia engines' default resource bundle
     *                           	  if the requested resource bundle is missing.
     *
     * @return ResourceBundle, the requested resource bundle
     */
    public static ResourceBundle getEngineResourceBundle(
                                                    final String resourceBundle,
                                                    final ProcessingContext jParams,
                                                    final Locale locale,
                                                    final boolean useDefault ){

        ResourceBundle res = null;

        final Locale loc = checkLocale(locale,jParams);

        try {
            res = ResourceBundle.getBundle(resourceBundle,loc);
        } catch ( Exception t ){
            // logger.debug("Error while retrieving engine resource bundle :" + resourceBundle + " for locale " + loc, t);
        }
        if ( ((res == null) && useDefault) && (jParams != null) ){

            // first look for usr, grp's engine resource bundle
            res = getGrpUsrEngineResourceBundle(jParams.getPageID(),jParams.getUser());

            if ( res == null ){
                // second look for page's engine resource bundle
                res = getPageEngineResourceBundle(jParams.getPageID(),jParams);
            }

            if ( res == null ){
                // third look for site's engine resource bundle
                res = getSiteEngineResourceBundle(jParams.getSite(),jParams,loc);
            }
            if ( res == null ){
                // fourth look for jahia default engine resource bundle
                res = getEngineDefaultResourceBundle(jParams,loc);
            }
        }
        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns Jahia engines' default resource bundle
     * This resource bundle's name is "JahiaEnginesResources"
     *
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the Jahia engines' default resource bundle or null if not found
     */
    public static ResourceBundle getEngineDefaultResourceBundle(
                                                    final ProcessingContext jParams,
                                                    final Locale locale ){

        final Locale loc = checkLocale(locale,jParams);

        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle(ENGINE_DEFAULT_RESOURCE_BUNDLE,loc);
        } catch ( Exception t ){
            logger.debug("Error while using default engine resource bundle (" +
                         ENGINE_DEFAULT_RESOURCE_BUNDLE + ") with locale " + loc, t);
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns Jahia admin' default resource bundle
     * This resource bundle's name is "JahiaAdministrationResources"
     *
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the Jahia engines' default resource bundle or null if not found
     */
    public static ResourceBundle getAdminDefaultResourceBundle(
                                                    final ProcessingContext jParams,
                                                    final Locale locale ){

        final Locale loc = checkLocale(locale,jParams);

        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle(ADMIN_DEFAULT_RESOURCE_BUNDLE,loc);
        } catch ( Exception t ){
            logger.debug("Error while retrieving administration default resource bundle " +
                         ADMIN_DEFAULT_RESOURCE_BUNDLE + " with locale " + loc, t);
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns Jahia Message default resource bundle
     * This resource bundle's name is "JahiaMessageResources"
     *
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the Jahia engines' default resource bundle or null if not found
     */
    public static ResourceBundle getMessageDefaultResourceBundle(
                                                    final ProcessingContext jParams,
                                                    final Locale locale ){

        final Locale loc = checkLocale(locale,jParams);

        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle(MESSAGE_DEFAULT_RESOURCE_BUNDLE,loc);
        } catch ( Exception t ){
            logger.debug("Error while retrieving message default resource bundle " +
                    MESSAGE_DEFAULT_RESOURCE_BUNDLE + " with locale " + loc, t);
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the current site engines' resource bundle
     * Internally, Jahia look for a resource bundle whose name is :
     *
     * "JahiaEnginesResources" + jParams.getSite().getSiteKey().toUpperCase()
     *
     * like :	JahiaEnginesResourcesMYJAHIASITE
     *
     * @param site
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the site engines' default resource bundle or null if not found
     */
    public static ResourceBundle getSiteEngineResourceBundle( final JahiaSite site,
                                                              final ProcessingContext jParams,
                                                              final Locale locale ){

        if ( site == null )
            return null;

        final Locale loc = checkLocale(locale,jParams);

        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle( ENGINE_DEFAULT_RESOURCE_BUNDLE
                                            +site.getSiteKey().toUpperCase(),loc);
        } catch ( Exception t ){
            //JahiaConsole.println(	CLASS_NAME+".getSiteEngineResourceBundle",
            //						t.getMessage());
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the current site admin' resource bundle
     * Internally, Jahia look for a resource bundle whose name is :
     *
     * "JahiaAdministrationResources" + jParams.getSite().getSiteKey().toUpperCase()
     *
     * like :	JahiaAdministrationResourcesMYJAHIASITE
     *
     * @param site
     * @param jParams
     * @param locale if null, uses the locale returned by ProcessingContext.getLocale()
     *
     * @return ResourceBundle, the site engines' default resource bundle or null if not found
     */
    public static ResourceBundle getSiteAdminResourceBundle( final JahiaSite site,
                                                             final ProcessingContext jParams,
                                                             final Locale locale ){

        if ( site == null )
            return null;

        final Locale loc = checkLocale(locale,jParams);

        ResourceBundle res = null;

        try {
            res = ResourceBundle.getBundle( ADMIN_DEFAULT_RESOURCE_BUNDLE
                                            +site.getSiteKey().toUpperCase(),loc);
        } catch ( Exception t ){
            //JahiaConsole.println(	CLASS_NAME+".getSiteEngineResourceBundle",
            //						t.getMessage());
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the engine resource bundle associated with the page.
     * With the SetEngineResourceBundleTag, you can associate an engine resource bundle
     * with a template JSP.
     *
     * Jahia will use this resource bundle to give different look to the engines
     * popup that are opened from pages using this template.
     *
     * @param pageID the page id
     *
     * @return ResourceBundle, the site engines' default resource bundle or null if not found
     */
    public static ResourceBundle getPageEngineResourceBundle( final int pageID , 
                                                              final ProcessingContext jParams){


        if ( jParams == null )
            return null;

        if ( pageID == -1 )
            return null;

        ContentPage contentPage = null;
        try {
            contentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pageID,true);
        } catch ( Exception t) {
            //JahiaConsole.println(	CLASS_NAME+".getPageEngineResourceBundle",
            //						t.getMessage());
        }

        if ( (contentPage == null) )
            return null;

        ResourceBundle res = null;

        try {
            res = PagesEngineResourceBundle.getInstance().getResourceBundle( contentPage , jParams );
        } catch ( Exception t ){
            //JahiaConsole.println(	CLASS_NAME+".getPageEngineResourceBundle",
            //						t.getMessage());
        }

        return res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the engine resource bundle associated with a page and a given Principal.
     * With the SetUsrEngineResourceBundleTag and SetGrpEngineResourceBundleTag,
     * you can associate an engine resource bundle with a template JSP for a given user or group.
     *
     * Jahia will use this resource bundle to give different look to the engines
     * popup that are opened from pages using this template and for the current user.
     *
     * @param pageID the page id
     *
     * @return ResourceBundle, the grp or usr engines' default resource bundle or null if not found
     */
    public static ResourceBundle getGrpUsrEngineResourceBundle( final int pageID , final JahiaUser user){


        if ( pageID == -1 || user==null )
            return null;

        ContentPage contentPage = null;
        try {
            contentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pageID,true);
        } catch ( Exception t) {
            //JahiaConsole.println(	CLASS_NAME+".getPageEngineResourceBundle",
            //						t.getMessage());
        }

        if ( (contentPage == null) )
            return null;

        ResourceBundle res = null;

        try {
            res = GrpUsrEngineResourceBundle.getInstance().getResourceBundle( contentPage , user );
        } catch ( Exception t ){
            //JahiaConsole.println(	CLASS_NAME+".getPageEngineResourceBundle",
            //						t.getMessage());
        }

        return res;
    }


    private static Locale checkLocale(final Locale locale, final ProcessingContext jParams){
        final Locale resLocale;
        if ( locale == null ){
            if ( jParams != null ){
                resLocale = jParams.getLocale();
            } else {
                resLocale = Locale.getDefault();
            }
            
        } else {
            resLocale = locale;
        }
        return resLocale;
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
        String resource = null;
        try {
            resource = res.getString(resName);
        } catch (Exception ex) {
            logger.warn("Resource [" + resName +
                    "] not found in " + res.getClass() + " using locale [" +
                    locale + "]");
        }
        return resource;
    }

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
    public static String getString(final ResourceBundle res,
                                   final String resName,
                                   final Locale locale,
                                   final String defaultValue) {
        String resource = null;
        try {
            resource = res.getString(resName);
        } catch (Exception ex) {
            logger.warn("Resource [" + resName +
                    "] not found in " + res.getClass() + " using locale [" +
                    locale + "]");
        }

        if (resource != null && resource.length() > 0) {
            return resource;
        } else {
            return defaultValue;
        }
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