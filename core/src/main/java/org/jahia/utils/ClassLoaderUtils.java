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
package org.jahia.utils;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.jahia.osgi.BundleUtils;

/**
 * Class loading related utilities.
 *
 * @author Sergiy Shyrkov
 */
public final class ClassLoaderUtils {

    /**
     * Defines a callback processor for an action which will be executed using the provided class loader.
     *
     * @param <T>
     *            the return type of the execution method
     * @author Sergiy Shyrkov
     */
    public static interface Callback<T> {
        T execute();
    }

    /**
     * A class loader capable of loading both core and modules classes.
     * <p>
     * Relies solely on the parent class loader when loading core classes.
     */
    public static class CoreAndModulesClassLoader extends ClassLoader {

        /**
         * @param parent Parent class loader capable of loading core classes.
         */
        public CoreAndModulesClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                return super.loadClass(name);
            } catch (ClassNotFoundException e) {
                return BundleUtils.loadModuleClass(name);
            }
        }
    }

    private static class ChainedClassLoader extends ClassLoader {
        private ClassLoader[] loaders;

        ChainedClassLoader(ClassLoader[] loaders) {
            super(null);
            this.loaders = loaders;
        }

        @Override
        public URL getResource(final String name) {
            if (System.getSecurityManager() != null) {
                    return AccessController.doPrivileged(new PrivilegedAction<URL>() {

                            public URL run() {
                                    return myGetResource(name);
                            }
                    });
            } else {
                    return myGetResource(name);
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {

            final List<Enumeration<URL>> urlsEnums = new ArrayList<Enumeration<URL>>();
            for (ClassLoader loader : loaders) {
                Enumeration<URL> urls = loader.getResources(name);
                if (urls != null && urls.hasMoreElements()) {
                    // we only add enumerations that have elements, make things simpler
                    urlsEnums.add(urls);
                }
            }

            if (urlsEnums.size() == 0) {
                return java.util.Collections.emptyEnumeration();
            }

            return new Enumeration<URL>() {

                int i=0;
                Enumeration<URL> currentEnum = urlsEnums.get(i);

                @Override
                public boolean hasMoreElements() {
                    if (currentEnum.hasMoreElements()) {
                        return true;
                    }
                    int j=i;
                    do {
                        j++;
                    } while (j < (urlsEnums.size()-1) && !urlsEnums.get(j).hasMoreElements());
                    if (j <= (urlsEnums.size()-1)) {
                        return urlsEnums.get(j).hasMoreElements();
                    } else {
                        return false;
                    }
                }

                @Override
                public URL nextElement() {
                    if (currentEnum.hasMoreElements()) {
                        return currentEnum.nextElement();
                    }
                    do {
                        i++;
                        currentEnum = urlsEnums.get(i);
                    } while (!currentEnum.hasMoreElements() && i < (urlsEnums.size()-1));
                    if (currentEnum.hasMoreElements()) {
                        return currentEnum.nextElement();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public Class<?> loadClass(final String className) throws ClassNotFoundException {

            if (System.getSecurityManager() != null) {
                try {
                    return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                        public Class<?> run() throws Exception {
                            return myLoadClass(className);
                        }
                    });
                } catch (PrivilegedActionException pae) {
                    throw (ClassNotFoundException) pae.getException();
                }
            } else {
                return myLoadClass(className);
            }
        }

        private URL myGetResource(String resource) {
            URL url = null;
            synchronized (loaders) {
                for (ClassLoader cl : loaders) {
                    url = cl.getResource(resource);
                }
            }
            return url;
        }

        private Class<?> myLoadClass(String name) throws ClassNotFoundException {
            synchronized (loaders) {
                for (ClassLoader cl : loaders) {
                    try {
                        return cl.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // continue
                    }
                }
            }

            throw new ClassNotFoundException(name);
        }

    }

    /**
     * Executes the provided callback within the context of the specified class loader.
     *
     * @param cl
     *            the class loader to use as a context class loader for the execution
     * @param callback
     *            the execution callback handler
     * @return the result of the execution
     */
    public static <T> T executeWith(ClassLoader cl, Callback<T> callback) {
        Thread current = Thread.currentThread();
        ClassLoader contextCL = current.getContextClassLoader();
        try {
            current.setContextClassLoader(cl);
            return callback.execute();
        } finally {
            current.setContextClassLoader(contextCL);
        }
    }

    /**
     * Returns an instance of the class loader that uses provided set of class loaders in a chain.
     *
     * @param loaders
     *            the chain of the class loaders to use
     * @return an instance of the class loader that uses provided set of class loaders in a chain
     */
    public static ClassLoader getChainedClassLoader(ClassLoader... loaders) {
        return new ChainedClassLoader(loaders);
    }

    /**
     * Initializes an instance of this class.
     */
    private ClassLoaderUtils() {
        super();
    }
}
