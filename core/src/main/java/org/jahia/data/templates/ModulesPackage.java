package org.jahia.data.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.commons.Version;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.osgi.framework.BundleException;
import org.osgi.framework.VersionRange;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This represents a package of modules.
 * A package of module is an archive that contains Jahia modules (jar files) and a description File package.xml
 * here a sample structure of the description file :
 * <p>&lt;package&gt;<br />
 * &nbsp;&nbsp; &nbsp;&lt;packageDescription&gt;This is a sample package&lt;/packageDescription&gt;<br />
 * &nbsp;&nbsp; &nbsp;&lt;packageVersion&gt;1.0-SNAPSHOT&lt;/packageVersion&gt;<br />
 * &nbsp;&nbsp; &nbsp;&lt;modules&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;artifactId&gt;article&lt;/artifactId&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;versionRange&gt;[1.0,3.0]&lt;/versionRange&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;/module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;artifactId&gt;contact&lt;/artifactId&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;versionRange&gt;[1.0,3.0]&lt;/versionRange&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;/module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;artifactId&gt;bookmarks&lt;/artifactId&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;versionRange&gt;[1.0,3.0]&lt;/versionRange&gt;<br />
 * &nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&lt;/module&gt;<br />
 * &nbsp;&nbsp; &nbsp;&lt;/modules&gt;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; &nbsp;<br />
 * &lt;/package&gt;</p>
 * <p>
 * where for the package :<br />
 * - &lt;packageVersion&gt; is the version of the current package (not used yet)<br />
 * - &lt;packageDescription&gt; is the description of the package (not used yet)<br />
 * - &lt;modules&gt; contains the description of the modules in the package<br />
 * </p>
 * <p>
 * for a module :<br />
 * - &lt;artifactId&gt; is the artifactID of the module, it corresponds to the module filename without the version<br />
 * - &lt;versionRange&gt; is the version range for the module allowed to be used by the package<br />
 * </p>
 */
public class ModulesPackage {

    private List<Module> modules;
    private String description;
    private Version version;
    private File zipFile;

    /**
     * Creates a new modules package from a ZipFile
     * It reads the description file to build it.
     *
     * @param zipFile is the archive that contains modules.
     * @throws BundleException
     */
    public ModulesPackage(File zipFile) throws BundleException {
        org.jahia.utils.zip.ZipEntry z;
        modules = new ArrayList<Module>();
        try {
            NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            while ((z = zis2.getNextEntry()) != null) {
                try {
                    if (StringUtils.equals("package.xml", z.getName())) {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(zis2);
                        description = doc.getElementsByTagName("packageDescription").item(0).getTextContent();
                        version = new Version(doc.getElementsByTagName("packageVersion").item(0).getTextContent());
                        NodeList moduleList = doc.getElementsByTagName("module");
                        for (int i = 0; i < moduleList.getLength(); i++) {
                            Node mod = moduleList.item(i);
                            NodeList modDetails = mod.getChildNodes();
                            Module module = new Module();
                            for (int j = 0; j < modDetails.getLength(); j++) {
                                Node modDetail = modDetails.item(j);
                                if (StringUtils.equals(modDetail.getNodeName(), "artifactId")) {
                                    module.setAtrifactId(modDetail.getTextContent());
                                }
                                if (StringUtils.equals(modDetail.getNodeName(), "versionRange")) {
                                    module.setVersionRange(new VersionRange(modDetail.getTextContent()));
                                }
                                if (StringUtils.equals(modDetail.getNodeName(), "url")) {
                                    module.setRemoteUrl(modDetail.getTextContent());
                                }
                            }
                            modules.add(module);
                        }
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } finally {
                    zis2.closeEntry();
                    IOUtils.closeQuietly(zis2);
                }
                this.zipFile = zipFile;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method gets a file from the archive according to its name only
     * the name of the file, is the name before the version (separator is "-")
     *
     * @param fileName file to fetch
     * @return the file
     */
    public File fetchFile(String fileName) {
        org.jahia.utils.zip.ZipEntry z;
        try {
            NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            try {
                while ((z = zis2.getNextEntry()) != null) {
                    if (StringUtils.startsWith(z.getName(), fileName + "-")) {
                        File jarFile = File.createTempFile(z.getName(), "");
                        OutputStream out = new FileOutputStream(jarFile);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis2.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        out.flush();
                        out.close();
                        return jarFile;
                    }
                }
            } finally {
                zis2.closeEntry();
                IOUtils.closeQuietly(zis2);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * returns the list of Jahia modules within the package
     *
     * @return
     */
    public List<Module> getModules() {
        return modules;
    }

    public String getDescription() {
        return description;
    }

    public Version getVersion() {
        return version;
    }

    /**
     * This is the representation of a Module within a ModulesPackage
     */
    public class Module {
        private VersionRange versionRange;
        private String atrifactId;
        private String remoteUrl;

        public String getRemoteUrl() {
            return remoteUrl;
        }

        public void setRemoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
        }

        public String getAtrifactId() {
            return atrifactId;
        }

        public void setAtrifactId(String atrifactId) {
            this.atrifactId = atrifactId;
        }

        public void setVersionRange(VersionRange versionRange) {
            this.versionRange = versionRange;
        }

        public VersionRange getVersionRange() {
            return versionRange;
        }
    }
}
