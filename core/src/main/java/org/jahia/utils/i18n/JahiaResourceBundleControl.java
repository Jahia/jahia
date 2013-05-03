/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia implementation of the ResourceBundle {@link Control} class for looking up resources in modules.
 * 
 * @author Sergiy Shyrkov
 */
final class JahiaResourceBundleControl extends Control {

    private TemplatePackageRegistry templatePackageRegistry;

    private static JahiaResourceBundleControl defaultControl;

    private static final Logger logger = LoggerFactory.getLogger(JahiaResourceBundleControl.class);

    public static JahiaResourceBundleControl getInstance() {
        if (defaultControl == null) {
            defaultControl = new JahiaResourceBundleControl(SettingsBean.getInstance().isConsiderDefaultJVMLocale());
        }
        return defaultControl;
    }

    private boolean considerDefaultJVMLocale;

    private JahiaResourceBundleControl(boolean considerDefaultJVMLocale) {
        super();
        this.considerDefaultJVMLocale = considerDefaultJVMLocale;
        this.templatePackageRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry();
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

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {

        if (!format.equals("java.properties")) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        ResourceBundle bundle = null;
        String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
        InputStream stream = getStreamFromSources(baseName, resourceName);
        if (stream == null) {
            stream = getClasspathStream(loader, reload, resourceName);
        }

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

    private InputStream getStreamFromSources(String baseName, String resourceName) throws FileNotFoundException {
        JahiaTemplatesPackage aPackage = templatePackageRegistry.getPackageForResourceBundle(baseName);
        if (aPackage == null || aPackage.getSourcesFolder() == null) {
            return null;
        }

        File sourcesFolder = new File(new File(aPackage.getSourcesFolder(), "src/main/resources"), resourceName);
        if (sourcesFolder.exists()) {
            return new BufferedInputStream(new FileInputStream(sourcesFolder));
        }

        sourcesFolder = new File(new File(aPackage.getSourcesFolder(), "src/main/webapp"), resourceName);
        if (sourcesFolder.exists()) {
            return new BufferedInputStream(new FileInputStream(sourcesFolder));
        }
        return null;
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        JahiaTemplatesPackage aPackage = templatePackageRegistry.getPackageForResourceBundle(baseName);
        if (aPackage != null && aPackage.getSourcesFolder() != null) {
            return TTL_DONT_CACHE;
        }
        return super.getTimeToLive(baseName, locale);
    }
}
