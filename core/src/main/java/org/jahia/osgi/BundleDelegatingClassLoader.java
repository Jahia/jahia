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

/**
 * This class was copied from the Spring OSGi sources since it was the only file we needed.
 */
package org.jahia.osgi;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassLoader backed by an OSGi bundle. Provides the ability to use a separate
 * class loader as fall back.
 *
 * @author Adrian Colyer
 * @author Andy Piper
 * @author Costin Leau
 */
class BundleDelegatingClassLoader extends ClassLoader {

	/** use degradable logger */
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
	 * load a class or find a resource. Can be <code>null</code>
	 * @return class loader adapter over the given bundle and class loader
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
		return (BundleDelegatingClassLoader) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				return new BundleDelegatingClassLoader(bundle, bridge);
			}
		});
	}

	/**
	 * Private constructor.
	 *
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

	protected Class findClass(String name) throws ClassNotFoundException {
		try {
			return this.backingBundle.loadClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]",
				cnfe);
		}
		catch (NoClassDefFoundError ncdfe) {
			// This is almost always an error
			// This is caused by a dependent class failure,
			// so make sure we search for the right one.
			String cname = ncdfe.getMessage().replace('/', '.');
			NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle ["
					+ backingBundle + "]");
			e.initCause(ncdfe);
			throw e;
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

	protected Enumeration findResources(String name) throws IOException {
		boolean trace = log.isTraceEnabled();

		if (trace)
			log.trace("Looking for resources " + name);

		Enumeration enm = this.backingBundle.getResources(name);

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
        Enumeration enm = findResources(name);
        if (bridge != null && enm == null) {
            enm = bridge.getResources(name);
        }
        return enm;
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class clazz = null;
		try {
			clazz = findClass(name);
		}
		catch (ClassNotFoundException cnfe) {
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
	 *
	 * @return the backing bundle
	 */
	public Bundle getBundle() {
		return backingBundle;
	}

}