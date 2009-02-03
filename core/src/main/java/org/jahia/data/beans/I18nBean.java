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

package org.jahia.data.beans;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;

/**
 * Facade for retrieving resource messages by name.
 * 
 * @author Sergiy Shyrkov
 */
public class I18nBean extends LookupBaseBean {

    public static I18nBean getInstance(Locale locale, ProcessingContext ctx) {
        return getInstance(ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService().getCurrentResourceBundleName(
                        ctx), locale, ctx);
    }

    public static I18nBean getInstance(ProcessingContext ctx) {
        return getInstance(ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService().getCurrentResourceBundleName(
                        ctx), ctx.getLocale(), ctx);
    }

    public static I18nBean getInstance(String name,
            JahiaTemplatesPackage currentTemplateSet, Locale locale) {
        List<ResourceBundle> resourceBundles = new LinkedList<ResourceBundle>();
        if (currentTemplateSet != null
                && currentTemplateSet.getResourceBundleName() != null
                && (name == null || currentTemplateSet.getResourceBundleName()
                        .equals(name))) {
            for (String bundleName : currentTemplateSet
                    .getResourceBundleHierarchy()) {
                resourceBundles.add(ResourceBundle
                        .getBundle(bundleName, locale));
            }
        } else {
            if (name != null) {
                resourceBundles.add(ResourceBundle.getBundle(name, locale));
            }
            resourceBundles.add(ResourceBundle.getBundle(
                    "jahiatemplates.common", locale));
            resourceBundles.add(ResourceBundle.getBundle("CommonTag", locale));
        }

        return new I18nBean(name, resourceBundles);
    }

    public static I18nBean getInstance(String name, Locale locale,
            ProcessingContext ctx) {
        return getInstance(name, ctx.getSite() != null ? ServicesRegistry
                .getInstance().getJahiaTemplateManagerService()
                .getTemplatePackage(ctx.getSite().getTemplatePackageName())
                : null, locale);
    }

    public static I18nBean getInstance(String name, ProcessingContext ctx) {
        return getInstance(name, ctx.getLocale(), ctx);
    }

    private String bundleName;

    private List<ResourceBundle> resourceBundles;

    I18nBean(String name, List<ResourceBundle> resourceBundles) {
        this.bundleName = name;
        this.resourceBundles = resourceBundles;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.data.beans.LookupBaseBean#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        String resourceKey = String.valueOf(key);
        return get(resourceKey, resourceKey);
    }

    public String get(String key, String defaultValue) {
        String value = null;
        for (ResourceBundle bundle : resourceBundles) {
            try {
                value = bundle.getString(key);
                if (value != null) {
                    break;
                }
            } catch (MissingResourceException e) {
                // ignore it
            }
        }

        return value != null ? value : defaultValue;
    }

    public String getBundleName() {
        return bundleName;
    }
}