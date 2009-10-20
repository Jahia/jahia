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
package org.jahia.utils.i18n;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;

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
import java.util.regex.Pattern;

public class JahiaTemplatesRBLoader extends ClassLoader {
    private static transient Logger logger = Logger.getLogger(JahiaTemplatesRBLoader.class);
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
        try {
            Field cacheList = ResourceBundle.class
                    .getDeclaredField("cacheList");
            cacheList.setAccessible(true);
            ((Map<?, ?>) cacheList.get(ResourceBundle.class)).clear();
        } catch (Exception e) {
            logger.warn("Unable to flush resource bundle cache", e);
        }
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
        if (loader != null) {
            InputStream stream = loader.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            } else {
                String fileName = NAME_PATTERN.matcher(name).replaceAll(File.separator);
                if (aPackage != null) {
                    String path = templateManagerService.resolveResourcePath(fileName, aPackage.getName());
                    if (path != null) {
                        stream = Jahia.getStaticServletConfig().getServletContext().getResourceAsStream(path);
                    }
                    if (stream == null) {
                    	stream = Jahia.getStaticServletConfig().getServletContext().getResourceAsStream(NAME_PATTERN.matcher(name).replaceAll("/"));
                    }
                    if (stream != null) {
                    	return stream;
                    }
                }
                try {
                    return new FileInputStream(new File(Jahia.getSettings().getClassDiskPath(), fileName));
                } catch (FileNotFoundException e) {
                    logger.warn(e);
                }
            }
        }
        return ClassLoader.getSystemResourceAsStream(name);
    }
}