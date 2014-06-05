package org.jahia.data.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jahia.commons.Version;
import org.jahia.utils.PomUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
 */
public class ModulesPackage {

    private List<Model> modules;
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
    public static ModulesPackage create(JarFile jarFile) throws IOException, XmlPullParserException {
        return new ModulesPackage(jarFile);
    }

    private ModulesPackage(JarFile jarFile) throws IOException, XmlPullParserException {
        modules = new ArrayList<Model>();
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
                    modules.add(PomUtils.read(PomUtils.extractPomFromJar(moduleJarFile, moduleManifestAttributes.getValue("Jahia-GroupId"), moduleManifestAttributes.getValue("Bundle-SymbolicName"))));
                } finally {
                    IOUtils.closeQuietly(output);
                    IOUtils.closeQuietly(moduleJarFile);
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

    public List<Model> getModules() {
        return modules;
    }

    public String getDescription() {
        return description;
    }

    public Version getVersion() {
        return version;
    }

}
