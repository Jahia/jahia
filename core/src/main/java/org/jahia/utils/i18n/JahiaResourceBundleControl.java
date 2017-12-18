/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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

    private static volatile JahiaResourceBundleControl defaultControl;
    private static final Logger logger = LoggerFactory.getLogger(JahiaResourceBundleControl.class);

    private TemplatePackageRegistry templatePackageRegistry;
    private boolean considerDefaultJVMLocale;

    private JahiaResourceBundleControl(boolean considerDefaultJVMLocale) {
        this.considerDefaultJVMLocale = considerDefaultJVMLocale;
        this.templatePackageRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry();
    }

    public static JahiaResourceBundleControl getInstance() {
        if (defaultControl == null) {
            synchronized (JahiaResourceBundleControl.class) {
                if (defaultControl == null) {
                    defaultControl = new JahiaResourceBundleControl(SettingsBean.getInstance().isConsiderDefaultJVMLocale());
                }
            }
        }
        return defaultControl;
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

    /**
     * Override getTimeToLive : if sources are available, never cache resource bundles
     * @param baseName
     * @param locale
     * @return
     */
    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        JahiaTemplatesPackage aPackage = templatePackageRegistry.getPackageForResourceBundle(baseName);
        if (aPackage != null && aPackage.getSourcesFolder() != null) {
            return 1000;
        }
        return super.getTimeToLive(baseName, locale);
    }

    @Override
    public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader,
            ResourceBundle bundle, long loadTime) {
        boolean needsReload = super.needsReload(baseName, locale, format, loader, bundle, loadTime);

        if (!needsReload) {
            JahiaTemplatesPackage aPackage = templatePackageRegistry.getPackageForResourceBundle(baseName);
            if (aPackage != null && aPackage.getSourcesFolder() != null) {
                String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
                File rbSrc = new File(new File(aPackage.getSourcesFolder(), "src/main/resources"), resourceName);
                if (rbSrc.exists() && rbSrc.lastModified() > loadTime) {
                    needsReload = true;
                }
            }
        }

        return needsReload;
    }
}
