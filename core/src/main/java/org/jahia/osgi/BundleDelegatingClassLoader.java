/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

            @Override
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            synchronized (backingBundle) {
                return backingBundle.loadClass(name);
            }
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
        }
    }

    @Override
    protected URL findResource(String name) {
        log.trace("Looking for resource {}", name);
        URL url;
        synchronized (backingBundle) {
            url = backingBundle.getResource(name);
        }
        if (url != null) {
            log.trace("Found resource {} at {}", name, url);
        }
        return url;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        log.trace("Looking for resources {}", name);
        Enumeration<URL> enm;
        synchronized (backingBundle) {
            enm = backingBundle.getResources(name);
        }
        if (enm != null && enm.hasMoreElements()) {
            log.trace("Found resource {} at {}", name, backingBundle.getLocation());
        }
        return enm;
    }

    @Override
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

    @Override
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

    @Override
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
    @Override
    public Bundle getBundle() {
        return backingBundle;
    }
}