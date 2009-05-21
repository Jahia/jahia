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
    public static String getActionLabel(ProcessingContext ctx, String action) {
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