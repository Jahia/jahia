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
package org.jahia.bundles.extender.jahiamodules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.apache.poi.util.IOUtils;
import org.jahia.registries.ServicesRegistry;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * Artifact URL transformer that transforms Jahia-Depends header values into corresponding Require-Capability header and also adds the
 * Provide-Capability for the module itself.
 */
public class ModuleDependencyTransformer extends AbstractURLStreamHandlerService implements ArtifactUrlTransformer {

    /**
     * A wrapper class for the underlying {@link URLConnection} to perform MANIFEST.MF modifications on the fly.
     */
    private static class TransformedURLConnection extends URLConnection {

        /**
         * Initializes an instance of this class.
         * 
         * @param url
         *            the URL to be transformed
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (ZipInputStream zis = new ZipInputStream(new URL(url.getFile()).openConnection().getInputStream())) {
                ZipOutputStream zos = new ZipOutputStream(out);

                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                    if ("META-INF/MANIFEST.MF".equals(zipEntry.getName())) {
                        addDependsCapabilitiesToManifest(zis, zos);
                    } else {
                        IOUtils.copy(zis, zos);
                    }
                    zis.closeEntry();
                }
                zos.close();
            }
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * The URL protocol name for the module dependency handler.
     */
    public static final String HANDLER_PREFIX = "legacydepends";

    /**
     * Modifies the manifest attributes for Provide-Capability and Require-Capability based on the module dependencies.
     * 
     * @param is
     *            the original JAR input stream
     * @param os
     *            the target output stream
     * @throws IOException
     *             in case of an I/O error
     */
    private static void addDependsCapabilitiesToManifest(InputStream is, OutputStream os) throws IOException {
        Manifest mf = new Manifest();
        mf.read(is);
        Attributes atts = mf.getMainAttributes();

        String bundleId = atts.getValue("Bundle-SymbolicName");

        populateProvideCapabilities(bundleId, atts);

        populateRequireCapabilities(bundleId, atts);

        mf.write(os);
    }

    /**
     * Calculates the value and set the Provide-Capability manifest attribute.
     * 
     * @param bundleId
     *            the ID of the module.
     * @param atts
     *            the manifest attributes
     */
    private static void populateProvideCapabilities(String bundleId, Attributes atts) {
        Attributes.Name provideCapabilityAttrName = new Attributes.Name("Provide-Capability");

        StringBuilder provide = new StringBuilder(256);
        String existingProvideValue = atts.getValue(provideCapabilityAttrName);
        if (StringUtils.isNotEmpty(existingProvideValue)) {
            provide.append(existingProvideValue).append(",");
        }
        if (existingProvideValue == null || !existingProvideValue.contains("com.jahia.modules.dependencies")) {
            provide.append("com.jahia.modules.dependencies;moduleIdentifier=\"").append(bundleId).append("\"");
            String bundleName = atts.getValue("Bundle-Name");
            if (StringUtils.isNotEmpty(bundleName)) {
                provide.append(",com.jahia.modules.dependencies;moduleIdentifier=\"").append(bundleName).append("\"");
            }
            atts.put(provideCapabilityAttrName, provide.toString());
        }
    }

    /**
     * Calculates the value and set the Require-Capability manifest attribute.
     * 
     * @param bundleId
     *            the ID of the module.
     * @param atts
     *            the manifest attributes
     */
    private static void populateRequireCapabilities(String bundleId, Attributes atts) {
        Set<String> dependsList = new LinkedHashSet<>();
        String deps = atts.getValue("Jahia-Depends");
        if (StringUtils.isNotBlank(deps)) {
            String[] dependencies = StringUtils.split(deps, ", ");
            for (String dependency : dependencies) {
                dependsList.add(dependency);
            }
        }

        if (!dependsList.contains("default") && !dependsList.contains("Default Jahia Templates")
                && !ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency()
                        .contains(bundleId)) {
            dependsList.add("default");
        }

        if (!dependsList.isEmpty()) {
            StringBuilder require = new StringBuilder(256);
            Attributes.Name requireCapabilityAttrName = new Attributes.Name("Require-Capability");
            String existingRequireValue = atts.getValue(requireCapabilityAttrName);
            if (StringUtils.isNotEmpty(existingRequireValue)) {
                require.append(existingRequireValue).append(",");
            }
            if (existingRequireValue == null || !existingRequireValue.contains("com.jahia.modules.dependencies")) {
                for (String depend : dependsList) {
                    require.append("com.jahia.modules.dependencies;filter:=\"(moduleIdentifier=").append(depend)
                            .append(")\",");
                }
                require.deleteCharAt(require.length() - 1);
                atts.put(requireCapabilityAttrName, require.toString());
            }
        }
    }

    @Override
    public boolean canHandle(File file) {
        if (file == null || !file.getName().endsWith(".jar")) {
            // we are not dealing with non-JAR files -> return
            return false;
        }
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            Manifest mf = jar.getManifest();
            if (mf != null) {
                Attributes attrs = mf.getMainAttributes();
                // it should be our module and it should not contain the capabilities for the module dependencies yet
                return attrs.getValue("Jahia-Module-Type") != null && !StringUtils
                        .contains(attrs.getValue("Provide-Capability"), "com.jahia.modules.dependencies");
            }
        } catch (IOException e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(jar);
        }

        return false;
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return new TransformedURLConnection(url);
    }

    @Override
    public URL transform(URL url) throws Exception {
        return new URL(HANDLER_PREFIX, null, url.toString());
    }
}
