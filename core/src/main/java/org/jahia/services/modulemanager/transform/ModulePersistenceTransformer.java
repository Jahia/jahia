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
package org.jahia.services.modulemanager.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.osgi.framework.Bundle;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Utility class that transforms the bundle URL by reading it from a persistence storage and applying additionally the module dependency
 * transformation using OSGi capabilities (see {@link ModuleDependencyTransformer}}.
 */
public class ModulePersistenceTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ModulePersistenceTransformer.class);

    private static class TransformedURLConnection extends URLConnection {

        /**
         * Initializes an instance of this class.
         *
         * @param url the URL to be transformed
         */
        protected TransformedURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            // Do nothing
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String bundleKey = url.getFile();
            try {
                return ModuleDependencyTransformer.getTransformedInputStream(getBundlePersister().getInputStream(bundleKey));
            } catch (ModuleManagementException e) {
                logger.warn("Couldn't resolve the {}: protocol path for: {}", Constants.URL_PROTOCOL_DX, bundleKey);
                throw new IOException(e);
            }
        }
    }

    private static File getBundleFile(Bundle bundle) {
        try {
            Method m = bundle.getClass().getDeclaredMethod("getArchive");
            m.setAccessible(true);
            return (File) BeanUtilsBean.getInstance().getPropertyUtils().getProperty(m.invoke(bundle), "currentRevision.content.file");
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            logger.error("Unable to detect the file for the deployed bundle " + bundle + ". Cause: " + e.getMessage(), e);
            // we do not propagate here the exception; will use the bundle.getLocation() instead
        }
        return null;
    }

    protected static BundlePersister getBundlePersister() {
        return (BundlePersister) SpringContextSingleton.getBean("org.jahia.services.modulemanager.persistence.BundlePersister");
    }

    /**
     * @return an instance of the {@link URLStreamHandlerService} that can perform the transformation of bundle URL by reading it from a persistence storage
     */
    public static URLStreamHandlerService getURLStreamHandlerService() {

        return new AbstractURLStreamHandlerService() {

            @Override
            public URLConnection openConnection(URL url) throws IOException {
                return new TransformedURLConnection(url);
            }
        };
    }

    /**
     * Performs the persistence of the supplied bundle (if needed) and returns the transformed input stream of its content, including module
     * dependency transformation (see {@link ModuleDependencyTransformer}).
     *
     * @param bundle the source bundle
     * @return the transformed input stream of its content
     */
    public static InputStream transform(Bundle bundle) {
        long startTime = System.currentTimeMillis();
        Resource bundleResource = null;
        try {
            File bundleFile = getBundleFile(bundle);
            if (bundleFile != null) {
                bundleResource = new FileSystemResource(bundleFile);
            } else {
                bundleResource = new UrlResource(bundle.getLocation());
            }
            logger.info("Persisting bundle {} from resource {}", bundle, bundleResource);
            PersistentBundle persistedBundle = getBundlePersister().store(bundleResource);
            logger.info("Bundle {} has been successfully transformed into {} in {} ms", new Object[] { bundle, persistedBundle.getLocation(), System.currentTimeMillis() - startTime });
            return ModuleDependencyTransformer.getTransformedInputStream(getBundlePersister().getInputStream(persistedBundle.getKey()));
        } catch (Exception e) {
            String msg = "Unable to transform bundle " + bundle + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }
    }

    /**
     * Performs the persistence of the supplied bundle (if needed) and modifies its URL.
     *
     * @param artifact the source bundle URL
     * @return the modified bundle URL which uses persistence or the original one if no modification is needed
     */
    public static URL transform(URL artifact) {

        if (Constants.URL_PROTOCOL_DX.equals(artifact.getProtocol())) {
            // no need to transform it
            return artifact;
        }

        URL transformed = null;
        long startTime = System.currentTimeMillis();
        logger.info("Transforming artifact {}", artifact);
        try {
            PersistentBundle persistedBundle = getBundlePersister().store(new UrlResource(artifact));
            transformed = new URL(persistedBundle.getLocation());
            logger.info("Artifact {} has been successfully transformed into {} in {} ms", new Object[] { artifact, transformed, System.currentTimeMillis() - startTime });
        } catch (Exception e) {
            String msg = "Unable to transform artifact " + artifact + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }

        return transformed != null ? transformed : artifact;
    }
}