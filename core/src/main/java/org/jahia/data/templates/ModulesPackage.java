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
package org.jahia.data.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.commons.Version;
import java.io.*;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private ModulesPackage(JarFile jarFile) throws IOException {
        modules = new LinkedHashMap<String, PackagedModule>();
        Attributes manifestAttributes = jarFile.getManifest().getMainAttributes();
        version = new Version(manifestAttributes.getValue("Jahia-Package-Version"));
        name = manifestAttributes.getValue("Jahia-Package-Name");
        description = manifestAttributes.getValue("Jahia-Package-Description");
        // read jars
        Enumeration<JarEntry> jars = jarFile.entries();

        while (jars.hasMoreElements()) {
            JarEntry jar = jars.nextElement();
            JarFile moduleJarFile = null;
            OutputStream output = null;
            if (StringUtils.endsWith(jar.getName(), ".jar")) {
                try {
                    InputStream input = jarFile.getInputStream(jar);
                    File moduleFile = File.createTempFile(jar.getName(), "");
                    output = new FileOutputStream(moduleFile);
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = input.read(bytes)) != -1) {
                        output.write(bytes, 0, read);
                    }
                    moduleJarFile = new JarFile(moduleFile);
                    Attributes moduleManifestAttributes = moduleJarFile.getManifest().getMainAttributes();
                    String bundleName = moduleManifestAttributes.getValue("Bundle-SymbolicName");
                    String jahiaGroupId = moduleManifestAttributes.getValue("Jahia-GroupId");
                    if(bundleName==null || jahiaGroupId==null) {
                        throw new IOException("Jar file "+jar.getName()+ " in package does not seems to be a Jahia bundle.");
                    }
                    modules.put(bundleName, new PackagedModule(moduleManifestAttributes, moduleFile));
                } finally {
                    IOUtils.closeQuietly(output);
                    if (moduleJarFile != null) {
                        moduleJarFile.close();
                    }
                }
            }
        }
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

    public class PackagedModule {
        private final Attributes manifestAttributes;
        private final File moduleFile;

        public PackagedModule(Attributes manifestAttributes, File moduleFile) {
            this.manifestAttributes = manifestAttributes;
            this.moduleFile = moduleFile;
        }

        public Attributes getManifestAttributes() {
            return manifestAttributes;
        }

        public File getModuleFile() {
            return moduleFile;
        }
    }
}
