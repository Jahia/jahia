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

package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * This class provides localized messages from built-in and custom resource bundles.
 *
 * @author rfelden
 * @version 13 f?vr. 2008 - 16:32:20
 */
public class ActionMenuLabelProvider {

    public static final String CONTAINER_LIST = "containerlist" ;
    public static final String CONTAINER = "container" ;
    public static final String FIELD = "field" ;
    public static final String PAGE = "page" ;

    public static final String COMMON_TAG_BUNDLE = "CommonTag" ;

    private final static Logger logger = Logger.getLogger(ActionMenuLabelProvider.class) ;

    /**
     * This is the main method to return the formatted localized label of a given action item.
     *
     * @param bundleName the bundle to use (null means default bundle)
     * @param ctx the current processing context
     * @param action the action
     * @param postfix the suffix key to append (null means no suffix)
     * @param type the type of content object
     * @return the formatted localized label
     */
    public static String getLocalizedActionLabel(String bundleName, ProcessingContext ctx, String action, String postfix, String type) {
        return new StringBuilder(getActionLabel(ctx, action)).append(getSuffixLabel(bundleName, ctx, action, postfix, type)).toString() ;
    }

    /**
     * Format the action menu label using an action identifier, a suffix and a locale.
     *
     * @param ctx the current processing context
     * @param action the action identifier (see constants)
     * @return the formatted label
     */
    private static String getActionLabel(ProcessingContext ctx, String action) {
        return new JahiaResourceBundle(COMMON_TAG_BUNDLE, ctx.getLocale(), ctx.getSite().getTemplatePackageName()).getString(new StringBuilder("actionmenus.actions.").append(action).toString(), action);

    }

    private static String getSuffixLabel(String bundleName, ProcessingContext ctx, String action, String postfix, String type) {
        if (bundleName == null || bundleName.length() == 0) {
            bundleName = COMMON_TAG_BUNDLE ;
        }
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, ctx.getLocale()) ;

        String suffix ;
        if (postfix == null || postfix.length() == 0) {
            suffix = type ;
        } else {
            suffix = postfix ;
        }

        String actionSuffixKey = new StringBuilder("actionmenus.postfixes.").append(suffix).append(".").append(action).toString();
        String defaultSuffixKey = new StringBuilder("actionmenus.postfixes.").append(suffix).append(".default").toString();

        String suffixLabel = null ;
        try {
            suffixLabel = bundle.getString(actionSuffixKey) ;
        } catch (MissingResourceException ex) {
            try {
                suffixLabel = bundle.getString(defaultSuffixKey) ;
            } catch (MissingResourceException e) {
                logger.debug("No resource for entry '" + actionSuffixKey + "/" + defaultSuffixKey + "' in bundle " + bundleName);
                if (!bundleName.equals(COMMON_TAG_BUNDLE)) {
                    bundle = ResourceBundle.getBundle(COMMON_TAG_BUNDLE, ctx.getLocale()) ;
                    try {
                        suffixLabel = bundle.getString(actionSuffixKey) ;
                    } catch (MissingResourceException e1) {
                        try {
                            suffixLabel = bundle.getString(defaultSuffixKey) ;
                        } catch (MissingResourceException e2) {
                            logger.debug("No resource for entry '" + actionSuffixKey + "/" + defaultSuffixKey + "' in bundle " + COMMON_TAG_BUNDLE);
                        }
                    }
                    if (suffixLabel == null) {
                        actionSuffixKey = new StringBuilder("actionmenus.postfixes.").append(type).append(".").append(action).toString();
                        defaultSuffixKey = new StringBuilder("actionmenus.postfixes.").append(type).append(".default").toString();
                        try {
                            suffixLabel = bundle.getString(actionSuffixKey) ;
                        } catch (MissingResourceException e1) {
                            try {
                                suffixLabel = bundle.getString(defaultSuffixKey) ;
                            } catch (MissingResourceException e2) {
                                logger.info("No resource for entry '" + actionSuffixKey + "/" + defaultSuffixKey + "' in bundle " + COMMON_TAG_BUNDLE);
                                return "" ;
                            }
                        }
                    }
                } else {
                    return "" ;
                }
            }
        }
        return new StringBuilder(" ").append(suffixLabel).toString() ;
    }

    /**
     * Format the action menu icon label using given key and locale, searching into the provided custom resource bundle.
     *
     * @param bundleName the bundle to take custom resources from, if null use default
     * @param key the action menu label key
     * @param ctx the current processing context
     * @param type the type of content object
     * @return the formatted label
     */
    public static String getIconLabel(String bundleName, String key, String type, ProcessingContext ctx) {
        if (bundleName == null || bundleName.length() == 0) {
            bundleName = COMMON_TAG_BUNDLE ;
        }
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, ctx.getLocale()) ;
        String iconKey = new StringBuilder("actionmenus.iconlabels.").append(key).toString();
        String iconDefaultKey = new StringBuilder("actionmenus.iconlabels.").append(type).toString();
        String iconLabel ;
        try {
            iconLabel = bundle.getString(iconKey);
        } catch (MissingResourceException e) {
            try {
                iconLabel = bundle.getString(iconDefaultKey) ;
            } catch (Exception e1) {
                logger.debug("No resource for entry '" + iconKey + "/" + iconDefaultKey + "' in bundle " + bundleName);
                if (!bundleName.equals(COMMON_TAG_BUNDLE)) {
                    try {
                        bundle = ResourceBundle.getBundle(COMMON_TAG_BUNDLE, ctx.getLocale()) ;
                        iconLabel = bundle.getString(iconDefaultKey) ;
                    } catch (MissingResourceException e3) {
                        logger.debug("No resource for entry '" + iconDefaultKey + "' in bundle " + COMMON_TAG_BUNDLE);
                        return "" ;
                    }
                } else {
                    return "" ;
                }
            }
        }
        return iconLabel;
    }

}