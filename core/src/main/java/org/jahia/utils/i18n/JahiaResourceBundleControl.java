/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia implementation of the ResourceBundle {@link Control} class for looking up resources in modules.
 * 
 * @author Sergiy Shyrkov
 */
class JahiaResourceBundleControl extends Control {

    private static JahiaResourceBundleControl defaultControl;

    private static final Logger logger = LoggerFactory.getLogger(JahiaResourceBundleControl.class);

    public static JahiaResourceBundleControl getInstance() {
        if (defaultControl == null) {
            defaultControl = new JahiaResourceBundleControl(JahiaContextLoaderListener.getServletContext(),
                    SettingsBean.getInstance().isConsiderDefaultJVMLocale());
        }
        return defaultControl;
    }

    private boolean considerDefaultJVMLocale;

    private ServletContext servletContext;

    private JahiaResourceBundleControl(ServletContext servletContext, boolean considerDefaultJVMLocale) {
        super();
        this.servletContext = servletContext;
        this.considerDefaultJVMLocale = considerDefaultJVMLocale;
    }

    private InputStream getClasspathStream(final ClassLoader classLoader, final boolean reloadFlag,
            final String resourceName) throws IOException {
        InputStream stream = null;
        try {
            stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                public InputStream run() throws IOException {
                    InputStream is = null;
                    if (reloadFlag) {
                        URL url = classLoader.getResource(resourceName);
                        if (url != null) {
                            URLConnection connection = url.openConnection();
                            if (connection != null) {
                                // Disable caches to get fresh data for
                                // reloading.
                                connection.setUseCaches(false);
                                is = connection.getInputStream();
                            }
                        }
                    } else {
                        is = classLoader.getResourceAsStream(resourceName);
                    }
                    return is;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        return stream;
    }

    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        return considerDefaultJVMLocale ? super.getFallbackLocale(baseName, locale) : null;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return FORMAT_PROPERTIES;
    }

    private InputStream getResourceBundleStream(final ClassLoader classLoader, final boolean reloadFlag,
            final String resourceName, final String baseName) throws IOException {
        InputStream stream = null;
        // check if a JahiaInternalResources or JahiaTypesResources is requested
        if (baseName.equals(ResourceBundles.JAHIA_INTERNAL_RESOURCES)
                || baseName.equals(ResourceBundles.JAHIA_TYPES_RESOURCES)) {
            stream = getClasspathStream(classLoader, reloadFlag, resourceName);
        } else {
            // check if a module resource bundle is requested
            if (resourceName.startsWith("modules/")) {
                // lookup using ServletContext
                stream = getServletContextStream(resourceName);
            } else {
                // otherwise look up it as classpath resource
                stream = getClasspathStream(classLoader, reloadFlag, resourceName);
            }
        }
        return stream;
    }

    private InputStream getServletContextStream(String resourceName) throws IOException {
        return servletContext != null ? servletContext.getResourceAsStream(resourceName.charAt(0) == '/' ? resourceName
                : ('/' + resourceName)) : null;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {

        if (!format.equals("java.properties")) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        if (servletContext == null) {
            ResourceBundle rb = super.newBundle(baseName, locale, format, loader, reload);
            if (logger.isDebugEnabled()) {
                logger.debug("{} resource bundle for basename '{}' and locale '{}'", new Object[] {
                        rb != null ? "Found" : "Cannot find", baseName, locale });
            }
            return rb;
        }

        ResourceBundle bundle = null;
        final String resourceName = StringUtils.replace(toResourceName(toBundleName(baseName, locale), "properties"),
                "___", ".");

        InputStream stream = getResourceBundleStream(loader, reload, resourceName, baseName);
        if (stream != null) {
            try {
                bundle = new JahiaPropertyResourceBundle(stream);
            } finally {
                stream.close();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} resource bundle for basename '{}' and locale '{}' under: {}", new Object[] {
                    bundle != null ? "Found" : "Cannot find", baseName, locale, resourceName });
        }

        return bundle;
    }
}
