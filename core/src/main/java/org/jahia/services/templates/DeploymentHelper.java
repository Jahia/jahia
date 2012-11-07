/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.templates;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.jahia.utils.zip.ExclusionWildcardFilter;
import org.jahia.utils.zip.JahiaArchiveFileHandler;
import org.jahia.utils.zip.PathFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

/**
 * Template package deployment helper.
 *
 * @author Sergiy Shyrkov
 * @author Thomas Draier
 */
class DeploymentHelper implements ServletContextAware {

    private class ChecksumFileFilter implements IOFileFilter {

        private File destDir;
        private File sourceDir;

        public ChecksumFileFilter(File sourceDir, File destDir) {
            this.sourceDir = sourceDir;
            this.destDir = destDir;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String sourceDirAbsPath = sourceDir.getAbsolutePath() + File.separator;
            if (file.getAbsolutePath().startsWith(sourceDirAbsPath)) {
                String fileRelativePath = file.getAbsolutePath().substring(sourceDirAbsPath.length());
                String fileDestPath = destDir.getAbsolutePath() + File.separator + fileRelativePath;
                try {
                    File destFile = new File(fileDestPath);
                    if (!destFile.exists()) {
                        return true;
                    }
                    return FileUtils.checksum(file, new CRC32()).getValue() != FileUtils.checksum(destFile, new CRC32()).getValue();
                } catch (IOException e) {
                    logger.error("Cannot compare CRC32 for file " + file.getName(), e);
                }
            }
            return false;
        }

        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }
    }

    private static class EmptyJarFilter implements IOFileFilter {

        protected static EmptyJarFilter INSTANCE = new EmptyJarFilter();

        public boolean accept(File file) {
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new FileInputStream(file));
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.getName().endsWith(".class")) {
                        return true;
                    }
                }
            } catch (IOException e) {
            } finally {
                IOUtils.closeQuietly(zis);
            }
            return false;
        }

        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }
    }

    private static Logger logger = LoggerFactory.getLogger(DeploymentHelper.class);

    private static final PathFilter TEMPLATE_FILTER = new ExclusionWildcardFilter("WEB-INF/web.xml", "META-INF/maven/*");

    private ServletContext servletContext;

    private SettingsBean settingsBean;


    /**
     * Create the fix descriptor as a XML file into the specified save location.
     */
    private void createDeploymentXMLFile(File installedFile, Map<String, String> deployedFiles, File packageWar, String packageName, String depends, String rootFolder, String implementationVersionStr, Calendar packageTimestamp) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(installedFile);
            Document description = getDOM(deployedFiles, packageWar, packageName, depends, rootFolder, implementationVersionStr, packageTimestamp);
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(description, os);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected File deployPackage(File templateWar) throws IOException {
        String packageName = null;
        String rootFolder = null;
        ModuleVersion version = new ModuleVersion("SNAPSHOT");
        String depends = null;
        Calendar packageTimestamp = Calendar.getInstance();

        Map<String, String> deployedFiles = new TreeMap<String, String>();
        JarFile jarFile = new JarFile(templateWar);
        try {
            Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
            packageName = (String) mainAttributes.get(new Attributes.Name("package-name"));
            rootFolder = (String) mainAttributes.get(new Attributes.Name("root-folder"));
            depends = jarFile.getManifest().getMainAttributes().getValue("depends");
            long manifestTime = jarFile.getEntry("META-INF/MANIFEST.MF").getTime();
            packageTimestamp.setTimeInMillis(manifestTime);
            version = new ModuleVersion((String) jarFile.getManifest().getMainAttributes().get(new Attributes.Name("Implementation-Version")));
        } catch (IOException e) {
            logger.warn("Cannot read MANIFEST file from " + templateWar, e);
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
                logger.warn("Error closing JAR file " + jarFile, e);
            }
        }
        if (packageName == null) {
            packageName = StringUtils.substringBeforeLast(templateWar.getName(), ".");
        }
        if (rootFolder == null) {
            rootFolder = StringUtils.substringBeforeLast(templateWar.getName(), ".");
        }

        String versionString = version.toString();

        File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), rootFolder);
        File versionFolder = new File(tmplRootFolder, versionString);

        if (!settingsBean.isDevelopmentMode()) {
            if (version.isSnapshot()) {
                logger.warn("Warning : deploying a snapshot version on a production server");
            } else if (versionFolder.exists()) {
                logger.warn("Module " + packageName + " is already deployed, skip deployment");
                return null;
            }
        }

        if (versionFolder.exists()) {
            if (FileUtils.isFileNewer(templateWar, versionFolder)) {
                logger.info("Older module package '{}' ({}) already deployed. Deleting it.", packageName, version);
                try {
                    FileUtils.deleteDirectory(versionFolder);
                } catch (IOException e) {
                    logger.error("Unable to delete the module directory " + versionFolder
                            + ". Skipping deployment.", e);
                }
            }
        }
        if (!versionFolder.exists()) {
            logger.info("Start unzipping module war package '{}' version {}", packageName, version);

            if (!deployPackageVersion(packageName, templateWar, versionFolder, deployedFiles)) {
                return null;
            }

            File metaInfFolder = new File(versionFolder, "META-INF");

            for (Map.Entry<String, String> deployedFile : deployedFiles.entrySet()) {
                deployedFiles.put(deployedFile.getKey(), deployedFile.getValue());
            }

            createDeploymentXMLFile(new File(metaInfFolder, "deployed.xml"), deployedFiles,
                    templateWar, packageName, depends, rootFolder, versionString, packageTimestamp);

            versionFolder.setLastModified(templateWar.lastModified());

            return versionFolder;
        }
        return null;
    }

    private boolean deployPackageVersion(String packageName, File templateWar, File versionFolder, Map<String, String> deployedFiles) throws IOException {
        versionFolder.mkdirs();

        JahiaArchiveFileHandler archiveFileHandler = null;
        try {
            archiveFileHandler = new JahiaArchiveFileHandler(templateWar.getPath());
            Map<String, String> unzippedFiles = archiveFileHandler.unzip(versionFolder.getAbsolutePath(), TEMPLATE_FILTER);
            deployedFiles.putAll(unzippedFiles);
        } catch (Exception e) {
            logger.error("Cannot unzip file: " + templateWar, e);
            return false;
        } finally {
            if (archiveFileHandler != null) {
                archiveFileHandler.closeArchiveFile();
            }
        }

        boolean newerClassesDetected = false;

        // check classes
        File classesFolder = new File(versionFolder, "WEB-INF/classes");
        if (classesFolder.exists()) {
            if (classesFolder.list().length > 0) {
                ChecksumFileFilter checksumFileFilter = new ChecksumFileFilter(classesFolder,
                        new File(settingsBean.getClassDiskPath()));
                Collection<File> changed = FileUtils.listFiles(classesFolder, checksumFileFilter,
                        null);
                if (changed.size() > 0) {
                    logger.warn("Detected {} newer classes for module {}."
                            + " Won't deploy module until server restart.", changed.size(),
                            packageName);
                    newerClassesDetected = true;
                }
            }
        }

        if (!newerClassesDetected) {
            // check JARs
            File libFolder = new File(versionFolder, "WEB-INF/lib");
            if (libFolder.exists()) {
                if (libFolder.list().length > 0) {
                    ChecksumFileFilter checksumFilter = new ChecksumFileFilter(libFolder, new File(
                            servletContext.getRealPath("/WEB-INF/lib")));
                    Collection<File> jars = FileUtils.listFiles(libFolder, new AndFileFilter(
                            checksumFilter, EmptyJarFilter.INSTANCE), null);
                    if (jars.size() > 0) {
                        logger.warn("Detected {} newer JARs for module {}."
                                + " Won't deploy module until server restart.", jars.size(),
                                packageName);
                        newerClassesDetected = true;
                    }
                }
            }
        }

        if (newerClassesDetected) {
            // module needs to be deployed using deployModule.sh or a server restart is needed
            FileUtils.deleteDirectory(versionFolder);
            return false;
        }

        FileUtils.deleteDirectory(new File(versionFolder, "WEB-INF/classes"));
        FileUtils.deleteDirectory(new File(versionFolder, "WEB-INF/lib"));

        // delete WEB-INF if it is empty
        File webInfFolder = new File(versionFolder, "WEB-INF");
        if (webInfFolder.exists() && webInfFolder.list().length == 0) {
            webInfFolder.delete();
        }

        // version successfully deployed and there are no changes in classes -> we can continue without restart
        return true;
    }

    private Document getDOM(Map<String, String> deployedFiles, File packageWar, String packageName, String depends, String rootFolder, String implementationVersionStr, Calendar packageTimestamp) {
        Element moduleElement = new Element("module");

        moduleElement.addContent(new Element("name").setText(packageName));
        if (depends != null) {
            moduleElement.addContent(new Element("depends").setText(depends));
        }
        moduleElement.addContent(new Element("rootFolder").setText(rootFolder));
        if (implementationVersionStr != null) {
            moduleElement.addContent(new Element("version").setText(implementationVersionStr));
        }
        String iso8601BuildTimestamp = ISO8601.format(packageTimestamp);
        moduleElement.addContent(new Element("build-timestamp").setText(iso8601BuildTimestamp));
        Calendar nowCalendar = Calendar.getInstance();
        String iso8601DeploymentTimestamp = ISO8601.format(nowCalendar);
        moduleElement.addContent(new Element("deployment-timestamp").setText(iso8601DeploymentTimestamp));
        Element packageElement = new Element("package");
        packageElement.setAttribute("name", packageWar.getName());
        packageElement.setAttribute("path", StringUtils.substringAfter(packageWar.getAbsolutePath(), servletContext.getRealPath("/")));
        moduleElement.addContent(packageElement);

        Element installedFiles = new Element("deployed");
        for (Map.Entry<String, String> deployedFile : deployedFiles.entrySet()) {
            Element deployedFileElement = new Element("file");
            deployedFileElement.setAttribute("source", deployedFile.getKey());
            deployedFileElement.setAttribute("destination", deployedFile.getValue());
            installedFiles.addContent(deployedFileElement);
        }

        moduleElement.addContent(installedFiles);

        return new Document(moduleElement);
    }

    private String getImplementationVersion(File manifest) {
        String version = null;

        if (manifest.exists()) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(manifest), 1024);
                Manifest mf = new Manifest(is);
                version = (String) mf.getMainAttributes().get(
                        new Attributes.Name("Implementation-Version"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return version;
    }


    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}