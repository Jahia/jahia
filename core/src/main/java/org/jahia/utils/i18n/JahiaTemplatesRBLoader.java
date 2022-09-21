/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import org.slf4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * @deprecated use {@link ResourceBundles} or {@link Messages} instead
 */
@Deprecated
public class JahiaTemplatesRBLoader extends ClassLoader {
    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaTemplatesRBLoader.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("\\\\.");
    private ClassLoader loader = ClassLoader.getSystemClassLoader();
    private JahiaTemplatesPackage aPackage;
    private JahiaTemplateManagerService templateManagerService;
    
    private static WeakHashMap<ClassLoader, Map<String, JahiaTemplatesRBLoader>> loadersCache = new WeakHashMap<ClassLoader, Map<String, JahiaTemplatesRBLoader>>();

    public static JahiaTemplatesRBLoader getInstance(ClassLoader loader,
            String templatePackageName) {
        JahiaTemplatesRBLoader instance = null;
        Map<String, JahiaTemplatesRBLoader> cacheByTemplatePackage = loader != null ? loadersCache
                .get(loader)
                : null;
        if (cacheByTemplatePackage != null) {
            instance = cacheByTemplatePackage.get(templatePackageName);
        }
        if (instance == null) {
            instance = new JahiaTemplatesRBLoader(loader, templatePackageName);
            if (loader != null && templatePackageName != null) {
                synchronized (loadersCache) {
                    Map<String, JahiaTemplatesRBLoader> cacheByPackage = loader != null ? loadersCache
                            .get(loader)
                            : null;
                    if (cacheByPackage == null) {
                        cacheByPackage = new HashMap<String, JahiaTemplatesRBLoader>();
                        loadersCache.put(loader, cacheByPackage);
                    }
                    cacheByPackage.put(templatePackageName, instance);
                }
            }
        }

        return instance;
    }
    
    /**
     * Flushes the ResourceBundle internal caches.
     * TODO use ResourceBundle#clearCache() after switch to Java 6
     */
    public static void clearCache() {
        loadersCache.clear();
    }
    
    private JahiaTemplatesRBLoader(ClassLoader loader, String templatePackageName) {
        super();
        templateManagerService =  ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        if (templateManagerService != null) {
            // This case is possible upon Jahia startup, when accessing resources while starting up the template
            // manager service.
            aPackage = templateManagerService.getTemplatePackage(templatePackageName);
        }
        if (loader != null) {
            this.loader = loader;
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (loader != null) {
            final Class<?> aClass = loader.loadClass(name);
            if (aClass != null) {
                return aClass;
            }
        }
        return Class.forName(name);
    }

    public URL getResource(String name) {
        if (loader != null) {
            return loader.getResource(name);
        }
        return ClassLoader.getSystemResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        name = Patterns.TRIPPLE_UNDERSCORE.matcher(name).replaceAll(".");
        if (loader != null) {
            InputStream stream = loader.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            } else {
                String fileName = NAME_PATTERN.matcher(name).replaceAll(File.separator);
                if (aPackage != null) {
                    String path = aPackage.getRootFolderPath() + (!fileName.startsWith("/") ? "/" : "") + fileName;
                    path = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path) != null ? path : null;
                    if (path != null) {
                        stream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
                    }
                    if (stream == null) {
                    	stream = JahiaContextLoaderListener.getServletContext().getResourceAsStream((!name.startsWith("/") ? "/" : "") + NAME_PATTERN.matcher(name).replaceAll("/"));
                    }
                    if (stream == null) {
                        String resourcePath = "resources/" + StringUtils.substringAfter(fileName,"/resources/");
                        try {
                            stream = aPackage.getResource(resourcePath).getInputStream();
                        } catch (IOException e) {
                            //
                        }
                    }
                    if (stream != null) {
                    	return stream;
                    }
                }
                try {
                    if (SettingsBean.getInstance() != null) {
                        File file = new File(SettingsBean.getInstance().getClassDiskPath(), fileName);
                        if (file.exists()) {
                            return new BufferedInputStream(new FileInputStream(file));
                        }
                    }
                } catch (FileNotFoundException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return ClassLoader.getSystemResourceAsStream(name);
    }
}