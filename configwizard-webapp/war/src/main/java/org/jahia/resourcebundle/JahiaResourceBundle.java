/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *///
//
// NK 18.02.2002 - added in Jahia
//

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

