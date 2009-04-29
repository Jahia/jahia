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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;

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
        return new JahiaResourceBundle(ctx.getLocale(), ctx.getSite().getTemplatePackageName()).getString(new StringBuilder("actionmenus.actions.").append(action).toString(), action);
    }

    private static String getSuffixLabel(String bundleName, ProcessingContext ctx, String action, String postfix, String type) {
        String suffix = postfix != null && postfix.length() > 0 ? postfix : type;
        JahiaResourceBundle bundle = new JahiaResourceBundle(bundleName != null && bundleName.length() > 0 ? bundleName : null, ctx.getLocale(), ctx.getSite().getTemplatePackageName());
        
        String suffixLabel ;
        suffixLabel = getMessage(bundle,
                new StringBuilder("actionmenus.postfixes.").append(suffix).append(".").append(action).toString(),
                new StringBuilder("actionmenus.postfixes.").append(suffix).append(".default").toString());
        
        if (suffixLabel == null && !suffix.equals(type)) {
            suffixLabel = getMessage(bundle,
                    new StringBuilder("actionmenus.postfixes.").append(type).append(".").append(action).toString(),
                    new StringBuilder("actionmenus.postfixes.").append(type).append(".default").toString());
        }
        
        return suffixLabel != null && suffixLabel.length() > 0 ? new StringBuilder(" ").append(suffixLabel).toString() : "";
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
        JahiaResourceBundle bundle = new JahiaResourceBundle(bundleName != null && bundleName.length() > 0 ? bundleName : null , ctx.getLocale(), ctx.getSite().getTemplatePackageName()) ;
        String[] labelKey = new String[] {new StringBuilder("actionmenus.iconlabels.").append(key).toString(), new StringBuilder("actionmenus.iconlabels.").append(type).toString()};
        String iconLabel = getMessage(bundle, labelKey) ;
        return iconLabel != null ? iconLabel : "";
    }

    private static String getMessage(JahiaResourceBundle bundle, String... labels) {
        String message = null;
        for (String label : labels) {
            try {
                message = bundle.getString(label);
                if (message != null) {
                    break;
                }
            } catch (MissingResourceException ex) {
                logger.debug("No resource for entry '" + label + "' in bundle " + bundle.getLookupBundles());
            }
        }

        return message;
    }
}