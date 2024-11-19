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
package org.jahia.data.templates;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import org.jahia.commons.Version;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.modulemanager.Constants;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This represents a Package of modules.
 * A package of module is a jar file that contains jahia modules as jar file and a description file (MANIFEST.MF)
 * the structure of the package is :
 * - module1.jar
 * - module2.jar
 * - META-INF/MANIFEST.MF
 * The manifest entries are :
 * - Jahia-Package-Name : the name of the package
 * - Jahia-Package-Version : the version of the package
 * - Jahia-Package-Description : the description of the package
 * - Jahia-Required-Version : the required version of Jahia
 * - Jahia-Package-License : the required license to install this package
 */
@SuppressWarnings("java:S899")
public class ModulesPackage {

    private Map<String, PackagedModule> modules;
    private String name;
    private String description;
    private Version version;

    /**
     * creates a new ModulesPackage from a jar file.
     * All the information are loaded
     *
     * @param jarFile the package jar file
     * @return a ModulesPackage
     * @throws IOException
     */
    public static ModulesPackage create(JarFile jarFile) throws IOException {
        return new ModulesPackage(jarFile);
    }

    /**
     * Parses the corresponding manifest attribute and extracts package names.
     *
     * @param module        the module package to retrieve manifest attribute
     * @param attributeName the name of the manifest attribute to parse its value
     * @return the set of package names, parsed from the requested manifest attribute; an empty set is returned if no requested attribute is
     * found in manifest
     */
    private static Collection<String> getPackageNames(PackagedModule module, String attributeName) {
        String attributeValue = module.getManifestAttributes().getValue(attributeName);
        if (StringUtils.isEmpty(attributeValue)) {
            return Collections.emptySet();
        }
        Set<String> packageNames = new LinkedHashSet<>();
        for (Clause clause : Parser.parseHeader(attributeValue)) {
            packageNames.add(clause.getName());
        }
        return packageNames;
    }

    protected static void sortByDependencies(Map<String, PackagedModule> modules) throws CycleDetectedException {
        if (modules.size() <= 1) {
            return;
        }
        Map<String, PackagedModule> copy = new LinkedHashMap<>(modules);

        // we build a Directed Acyclic Graph of dependencies (only those, which are present in the package)
        DAG dag = new DAG();

        Map<String, String> exports = new HashMap<>();
        // collect the exported package names, exported by all provided modules
        for (PackagedModule module : modules.values()) {
            Collection<String> exportPackages = getPackageNames(module, org.osgi.framework.Constants.EXPORT_PACKAGE);
            for (String exportPackage : exportPackages) {
                exports.put(exportPackage, module.getName());
            }
        }

        // iterate on all modules in the package
        buildEdgeGraph(modules, dag, exports);

        // use topological sort (Depth First Search) on the created graph
        @SuppressWarnings("unchecked")
        List<String> vertexes = TopologicalSorter.sort(dag);

        modules.clear();
        for (String vertex : vertexes) {
            modules.put(vertex, copy.get(vertex));
        }
    }

    private static void buildEdgeGraph(Map<String, PackagedModule> modules, DAG dag, Map<String, String> exports) throws CycleDetectedException {
        for (PackagedModule module : modules.values()) {
            String moduleName = module.getName();
            dag.addVertex(moduleName);
            for (String dependency : module.getDepends()) {
                if (modules.containsKey(dependency)) {
                    // add a graph edge for module and its dependency
                    dag.addEdge(moduleName, dependency);
                }
            }
            Collection<String> importPackages = getPackageNames(module, org.osgi.framework.Constants.IMPORT_PACKAGE);
            for (String importPackageName : importPackages) {
                // if the imported package is exported by another module (not same one) and we do not have an edge in the graph, we add the
                // edge from this module to the module, which exports that package
                if (exports.containsKey(importPackageName) && !exports.get(importPackageName).equals(moduleName)
                        && !dag.hasEdge(moduleName, exports.get(importPackageName))) {
                    dag.addEdge(moduleName, exports.get(importPackageName));
                }
            }
        }
    }

    private ModulesPackage(JarFile jarFile) throws IOException {
        modules = new LinkedHashMap<>();
        Attributes manifestAttributes = jarFile.getManifest().getMainAttributes();
        version = new Version(manifestAttributes.getValue(Constants.ATTR_NAME_JAHIA_PACKAGE_VERSION));
        name = manifestAttributes.getValue(Constants.ATTR_NAME_JAHIA_PACKAGE_NAME);
        description = manifestAttributes.getValue(Constants.ATTR_NAME_JAHIA_PACKAGE_DESCRIPTION);
        // read jars
        Enumeration<JarEntry> jars = jarFile.entries();
        File slipZipControlDirectory = Files.createTempDirectory("slipZip").toFile();
        //deny for all
        slipZipControlDirectory.setReadable(false);
        slipZipControlDirectory.setWritable(false);
        slipZipControlDirectory.setExecutable(false);
        // allow for owner only
        slipZipControlDirectory.setExecutable(true, true);
        slipZipControlDirectory.setWritable(true, true);
        slipZipControlDirectory.setReadable(true, true);
        while (jars.hasMoreElements()) {
            JarEntry jar = jars.nextElement();
            JarFile moduleJarFile = null;
            if (StringUtils.endsWith(jar.getName(), ".jar")) {
                try (InputStream input = jarFile.getInputStream(jar)) {
                    File moduleFile = getModuleFile(slipZipControlDirectory, jar, input);
                    moduleJarFile = new JarFile(moduleFile);
                    Attributes moduleManifestAttributes = moduleJarFile.getManifest().getMainAttributes();
                    String bundleName = moduleManifestAttributes.getValue(Constants.ATTR_NAME_BUNDLE_SYMBOLIC_NAME);
                    if (bundleName == null) {
                        throw new IOException("Jar file " + jar.getName() + " in package does not seem to be a valid bundle.");
                    }
                    modules.put(bundleName, new PackagedModule(bundleName, moduleManifestAttributes, moduleFile));
                } finally {
                    if (moduleJarFile != null) {
                        moduleJarFile.close();
                    }
                }
            }
        }

        // we finally sort modules based on dependencies
        try {
            sortByDependencies(modules);
        } catch (CycleDetectedException e) {
            throw new JahiaRuntimeException("A cyclic dependency detected in the modules of the supplied package", e);
        }
    }

    private File getModuleFile(File slipZipControlFile, JarEntry jar, InputStream input) throws IOException {
        String canonicalSlipZipControlDirPath = slipZipControlFile.getCanonicalPath();
        File moduleFile = new File(canonicalSlipZipControlDirPath, jar.getName());
        if (!moduleFile.getCanonicalPath().startsWith(canonicalSlipZipControlDirPath + File.separator)) {
            throw new IOException("Jar entry is outside of the target directory");
        }
        Files.deleteIfExists(moduleFile.toPath());
        try (OutputStream output = new FileOutputStream(moduleFile)) {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
        }
        return moduleFile;
    }

    public String getName() {
        return name;
    }

    public Map<String, PackagedModule> getModules() {
        return modules;
    }

    public String getDescription() {
        return description;
    }

    public Version getVersion() {
        return version;
    }

    public static class PackagedModule {
        private final Attributes manifestAttributes;
        private final File moduleFile;
        private final String name;
        private final Set<String> depends;

        protected PackagedModule(String name, Attributes manifestAttributes, File moduleFile) {
            this.name = name;
            this.manifestAttributes = manifestAttributes;
            this.moduleFile = moduleFile;
            this.depends = parseDependencies(manifestAttributes);
        }

        public Attributes getManifestAttributes() {
            return manifestAttributes;
        }

        public File getModuleFile() {
            return moduleFile;
        }

        public String getName() {
            return name;
        }

        public Set<String> getDepends() {
            return depends;
        }

        private static Set<String> parseDependencies(Attributes manifestAttributes) {
            Set<String> dependencies = Collections.emptySet();
            String dependsValue = manifestAttributes.getValue(Constants.ATTR_NAME_JAHIA_DEPENDS);
            if (dependsValue != null && dependsValue.length() > 0) {
                dependencies = new LinkedHashSet<>();
                dependencies.addAll(Arrays.asList(StringUtils.split(dependsValue, ", ")));
            }

            return dependencies;
        }
    }
}
