package org.jahia.data.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
    private JarFile jarFile;

    /**
     * creates a new ModulesPackage from a jar file.
     * All the information are loaded
     *
     * @param jarFile the package jar file
     * @return a ModulesPackage
     * @throws IOException
     * @throws XmlPullParserException
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
                    modules.put(moduleManifestAttributes.getValue("Bundle-SymbolicName"), new PackagedModule(moduleManifestAttributes, moduleFile));
                } finally {
                    IOUtils.closeQuietly(output);
                    if (moduleJarFile != null) {
                        moduleJarFile.close();
                    }
                }
            }
        }
        this.jarFile = jarFile;
    }

    /**
     * This method gets a file from the archive according to its artifactID and version
     *
     * @return the file
     */
    public File fetchFile(String artifactId, String version) throws IOException {
        OutputStream output = null;
        InputStream input = null;
        try {
            String fileName = artifactId + "-" + version + ".jar";
            JarEntry jarEntry = jarFile.getJarEntry(fileName);
            input = jarFile.getInputStream(jarEntry);
            File moduleFile = File.createTempFile(fileName, "");
            output = new FileOutputStream(moduleFile);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return moduleFile;
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
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
