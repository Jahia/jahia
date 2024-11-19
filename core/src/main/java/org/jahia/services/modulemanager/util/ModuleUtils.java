/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.models.JahiaDepends;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.jahia.services.modulemanager.Constants.*;

/**
 * Utility class that contains common module methods for persisting bundle and transforming Jahia-Depends header values of module bundles into corresponding Require-Capability header and also adding
 * the Provide-Capability for the module itself.
 */
public class ModuleUtils {

    private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);
    protected static final Collection<String> DEPENDENCIES_TO_REMOVE = Arrays.asList("jaxrs-osgi-extender");

    private ModuleUtils() {
    }

    /**
     * Modifies the manifest attributes for Provide-Capability and Require-Capability (if needed) based on the module dependencies.
     *
     * @param atts the manifest attributes to be modified
     * @return <code>true</code> if the manifest attributes were modified; <code>false</code> if nothing was touched
     */
    static boolean addCapabilities(Attributes atts) {
        if (!requiresTransformation(atts)) {
            // the manifest already contains the required capabilities thus no modification is needed
            return false;
        }
        String moduleId = atts.getValue(ATTR_NAME_BUNDLE_SYMBOLIC_NAME);
        cleanupJahiaDepends(atts);
        populateProvideCapabilities(moduleId, atts);
        populateRequireCapabilities(moduleId, atts);
        return true;
    }

    private static void cleanupJahiaDepends(Attributes atts) {
        if (atts.getValue(ATTR_NAME_JAHIA_DEPENDS) != null) {
            String[] split = StringUtils.split(atts.getValue(ATTR_NAME_JAHIA_DEPENDS), ",");
            if (Arrays.stream(split).anyMatch(DEPENDENCIES_TO_REMOVE::contains)) {
                atts.putValue(ATTR_NAME_JAHIA_DEPENDS.toString(), Arrays.stream(split)
                        .filter(s -> !DEPENDENCIES_TO_REMOVE.contains(s))
                        .collect(Collectors.joining(",")));
            }
        }
    }

    /**
     * Performs the transformation of the capability attributes in the MANIFEST.MF file of the supplied stream.
     *
     * @param sourceStream the source stream for the bundle, which manifest has to be adjusted w.r.t. module dependencies; the stream is
     *            closed after returning from this method
     * @return the transformed stream for the bundle with adjusted manifest
     * @throws IOException in case of I/O errors
     */
    public static InputStream addModuleDependencies(InputStream sourceStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(sourceStream); ZipOutputStream zos = new ZipOutputStream(out);) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                if (JarFile.MANIFEST_NAME.equals(zipEntry.getName())) {
                    // we read the manifest from the source stream
                    Manifest mf = new Manifest();
                    mf.read(zis);

                    addCapabilities(mf.getMainAttributes());

                    // write the manifest entry into the target output stream
                    mf.write(zos);
                } else {
                    IOUtils.copy(zis, zos);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Builds a single clause for the Provide-Capability header.
     *
     * @param dependency the dependency to use in the clause
     * @return a single clause for the Require-Capability header
     */
    static String buildClauseProvideCapability(String dependency, String version) {
        // e.g. com.jahia.modules.dependencies;moduleIdentifier="<id>";module-version:Version=<version>
        String prefix = String.format("%s;%s=\"", OSGI_CAPABILITY_MODULE_DEPENDENCIES,
                OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        StringBuilder strBuilder = new StringBuilder(prefix).append(dependency).append('\"');
        if (StringUtils.isNotEmpty(version)) {
            strBuilder.append(';')
                    .append(OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY)
                    .append(":Version=").append(version);
        }
        return strBuilder.toString();
    }

    /**
     * Builds a single clause for the Require-Capability header.
     *
     * @param dependency the dependency to use in the clause
     * @return a single clause for the Require-Capability header
     */
    static String buildClauseRequireCapability(String dependency) {
        JahiaDepends depends = new JahiaDepends(dependency);
        StringBuilder sb = new StringBuilder(OSGI_CAPABILITY_MODULE_DEPENDENCIES)
                .append(";filter:=\"").append(depends.toFilterString()).append("\"");
        if (depends.isOptional()) {
            sb.append(";resolution:=\"optional\"");
        }
        return sb.toString();
    }

    private static File getBundleFile(Bundle bundle) {
        // The most reliable way to get the bundle JAR file is to access the Felix's
        // Bundle.getArchive().getCurrentRevision().getContent().getFile().
        // As we do not have a compile time dependency to Felix, we use reflection here.
        try {
            Method m = bundle.getClass().getDeclaredMethod("getArchive");
            m.setAccessible(true);
            return (File) BeanUtilsBean.getInstance().getPropertyUtils().getProperty(m.invoke(bundle),
                    "currentRevision.content.file");
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException
                | IllegalArgumentException e) {
            logger.error("Unable to detect the file for the deployed bundle " + bundle + ". Cause: " + e.getMessage(),
                    e);
            // we do not propagate here the exception; will use the bundle.getLocation() instead
        }
        return null;
    }

    private static BundlePersister getBundlePersister() {
        return (BundlePersister) SpringContextSingleton
                .getBean("org.jahia.services.modulemanager.persistence.BundlePersister");
    }

    /**
     * Retrieves an instance of the module manager.
     *
     * @return an instance of the module manager
     */
    public static ModuleManager getModuleManager() {
        return (ModuleManager) SpringContextSingleton.getBean("ModuleManager");
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
    private static List<String> getRequireCapabilities(String moduleId, String requiredJahiaVersion, String dependencies) {
        List<String> capabilities = new LinkedList<>();
        Set<String> dependsList = new LinkedHashSet<>();

        if (requiredJahiaVersion != null) {
            Version v = new Version(requiredJahiaVersion);
            String lowerBound = v.getMajor() + "." + v.getMinor();
            String upperBound = Integer.toString(v.getMajor() + 1);
            capabilities.add(OSGI_CAPABILITY_SERVER + ";filter:=\"(&("+ OSGI_CAPABILITY_SERVER_VERSION + ">="+ lowerBound + ")(!("+ OSGI_CAPABILITY_SERVER_VERSION + ">="+ upperBound + ")))\"");
        }

        // build the set of provided dependencies
        if (StringUtils.isNotBlank(dependencies)) {
            String[] depList = ModuleUtils.toDependsArray(dependencies);
            for (String dependency : depList) {
                dependsList.add(dependency.trim());
            }
        }

        // check if we need to automatically add dependency to default module
        boolean addDependencyToDefault = !dependsList.contains(JahiaTemplatesPackage.ID_DEFAULT)
                && !dependsList.contains(JahiaTemplatesPackage.NAME_DEFAULT)
                && !getModulesWithNoDefaultDependency().contains(moduleId);

        if (addDependencyToDefault || !dependsList.isEmpty()) {
            if (addDependencyToDefault) {
                capabilities.add(buildClauseRequireCapability(JahiaTemplatesPackage.ID_DEFAULT));
            }
            for (String dependency : dependsList) {
                capabilities.add(buildClauseRequireCapability(dependency));
            }
        }

        return capabilities;
    }

    /**
     *
     * @return Replace dependency separator with DEPENDENCY_DELIMITER instead of comma
     * so it doesn't conflict with version range separator
     *
     * e.g. 'module1=[1.2,2),module2,module3=4'
     * to 'module1=[1.2,2);module2;module3=4'
     */
    public static String replaceDependsDelimiter(String dependsValue) {
        List<String> result = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(dependsValue, ",");
        String nextToken = null;
        while (nextToken != null || tokens.hasMoreTokens()) {
            String token = (nextToken != null) ? nextToken : tokens.nextToken();
            nextToken = (tokens.hasMoreTokens()) ? tokens.nextToken() : null;
            if (!token.contains("=")) {
                result.add(token);
                continue;
            }

            String dependency = token;
            String[] deps = token.split("=");
            // remove 'optional' tag (if it exists) when checking open clause
            if (JahiaDepends.isOpenClause(deps[1].replace("optional;", ""))) {
                String nextTokenStr = (nextToken != null) ? nextToken : "";
                dependency = String.format("%s,%s", token, nextTokenStr);
                nextToken = null;
            }
            JahiaDepends.parse(dependency); // try to parse; will throw error if invalid format
            result.add(dependency);
        }
        return String.join(DEPENDENCY_DELIMITER, result);
    }

    public static String[] toDependsArray(String dependsValue) {
        dependsValue = ModuleUtils.replaceDependsDelimiter(dependsValue);
        return dependsValue.split(DEPENDENCY_DELIMITER);
    }

    /**
     * Load the bundle .jar resource.
     *
     * @param bundle the bundle to be loaded
     * @return the bundle resource
     * @throws java.net.MalformedURLException
     */
    public static Resource loadBundleResource(Bundle bundle) throws MalformedURLException {
        try {
            Resource bundleResource = null;
            File bundleFile = getBundleFile(bundle);
            if (bundleFile != null) {
                bundleResource = new FileSystemResource(bundleFile);
            } else {
                bundleResource = new UrlResource(bundle.getLocation());
            }
            return bundleResource;
        } catch (Exception e) {
            if (e instanceof ModuleManagementException) {
                // re-throw
                throw (ModuleManagementException) e;
            }
            String msg = "Unable to load bundle resource using location " + bundle.getLocation() + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }
    }

    /**
     * Returns the persistent bundle info for the specified key.
     *
     * @param bundleKey the key of the bundle to look up
     * @return the persistent bundle info for the specified key
     * @throws ModuleManagementException in case of a lookup error
     */
    public static PersistentBundle loadPersistentBundle(String bundleKey) throws ModuleManagementException {
        try {
            return getBundlePersister().find(bundleKey);
        } catch (Exception e) {
            if (e instanceof ModuleManagementException) {
                // re-throw
                throw e;
            }
            throw new ModuleManagementException(
                    "Unable to load persistent bundle for key " + bundleKey + ". Cause: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the persistence of the supplied bundle and returns the information about it.
     *
     * @param bundle the source bundle
     * @return information about persisted bundle
     * @throws ModuleManagementException in case of an error during persistence of the bundle
     */
    public static PersistentBundle persist(Bundle bundle) throws ModuleManagementException {
        try {
            return persist(loadBundleResource(bundle));
        } catch (Exception e) {
            if (e instanceof ModuleManagementException) {
                // re-throw
                throw (ModuleManagementException) e;
            }
            String msg = "Unable to persist bundle " + bundle + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }
    }

    /**
     * Performs the persistence of the supplied bundle resource and returns the information about it.
     *
     * @param bundleResource the source bundle resource
     * @return information about persisted bundle
     * @throws ModuleManagementException in case of an error during persistence of the bundle
     */
    public static PersistentBundle persist(Resource bundleResource) throws ModuleManagementException {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Persisting from resource {}", bundleResource);
            PersistentBundle persistedBundle = getBundlePersister().store(bundleResource);
            logger.debug("Bundle resource has been successfully persisted under {} in {} ms",
                    persistedBundle.getLocation(), System.currentTimeMillis() - startTime);
            return persistedBundle;
        } catch (Exception e) {
            String msg = "Unable to persist bundle from resource " + bundleResource + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }
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

        String bundleVersion = atts.getValue(ATTR_NAME_BUNDLE_VERSION);
        provide.append(buildClauseProvideCapability(moduleId, bundleVersion));
        String bundleName = atts.getValue(ATTR_NAME_BUNDLE_NAME);
        if (StringUtils.isNotEmpty(bundleName)) {
            provide.append(",").append(buildClauseProvideCapability(bundleName, bundleVersion));
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
        List<String> caps = getRequireCapabilities(moduleId, atts.getValue(ATTR_NAME_JAHIA_REQUIRED_VERSION), atts.getValue(ATTR_NAME_JAHIA_DEPENDS));

        if (!caps.isEmpty()) {
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
    public static boolean requiresTransformation(Attributes atts) {
        return (!StringUtils.contains(atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY), OSGI_CAPABILITY_MODULE_DEPENDENCIES)
                || !StringUtils.contains(atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY), OSGI_CAPABILITY_SERVER))
                && !atts.containsKey(ATTR_NAME_FRAGMENT_HOST);
    }

    /**
     * Performs the update of the bundle original location.
     *
     * @param bundle the bundle to update location for
     * @param updatedLocation the new value of the location
     */
    public static void updateBundleLocation(Bundle bundle, String updatedLocation) {
        // We access Felix's Bundle.getArchive() here.
        // As we do not have a compile time dependency to Felix, we use reflection.
        try {
            Method m = bundle.getClass().getDeclaredMethod("getArchive");
            m.setAccessible(true);
            Object archive = m.invoke(bundle);
            Field locationField = archive.getClass().getDeclaredField("m_originalLocation");
            locationField.setAccessible(true);
            locationField.set(archive, updatedLocation);
            // calling setLastModified() method on the BundleArchive finally writes the updated bundle info into bundle.info file,
            // also updating location value
            archive.getClass().getDeclaredMethod("setLastModified", long.class).invoke(archive,
                    System.currentTimeMillis());
        } catch (NoSuchMethodException | NoSuchFieldException | SecurityException | IllegalAccessException
                | InvocationTargetException | IllegalArgumentException e) {
            logger.error("Unable update the location for bundle " + bundle + ". Cause: " + e.getMessage(), e);
            throw new RuntimeException("Unable update the location for bundle " + bundle + ". Cause: " + e.getMessage(),
                    e);
        }
    }

}
