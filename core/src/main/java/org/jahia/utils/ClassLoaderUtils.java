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
package org.jahia.utils;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
