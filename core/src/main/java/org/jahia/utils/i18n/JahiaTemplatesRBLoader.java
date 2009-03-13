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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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