/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
/**
 * This class was copied from the Spring OSGi sources since it was the only file we needed.
 */
package org.jahia.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * ClassLoader backed by an OSGi bundle. Provides the ability to use a separate
 * class loader as fall back.
 *
 * @author Adrian Colyer
 * @author Andy Piper
 * @author Costin Leau
 */
public class BundleDelegatingClassLoader extends ClassLoader implements BundleReference {

    /**
     * use degradable logger
     */
    private static final Logger log = LoggerFactory.getLogger(BundleDelegatingClassLoader.class);

    private final ClassLoader bridge;

    private final Bundle backingBundle;


    /**
     * Factory method for creating a class loader over the given bundle.
     *
     * @param aBundle bundle to use for class loading and resource acquisition
     * @return class loader adapter over the given bundle
     */
    public static BundleDelegatingClassLoader createBundleClassLoaderFor(Bundle aBundle) {
        return createBundleClassLoaderFor(aBundle, null);
    }

    /**
     * Factory method for creating a class loader over the given bundle and with
     * a given class loader as fall-back. In case the bundle cannot find a class
     * or locate a resource, the given class loader will be used as fall back.
     *
     * @param bundle bundle used for class loading and resource acquisition
     * @param bridge class loader used as fall back in case the bundle cannot
     *               load a class or find a resource. Can be <code>null</code>
     * @return class loader adapter over the given bundle and class loader
     */
    public static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
        return AccessController.doPrivileged(new PrivilegedAction<BundleDelegatingClassLoader>() {

            public BundleDelegatingClassLoader run() {
                return new BundleDelegatingClassLoader(bundle, bridge);
            }
        });
    }

    /**
     * Private constructor.
     * <p/>
     * Constructs a new <code>BundleDelegatingClassLoader</code> instance.
     *
     * @param bundle
     * @param bridgeLoader
     */
    protected BundleDelegatingClassLoader(Bundle bundle, ClassLoader bridgeLoader) {
        super(null);
        this.backingBundle = bundle;
        this.bridge = bridgeLoader;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return this.backingBundle.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]",
                    cnfe);
        } catch (NoClassDefFoundError ncdfe) {
            // This is almost always an error
            // This is caused by a dependent class failure,
            // so make sure we search for the right one.
            String cname = ncdfe.getMessage().replace('/', '.');
            NoClassDefFoundError e = new NoClassDefFoundError(cname + " not found from bundle ["
                    + backingBundle + "]");
            e.initCause(ncdfe);
            throw e;
        } catch (IllegalStateException ise) {
            throw new IllegalStateException(String.format("Impossible to load the class %s from the bundle %s", name, this.backingBundle), ise);
        }
    }

    protected URL findResource(String name) {
        boolean trace = log.isTraceEnabled();

        if (trace)
            log.trace("Looking for resource " + name);
        URL url = this.backingBundle.getResource(name);

        if (trace && url != null)
            log.trace("Found resource " + name + " at " + url);
        return url;
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
        boolean trace = log.isTraceEnabled();

        if (trace)
            log.trace("Looking for resources " + name);

        Enumeration<URL> enm = this.backingBundle.getResources(name);

        if (trace && enm != null && enm.hasMoreElements())
            log.trace("Found resource " + name + " at " + this.backingBundle.getLocation());

        return enm;
    }

    public URL getResource(String name) {
        URL resource = findResource(name);
        if (bridge != null && resource == null) {
            resource = bridge.getResource(name);
        }
        return resource;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final Enumeration<URL> enm = findResources(name);
        final Enumeration<URL> bridgeEnm = bridge != null ? bridge.getResources(name) : null;
        if (enm == null && bridgeEnm == null) {
            return null;
        }
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return (enm != null && enm.hasMoreElements()) || (bridgeEnm != null && bridgeEnm.hasMoreElements());
            }

            @Override
            public URL nextElement() {
                if (enm != null && enm.hasMoreElements()) {
                    return enm.nextElement();
                }
                if (bridgeEnm != null && bridgeEnm.hasMoreElements()) {
                    return bridgeEnm.nextElement();
                }
                throw new NoSuchElementException();
            }
        };
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = findClass(name);
        } catch (ClassNotFoundException cnfe) {
            if (bridge != null) {
                try {
                    clazz = bridge.loadClass(name);
                } catch (ClassNotFoundException bridgeCnfe) {
                    // @todo would be good to output some logging information here to help debug class loading issues.
                    throw bridgeCnfe;
                }
            } else {
                throw cnfe;
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    public String toString() {
        return "BundleDelegatingClassLoader for [" + backingBundle + "]";
    }

    /**
     * Returns the bundle to which this class loader delegates calls to.
     * This method also make this classloader compatible with the BundleReference implementation so that the
     * FrameworkUtils class may be used on this implementation.
     *
     * @return the backing bundle
     */
    public Bundle getBundle() {
        return backingBundle;
    }

}
