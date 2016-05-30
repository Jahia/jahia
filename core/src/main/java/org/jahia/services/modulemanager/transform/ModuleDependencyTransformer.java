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

import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_NAME;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_SYMBOLIC_NAME;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_FRAGMENT_HOST;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_JAHIA_DEPENDS;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_PROVIDE_CAPABILITY;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_REQUIRE_CAPABILITY;
import static org.jahia.services.modulemanager.Constants.OSGI_CAPABILITY_MODULE_DEPENDENCIES;
import static org.jahia.services.modulemanager.Constants.OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Utility class that transforms Jahia-Depends header values of module bundles into corresponding Require-Capability header and also adds
 * the Provide-Capability for the module itself.
 */
public class ModuleDependencyTransformer {

    /**
     * A wrapper class for the underlying {@link URLConnection} to perform MANIFEST.MF modifications on the fly.
     */
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
            return getTransformedInputStream(new URL(url.getFile()).openConnection().getInputStream());
        }
    }

    /**
     * Modifies the manifest attributes for Provide-Capability and Require-Capability (if needed) based on the module dependencies.
     *
     * @param atts the manifest attributes to be modified
     *
     * @return <code>true</code> if the manifest attributes were modified; <code>false</code> if nothing was touched
     */
    static boolean addCapabilities(Attributes atts) {
        if (!requiresTransformation(atts)) {
            // the manifest already contains the required capabilities thus no modification is needed
            return false;
        }
        String moduleId = atts.getValue(ATTR_NAME_BUNDLE_SYMBOLIC_NAME);
        populateProvideCapabilities(moduleId, atts);
        populateRequireCapabilities(moduleId, atts);
        return true;
    }

    /**
     * Modifies the manifest attributes for Provide-Capability and Require-Capability based on the module dependencies (if needed).
     *
     * @param is the original JAR input stream
     * @param os the target output stream
     * @throws IOException in case of an I/O error
     */
    private static void addDependsCapabilitiesToManifest(InputStream is, OutputStream os) throws IOException {

        // we read the manifest from the source stream
        Manifest mf = new Manifest();
        mf.read(is);

        // adjust the manifest headers
        addCapabilities(mf.getMainAttributes());

        // write the manifest entry into the target output stream
        mf.write(os);
    }

    /**
     * Builds a single clause for the Provide-Capability header.
     *
     * @param dependency the dependency to use in the clause
     * @return a single clause for the Require-Capability header
     */
    static String buildClauseProvideCapability(String dependency) {
        return new StringBuilder()
                .append(OSGI_CAPABILITY_MODULE_DEPENDENCIES + ";" + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY + "=\"")
                .append(dependency).append("\"").toString();
    }

    /**
     * Builds a single clause for the Require-Capability header.
     *
     * @param dependency the dependency to use in the clause
     * @return a single clause for the Require-Capability header
     */
    static String buildClauseRequireCapability(String dependency) {
        return new StringBuilder().append(
                OSGI_CAPABILITY_MODULE_DEPENDENCIES + ";filter:=\"(" + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY + "=")
                .append(dependency).append(")\"").toString();
    }

    private static Set<String> getModulesWithNoDefaultDependency() {
        if (SpringContextSingleton.getInstance().isInitialized()) {
            return ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency();
        } else {
            // only the case for the unit test execution
            return JahiaTemplateManagerService.DEFAULT_MODULES_WITH_NO_DEFAUL_DEPENDENCY;
        }
    }

    /**
     * Returns required capabilities for the specified module and its dependencies.
     *
     * @param moduleId the ID of the module
     * @param dependencies the module dependencies (comma-separated)
     */
    private static List<String> getRequireCapabilities(String moduleId, String dependencies) {

        List<String> capabilities = new LinkedList<>();
        Set<String> dependsList = new LinkedHashSet<>();

        // build the set of provided dependencies
        if (StringUtils.isNotBlank(dependencies)) {
            for (String dependency : StringUtils.split(dependencies, ",")) {
                dependsList.add(dependency.trim());
            }
        }

        // check if we need to automatically add dependency to default module
        boolean addDependencyToDefault = !dependsList.contains("default")
                && !dependsList.contains("Default Jahia Templates")
                && !getModulesWithNoDefaultDependency().contains(moduleId);

        if (addDependencyToDefault || !dependsList.isEmpty()) {
            if (addDependencyToDefault) {
                capabilities.add(buildClauseRequireCapability("default"));
            }
            for (String dependency : dependsList) {
                capabilities.add(buildClauseRequireCapability(dependency));
            }
        }

        return capabilities;
    }

    /**
     * Performs the transformation of the capability attributes in the MANIFEST.MF file of the supplied stream.
     *
     * @param sourceStream the source stream for the bundle, which manifest has to be adjusted w.r.t. module dependencies
     * @return the transformed stream for the bundle with adjusted manifest
     * @throws IOException in case of I/O errors
     */
    public static InputStream getTransformedInputStream(InputStream sourceStream) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(sourceStream)) {
            ZipOutputStream zos = new ZipOutputStream(out);

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                if (JarFile.MANIFEST_NAME.equals(zipEntry.getName())) {
                    addDependsCapabilitiesToManifest(zis, zos);
                } else {
                    IOUtils.copy(zis, zos);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            zos.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Returns an instance of the {@link URLStreamHandlerService} that is capable of transforming the manifest of the underlying module
     * bundle to handle module dependencies based on OSGi capabilities.
     *
     * @return an instance of the {@link URLStreamHandlerService} that is capable of transforming the manifest of the underlying module
     *         bundle to handle module dependencies based on OSGi capabilities
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
     * Calculates the value and set the Provide-Capability manifest attribute and modifies it.
     *
     * @param moduleId the ID of the module.
     * @param atts the manifest attributes
     */
    private static void populateProvideCapabilities(String moduleId, Attributes atts) {
        StringBuilder provide = new StringBuilder();
        String existingProvideValue = atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY);
        if (StringUtils.isNotEmpty(existingProvideValue)) {
            provide.append(existingProvideValue).append(",");
        }
        provide.append(buildClauseProvideCapability(moduleId));
        String bundleName = atts.getValue(ATTR_NAME_BUNDLE_NAME);
        if (StringUtils.isNotEmpty(bundleName)) {
            provide.append(",").append(buildClauseProvideCapability(bundleName));
        }
        atts.put(ATTR_NAME_PROVIDE_CAPABILITY, provide.toString());
    }

    /**
     * Calculates the value and set the Require-Capability manifest attribute.
     *
     * @param moduleId the ID of the module
     * @param atts the manifest attributes
     */
    private static void populateRequireCapabilities(String moduleId, Attributes atts) {
        List<String> caps = getRequireCapabilities(moduleId, atts.getValue(ATTR_NAME_JAHIA_DEPENDS));
        if (caps.size() > 0) {
            StringBuilder require = new StringBuilder();
            String existingRequireValue = atts.getValue(ATTR_NAME_REQUIRE_CAPABILITY);
            if (StringUtils.isNotEmpty(existingRequireValue)) {
                require.append(existingRequireValue);
            }
            for (String cap : caps) {
                if (require.length() > 0) {
                    require.append(",");
                }
                require.append(cap);
            }
            atts.put(ATTR_NAME_REQUIRE_CAPABILITY, require.toString());
        }
    }

    /**
     * Checks if the artifact manifest requires adjustments in the capability headers w.r.t. module dependencies.
     *
     * @param atts the manifest attributes to be checked
     *
     * @return <code>true</code> if the artifact manifest requires adjustments in the capability headers w.r.t. module dependencies;
     *         <code>false</code> if it already contains that info
     */
    private static boolean requiresTransformation(Attributes atts) {
        return !StringUtils.contains(atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY), OSGI_CAPABILITY_MODULE_DEPENDENCIES)
                && !atts.containsKey(ATTR_NAME_FRAGMENT_HOST);
    }
}
