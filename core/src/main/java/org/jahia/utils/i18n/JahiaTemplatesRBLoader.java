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
package org.jahia.utils.i18n;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

public class JahiaTemplatesRBLoader extends ClassLoader {
    private static transient Logger logger = Logger.getLogger(JahiaTemplatesRBLoader.class);
    private ClassLoader loader = ClassLoader.getSystemClassLoader();
    private JahiaTemplatesPackage aPackage;
    
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
        try {
            Field cacheList = ResourceBundle.class
                    .getDeclaredField("cacheList");
            cacheList.setAccessible(true);
            ((Map) cacheList.get(ResourceBundle.class)).clear();
        } catch (Exception e) {
            logger.warn("Unable to flush resource bundle cache", e);
        }
    }
    
    private JahiaTemplatesRBLoader(ClassLoader loader, String templatePackageName) {
        aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templatePackageName);
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
        if (loader != null) {
            final InputStream stream = loader.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            } else {
                File s;
                if (aPackage != null) {
                    s = new File(new File(Jahia.getSettings()
                            .getJahiaTemplatesDiskPath(), aPackage
                            .getRootFolder()), name.replaceAll("\\\\.", File.separator));
                } else {
                    s = new File(Jahia.getSettings().getClassDiskPath(), name.replaceAll("\\\\.", File.separator));
                }
                try {
                    return new FileInputStream(s);
                } catch (FileNotFoundException e) {
                    // Try to find it inside WEB-INF/classes
                    s = new File(Jahia.getSettings().getClassDiskPath(), name.replaceAll("\\\\.", File.separator));
                    try {
                        return new FileInputStream(s);
                    } catch (FileNotFoundException e1) {
                        logger.warn(e1);
                    }
                }
            }
        }
        return ClassLoader.getSystemResourceAsStream(name);
    }
}