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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.*;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.zip.ExclusionWildcardFilter;
import org.jahia.utils.zip.JahiaArchiveFileHandler;
import org.jahia.utils.zip.PathFilter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.context.ServletContextAware;

/**
 * Template package deployer service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageDeployer implements ServletContextAware, ApplicationEventPublisherAware {

    private Map<String, Long> timestamps = new HashMap<String, Long>();

    class TemplatesWatcher extends TimerTask {
        private File sharedTemplatesFolder;
        private File deployedTemplatesFolder;

        TemplatesWatcher(File sharedTemplatesFolder, File deployedTemplatesFolder) {
            super();
            this.sharedTemplatesFolder = sharedTemplatesFolder;
            this.deployedTemplatesFolder = deployedTemplatesFolder;
            initTimestamps();
        }

        private void initTimestamps() {
            // list WEB-INF/var/shared_modules
            File[] existingFiles = getPackageFiles(sharedTemplatesFolder);
            for (File pkgFile : existingFiles) {
                logger.debug("Monitoring {} for changes", pkgFile.toString());
                timestamps.put(pkgFile.getPath(), pkgFile.lastModified());
            }
            
            if (settingsBean.isDevelopmentMode()) {
                // list first level folders under /modules
                for (File deployedFolder : deployedTemplatesFolder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter())) {
                    logger.debug("Monitoring {} for changes", deployedFolder.toString());
                    timestamps.put(deployedFolder.getPath(), deployedFolder.lastModified());
                    // list direct files (not recursing into sub-folders)
                    for (File file : deployedFolder.listFiles((FileFilter) FileFilterUtils.fileFileFilter())) {
                        logger.debug("Monitoring {} for changes", file.toString());
                        timestamps.put(file.getPath(), file.lastModified());
                    }
                    // watch for everything under module's META-INF/
                    File metaInf = new File(deployedFolder, "META-INF");
                    if (metaInf.exists()) {
                        for (File file : FileUtils.listFiles(metaInf, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                            logger.debug("Monitoring {} for changes", file.toString());
                            timestamps.put(file.getPath(), file.lastModified());
                        }
                    }
                    // watch for everything under module's WEB-INF/
                    File webInf = new File(deployedFolder, "WEB-INF");
                    if (webInf.exists()) {
                        for (File file : FileUtils.listFiles(webInf, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                            logger.debug("Monitoring {} for changes", file.toString());
                            timestamps.put(file.getPath(), file.lastModified());
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            boolean reloadSpringContext = false;
            boolean changed = false;

            // list WEB-INF/var/shared_modules
            File[] existingFiles = getPackageFiles(sharedTemplatesFolder);
            for (File file : existingFiles) {
                if (!timestamps.containsKey(file.getPath()) || timestamps.get(file.getPath()) != file.lastModified()) {
                    logger.debug("Detected modified resource {}", file.getPath());
                    try {
                    deployPackage(file);
                        timestamps.put(file.getPath(), file.lastModified());
                    reloadSpringContext = true;
                    changed = true;
                    } catch (ZipException e) {
                        logger.warn("Cannot deploy module : "+file.getName()+", will try again later");
                    } catch (Exception e) {
                        logger.error("Cannot deploy module : "+file.getName(),e);
                        timestamps.put(file.getPath(), file.lastModified());
                    }
                }
            }
            if (settingsBean.isDevelopmentMode()) {
                LinkedHashSet<File> remaining = new LinkedHashSet<File>();
    
                IOFileFilter fileFilter = new NotFileFilter(new SuffixFileFilter(new String[] {".pkg"}));

                // list first level folders under /modules
                for (File deployedFolder : deployedTemplatesFolder
                        .listFiles((FileFilter) FileFilterUtils.directoryFileFilter())) {
                    // list direct files (not recursing into sub-folders)
                    for (File file : deployedFolder.listFiles((FileFilter) FileFilterUtils
                            .fileFileFilter())) {
                        if (!timestamps.containsKey(file.getPath())
                                || timestamps.get(file.getPath()) != file.lastModified()) {
                            timestamps.put(file.getPath(), file.lastModified());
                            logger.debug("Detected modified resource {}", file.getPath());
                            remaining.add(deployedFolder);
                            if (file.getName().startsWith("import.")) {
                                reloadSpringContext = true;
                            }
                        }
                    }

                    // watch for everything under module's META-INF/
                    File metaInf = new File(deployedFolder, "META-INF");
                    if (metaInf.exists()) {
                        for (File file : FileUtils.listFiles(metaInf, fileFilter,
                                TrueFileFilter.INSTANCE)) {
                            if (!timestamps.containsKey(file.getPath())
                                    || timestamps.get(file.getPath()) != file.lastModified()) {
                                timestamps.put(file.getPath(), file.lastModified());
                                logger.debug("Detected modified resource {}", file.getPath());
                                remaining.add(deployedFolder);
                                reloadSpringContext = true;
                            }
                        }
                    }

                    // watch for everything under module's WEB-INF/
                    File webInf = new File(deployedFolder, "WEB-INF");
                    if (webInf.exists()) {
                        for (File file : FileUtils.listFiles(webInf, fileFilter,
                                TrueFileFilter.INSTANCE)) {
                            if (!timestamps.containsKey(file.getPath())
                                    || timestamps.get(file.getPath()) != file.lastModified()) {
                                timestamps.put(file.getPath(), file.lastModified());
                                logger.debug("Detected modified resource {}", file.getPath());
                                remaining.add(deployedFolder);
                                reloadSpringContext = true;
                            }
                        }
                    }

                    if (!timestamps.containsKey(deployedFolder.getPath())
                            || timestamps.get(deployedFolder.getPath()) != deployedFolder
                                    .lastModified()) {
                        timestamps.put(deployedFolder.getPath(), deployedFolder.lastModified());
                        logger.debug("Detected modified resource {}", deployedFolder.getPath());
                        remaining.add(deployedFolder);
                    }
                }

                if (!remaining.isEmpty()) {
                    LinkedHashSet<JahiaTemplatesPackage> remainingPackages = new LinkedHashSet<JahiaTemplatesPackage>();
                    for (File pkgFolder : remaining) {
                        JahiaTemplatesPackage packageHandler = getPackage(pkgFolder);
                        if (packageHandler != null) {
                            remainingPackages.add(packageHandler);
                        }
                    }
                    remainingPackages.addAll(unresolvedDependencies);
                    for (JahiaTemplatesPackage pack : getOrderedPackages(remainingPackages).values()) {
                        if (unzippedPackages.contains(pack.getRootFolder())) {
                            unzippedPackages.remove(pack.getRootFolder());
                            initialImports.add(pack);
                        }
                        unresolvedDependencies.remove(pack);
                        templatePackageRegistry.register(pack);
                        changed = true;
                    }
                }
            }
            
            if (changed) {
            	applicationEventPublisher.publishEvent(new TemplatePackageRedeployedEvent(TemplatePackageDeployer.class.getName()));
            }
            if (reloadSpringContext) {
                // reload the Spring application context for modules
                templatePackageRegistry.resetBeanModules();
                contextLoader.reload();
            }
        }

    }

    private static final PathFilter TEMPLATE_FILTER = new ExclusionWildcardFilter("WEB-INF/web.xml", "META-INF/maven/*");

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatePackageDeployer.class);
    
    private TemplatePackageRegistry templatePackageRegistry;
    
    private ImportExportService importExportService;

    private SettingsBean settingsBean;

    private Timer watchdog;

    private List<JahiaTemplatesPackage> initialImports = new LinkedList<JahiaTemplatesPackage>();
    private List<String> unzippedPackages = new LinkedList<String>();
    private Set<JahiaTemplatesPackage> unresolvedDependencies = new HashSet<JahiaTemplatesPackage>();

    private TemplatePackageApplicationContextLoader contextLoader;

    private ServletContext servletContext;

    private ApplicationEventPublisher applicationEventPublisher;

    private boolean isValidPackage(JahiaTemplatesPackage pkg) {
        if (StringUtils.isEmpty(pkg.getName())) {
            logger.warn("Template package name '" + pkg.getName() + "' is not valid. Setting it to 'templates'.");
            pkg.setName("templates");
        }
        if (StringUtils.isEmpty(pkg.getRootFolder())) {
            String folderName = pkg.getName().replace(' ', '_').toLowerCase();
            logger.warn("Template package root folder '" + pkg.getRootFolder() + "' is not valid. Setting it to '"
                    + folderName + "'.");
            pkg.setRootFolder(folderName);
        }
        return true;
    }

    public void registerTemplatePackages() {
        File templatesRoot = new File(settingsBean.getJahiaTemplatesDiskPath());
        logger.info("Scanning module directory (" + templatesRoot + ") for deployed packages...");
        if (templatesRoot.isDirectory()) {
            File[] dirs = templatesRoot.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

            LinkedHashSet<JahiaTemplatesPackage> remaining = new LinkedHashSet<JahiaTemplatesPackage>();

            for (int i = 0; i < dirs.length; i++) {
                JahiaTemplatesPackage packageHandler = getPackage(dirs[i]);
                if (packageHandler != null) {
                    remaining.add(packageHandler);
                }
            }

            for (JahiaTemplatesPackage pack : getOrderedPackages(remaining).values()) {
                if (unzippedPackages.contains(pack.getRootFolder())) {
                    unzippedPackages.remove(pack.getRootFolder());
                    initialImports.add(pack);
                }
                templatePackageRegistry.register(pack);
            }
        }
        logger.info("...finished scanning module directory. Found "
                + templatePackageRegistry.getAvailablePackagesCount() + " template packages.");
    }

    public JahiaTemplatesPackage getPackage(File templateDir) {
        logger.debug("Reading the module in " + templateDir);
        JahiaTemplatesPackage pkg = JahiaTemplatesPackageHandler.build(templateDir);
        if (pkg != null) {
            logger.debug("Module package found: " + pkg.getName());
            if (isValidPackage(pkg)) {
                return pkg;
            }
        } else {
            logger.warn("Unable to read module package from the directory " + templateDir);
        }
        return null;
    }

    /**
     * Goes through the template set archives in the in the shared templates
     * folder to check if there are any new or updated files, which needs to be
     * deployed to the templates folder. Does not register template set package
     * itself.
     */
    public void deploySharedTemplatePackages() {
        File sharedTemplates = new File(settingsBean.getJahiaSharedTemplatesDiskPath());

        logger.info("Scanning shared modules directory (" + sharedTemplates
                + ") for new or updated modules set packages ...");

        File[] warFiles = getPackageFiles(sharedTemplates);

        for (int i = 0; i < warFiles.length; i++) {
            File templateWar = warFiles[i];
            try {
            deployPackage(templateWar);
            } catch (Exception e) {
                logger.error("Cannot deploy module : "+templateWar.getName(),e);
        }
        }

        logger.info("...finished scanning shared modules directory.");
    }

    private class TracingFileFilter implements FileFilter {

        private Map<String,String> copiedFiles = new TreeMap<String,String>();
        private File referenceDir;
        private File sourceDir;
        private File destDir;

        public TracingFileFilter(File sourceDir, File destDir, File referenceDir) {
            this.sourceDir = sourceDir;
            this.destDir = destDir;
            this.referenceDir = referenceDir;
        }

        public boolean accept(File file) {
            String sourceDirAbsPath = sourceDir.getAbsolutePath() + File.separator;
            if (file.getAbsolutePath().startsWith(sourceDirAbsPath)) {
                String fileRelativePath = file.getAbsolutePath().substring(sourceDirAbsPath.length());
                String fileDestPath = destDir.getAbsolutePath() + File.separator + fileRelativePath;
                String referenceDirPath = referenceDir.getAbsolutePath() + File.separator;
                String referencePath = fileRelativePath;
                if (file.getAbsolutePath().startsWith(referenceDirPath)) {
                    referencePath = file.getAbsolutePath().substring(referenceDirPath.length());
                }
                copiedFiles.put(referencePath, fileDestPath);
            }
            return true;
        }

        public Map<String,String> getCopiedFiles() {
            return copiedFiles;
        }
    }

    private void deployPackage(File templateWar) throws IOException {
        String packageName = null;
        String rootFolder = null;
        String implementationVersionStr = null;
        String depends = null;
        Calendar packageTimestamp = Calendar.getInstance();
        // TODO there are still some bugs in the generated paths, we must fix the list, maybe exclude directories ?
        Map<String,String> deployedFiles = new TreeMap<String,String>();
        JarFile jarFile = new JarFile(templateWar);
        try {
            Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
            packageName = (String) mainAttributes.get(new Attributes.Name("package-name"));
            rootFolder = (String) mainAttributes.get(new Attributes.Name("root-folder"));
            depends = jarFile.getManifest().getMainAttributes().getValue("depends");
            long manifestTime = jarFile.getEntry("META-INF/MANIFEST.MF").getTime();
            packageTimestamp.setTimeInMillis(manifestTime);
            implementationVersionStr = (String) jarFile.getManifest().getMainAttributes().get(new Attributes.Name("Implementation-Version"));
            jarFile.close();
        } catch (IOException e) {
            logger.warn("Cannot read MANIFEST file from " + templateWar, e);
        }
        if (packageName == null) {
            packageName = StringUtils.substringBeforeLast(templateWar.getName(), ".");
        }
        if (rootFolder == null) {
            rootFolder = StringUtils.substringBeforeLast(templateWar.getName(), ".");
        }

        File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), rootFolder);
        if (tmplRootFolder.exists()) {
            if (FileUtils.isFileNewer(templateWar, tmplRootFolder)) {
                logger.debug("Older version of the module package '" + packageName + "' already deployed. Deleting it.");
                try {
                    FileUtils.deleteDirectory(tmplRootFolder);
                } catch (IOException e) {
                    logger.error("Unable to delete the module directory " + tmplRootFolder
                            + ". Skipping deployment.", e);
                }
            }
        }
        if (!tmplRootFolder.exists()) {
            logger.info("Start deploying new module package '" + packageName + "' version=" + implementationVersionStr );

            tmplRootFolder.mkdirs();

            try {
                JahiaArchiveFileHandler archiveFileHandler = new JahiaArchiveFileHandler(templateWar.getPath());
                Map<String,String> unzippedFiles = archiveFileHandler.unzip(tmplRootFolder.getAbsolutePath(), TEMPLATE_FILTER);
                deployedFiles.putAll(unzippedFiles);
            } catch (Exception e) {
                logger.error("Cannot unzip file: " + templateWar, e);
                return;
            }

            // deploy classes
            try {
                File classesFolder = new File(tmplRootFolder, "WEB-INF/classes");
                if (classesFolder.exists()) {
                    if (classesFolder.list().length > 0) {
                        logger.info("Deploying classes for module " + packageName);
                        TracingFileFilter tracingFileFilter = new TracingFileFilter(classesFolder, new File(settingsBean.getClassDiskPath()), tmplRootFolder);
                        FileUtils.copyDirectory(classesFolder, new File(settingsBean.getClassDiskPath()), tracingFileFilter);
                        deployedFiles.putAll(tracingFileFilter.getCopiedFiles());
                    }
                    FileUtils.deleteDirectory(new File(tmplRootFolder, "WEB-INF/classes"));
                }
            } catch (IOException e) {
                logger.error("Cannot deploy classes for module " + packageName, e);
            }

            // deploy JARs
            try {
                File libFolder = new File(tmplRootFolder, "WEB-INF/lib");
                if (libFolder.exists()) {
                    if (libFolder.list().length > 0) {
                        logger.info("Deploying JARs for module " + packageName);
                        TracingFileFilter tracingFileFilter = new TracingFileFilter(libFolder, new File(servletContext.getRealPath("/WEB-INF/lib")), tmplRootFolder);
                        FileUtils.copyDirectory(libFolder, new File(servletContext.getRealPath("/WEB-INF/lib")), tracingFileFilter);
                        deployedFiles.putAll(tracingFileFilter.getCopiedFiles());
                    }
                    FileUtils.deleteDirectory(new File(tmplRootFolder, "WEB-INF/lib"));
                }
            } catch (IOException e) {
                logger.error("Cannot deploy libs for module " + packageName, e);
            }

            // delete WEB-INF if it is empty
            File webInfFolder = new File(tmplRootFolder, "WEB-INF");
            if (webInfFolder.exists() && webInfFolder.list().length == 0) {
                webInfFolder.delete();
            }

            File metaInfFolder = new File(tmplRootFolder, "META-INF");
            if (!metaInfFolder.exists()) {
                metaInfFolder.mkdirs();
            }
            long lastModified = metaInfFolder.lastModified();
            createDeploymentXMLFile(new File(metaInfFolder, "deployed.xml"), deployedFiles,
                    templateWar, packageName, depends, rootFolder, implementationVersionStr, packageTimestamp);
            metaInfFolder.setLastModified(lastModified);

            unzippedPackages.add(tmplRootFolder.getName());
            tmplRootFolder.setLastModified(templateWar.lastModified());
        }
    }

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

    private Document getDOM(Map<String,String> deployedFiles, File packageWar, String packageName, String depends, String rootFolder, String implementationVersionStr, Calendar packageTimestamp) {
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
        packageElement.setAttribute("path", packageWar.getAbsolutePath());
        moduleElement.addContent(packageElement);

        Element installedFiles = new Element("deployed");
        for (Map.Entry<String,String> deployedFile : deployedFiles.entrySet()) {
            Element deployedFileElement = new Element("file");
            deployedFileElement.setAttribute("source", deployedFile.getKey());
            deployedFileElement.setAttribute("destination", deployedFile.getValue());
            installedFiles.addContent(deployedFileElement);
        }

        moduleElement.addContent(installedFiles);

        return new Document(moduleElement);
    }


    private boolean performInitialImport(final JahiaTemplatesPackage pack) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    initRepository(session, pack);
                    if (!pack.getInitialImports().isEmpty()) {
                        logger.info("Starting import for the module package '" + pack.getName() + "' including: "
                                + pack.getInitialImports());
                        cleanTemplates(pack.getRootFolder(), session);
                        for (String imp : pack.getInitialImports()) {
                            String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
                            File importFile = new File(pack.getFilePath(), imp);
                            logger.info("... importing " + importFile + " into " + targetPath);
                            try {
                                if (imp.toLowerCase().endsWith(".xml")) {
                                    InputStream is = null;
                                    try {
                                        is = new BufferedInputStream(new FileInputStream(importFile));
                                        session.importXML(targetPath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                                    } finally {
                                        IOUtils.closeQuietly(is);
                                    }
                                } else {
                                    importExportService.importZip(targetPath, importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE,session);
                                }
                                importFile.delete();
                                session.save(JCRObservationManager.IMPORT);
                            } catch (Exception e) {
                                logger.error("Unable to import content for package '" + pack.getName() + "' from file " + imp
                                        + ". Cause: " + e.getMessage(), e);
                            }
                        }
                        resetModuleAttributes(session, pack);
                        session.save();
                        logger.info("... finished initial import for module package '" + pack.getName() + "'.");
                        return true;
                    }
                    resetModuleAttributes(session, pack);
                    session.save();
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Unable to import content for package '" + pack.getName()
                    + "'. Cause: " + e.getMessage(), e);
            return false;
        }
    }

    private void resetModuleAttributes(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        JCRNodeWrapper modules = session.getNode("/templateSets");
        JCRNodeWrapper m = modules.getNode(pack.getRootFolder());
        m.setProperty("j:title", pack.getName());
        m.setProperty("j:installedModules", new Value[] { session.getValueFactory().createValue(pack.getName())});
        if (pack.getModuleType() != null) {
            m.setProperty("j:siteType",pack.getModuleType());
        }
        List<Value> l = new ArrayList<Value>();
        for (String d : pack.getDepends()) {
            if (templatePackageRegistry.lookup(d) != null) {
                l.add(session.getValueFactory().createValue(templatePackageRegistry.lookup(d).getFileName()));
            } else if (templatePackageRegistry.lookupByFileName(d) != null) {
                l.add(session.getValueFactory().createValue(templatePackageRegistry.lookupByFileName(d).getFileName()));
            } else {
                logger.warn("cannot found dependency " + d + " for package '" + pack.getName() + "'");
            }
        }
        Value[] values = new Value[pack.getDepends().size()];
        m.setProperty("j:dependencies",l.toArray(values));

        if (pack.getModuleType() == null) {
            String moduleType = guessModuleType(session, pack);
            pack.setModuleType(moduleType);
        }

        if (pack.getModuleType() != null) {
            m.setProperty("j:siteType",pack.getModuleType());
        }
    }

    private String guessModuleType(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        String moduleType = JahiaTemplateManagerService.MODULE_TYPE_MODULE;
        if (session.itemExists("/templateSets/" + pack.getRootFolder() + "/j:siteType")) {
            moduleType = session.getNode("/templateSets/"+pack.getRootFolder()).getProperty("j:siteType").getValue().getString();
        } else {
            List<String> files = new ArrayList<String>(Arrays.asList(new File(pack.getFilePath()).list()));
            files.removeAll(Arrays.asList("META-INF","WEB-INF", "resources"));
            if (files.isEmpty()) {
                moduleType = JahiaTemplateManagerService.MODULE_TYPE_SYSTEM;
            }
        }
        return moduleType;
    }

    private void initRepository(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        initModuleNode(session, pack, true);
        session.save();
    }

    private boolean initModuleNode(JCRSessionWrapper session, JahiaTemplatesPackage pack, boolean updateDeploymentDate)
            throws RepositoryException {
        boolean modified = false;
        if (!session.nodeExists("/templateSets")) {
            session.getRootNode().addNode("templateSets", "jnt:templateSets");
            modified = true;
        }
        JCRNodeWrapper modules = session.getNode("/templateSets");
        JCRNodeWrapper m;
        if (!modules.hasNode(pack.getRootFolder())) {
            modified = true;
            m = modules.addNode(pack.getRootFolder(), "jnt:virtualsite");
            m.addNode("portlets", "jnt:portletFolder");
            m.addNode("files", "jnt:folder");
            m.addNode("contents", "jnt:contentFolder");
            JCRNodeWrapper tpls = m.addNode("templates", "jnt:templatesFolder");
            tpls.addNode("files", "jnt:folder");
            tpls.addNode("contents", "jnt:contentFolder");
        } else {
            m = modules.getNode(pack.getRootFolder());
        }
        JCRNodeWrapper v;
        if (!m.hasNode("j:versionInfo")) {
            v = m.addNode("j:versionInfo", "jnt:versionInfo");
            modified = true;
        } else {
            v = m.getNode("j:versionInfo");
        }
        if (v.hasProperty("j:version")) {
            String s = v.getProperty("j:version").getString();
            if (!s.equals(pack.getVersion().toString())) {
                v.setProperty("j:version", pack.getVersion().toString());
                modified = true;
            }
        } else {
            v.setProperty("j:version", pack.getVersion().toString());
            modified = true;
        }
        if (updateDeploymentDate) {
            v.setProperty("j:deployementDate", new GregorianCalendar());
            modified = true;
        }

        return modified;
    }

    public List<JahiaTemplatesPackage> performInitialImport() {
        List<JahiaTemplatesPackage> results = new ArrayList<JahiaTemplatesPackage>();
        if (!initialImports.isEmpty()) {
            while (!initialImports.isEmpty()) {
                JahiaTemplatesPackage pack = initialImports.remove(0);
                performInitialImport(pack);
                results.add(pack);
            }
        }
        return results;
    }

    private void cleanTemplates(String moduleName, JCRSessionWrapper session) throws RepositoryException {
        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = queryManager.createQuery(
                "select * from [jnt:virtualsite] as n where isdescendantnode(n,['/templateSets']) and name(n) = '" + moduleName + "'", Query.JCR_SQL2).execute();
        final NodeIterator iterator = result.getNodes();
        while (iterator.hasNext()) {
            Node n = iterator.nextNode();
            removeTemplates(n.getNode("templates"));
        }
    }
    private void removeTemplates(Node n)  throws RepositoryException {
        NodeIterator c = n.getNodes();
        while(c.hasNext()) {
            Node nn = (c.nextNode());
            if (!nn.isNodeType("jnt:template")) {
                nn.remove();
            } else {
                if (nn.hasNodes()) {
                    removeTemplates(nn);
                }
            }
        }
    }

    private Map<String, JahiaTemplatesPackage> getOrderedPackages(LinkedHashSet<JahiaTemplatesPackage> remaining) {
        LinkedHashMap<String, JahiaTemplatesPackage> toDeploy = new LinkedHashMap<String, JahiaTemplatesPackage>();
        Set<String> folderNames = new HashSet<String>();
        while (!remaining.isEmpty()) {
            LinkedHashSet<JahiaTemplatesPackage> newRemaining = new LinkedHashSet<JahiaTemplatesPackage>();
            for (JahiaTemplatesPackage pack : remaining) {
                Set<String> allDeployed = new HashSet<String>(templatePackageRegistry.getPackageNames());
                allDeployed.addAll(templatePackageRegistry.getPackageFileNames());
                allDeployed.addAll(toDeploy.keySet());
                allDeployed.addAll(folderNames);

                if (pack.getDepends().isEmpty() || allDeployed.containsAll(pack.getDepends())) {
                    toDeploy.put(pack.getName(), pack);
                    folderNames.add(pack.getRootFolder());
                } else {
                    newRemaining.add(pack);
                }
            }
            if (newRemaining.equals(remaining)) {
                String str = "";
                for (JahiaTemplatesPackage item : newRemaining) {
                    unresolvedDependencies.add(item);
                    str += item.getName() + ",";
                }
                logger.error("Cannot deploy packages " + str + " unresolved dependencies");
                break;
            } else {
                remaining = newRemaining;
            }
        }
        return toDeploy;
    }

    private File[] getPackageFiles(File sharedTemplatesFolder) {
        File[] packageFiles;

        if (!sharedTemplatesFolder.exists()) {
            sharedTemplatesFolder.mkdirs();
        }

        if (sharedTemplatesFolder.exists() && sharedTemplatesFolder.isDirectory()) {
            packageFiles = sharedTemplatesFolder.listFiles((FilenameFilter) new SuffixFileFilter(new String[]{".jar", ".war"}));
        } else {
            packageFiles = new File[0];
        }
        return packageFiles;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

    public void startWatchdog() {
        long interval = settingsBean.isDevelopmentMode() ? 5000 : SettingsBean.getInstance().getTemplatesObserverInterval();
        if (interval <= 0) {
            return;
        }

        logger.info("Starting template packages watchdog with interval " + interval + " ms. Monitoring the folder "
                + settingsBean.getJahiaSharedTemplatesDiskPath());

        stopWatchdog();
        watchdog = new Timer(true);
        watchdog.schedule(new TemplatesWatcher(new File(settingsBean.getJahiaSharedTemplatesDiskPath()), new File(settingsBean.getJahiaTemplatesDiskPath())),
                interval, interval);
    }

    public void stopWatchdog() {
        if (watchdog != null) {
            watchdog.cancel();
        }
    }

    public void setTimestamp(String path, long time) {
        timestamps.put(path, time);
    }


    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    public void setContextLoader(TemplatePackageApplicationContextLoader contextLoader) {
        this.contextLoader = contextLoader;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void initializeMissingModuleNodes() {
        long timer = System.currentTimeMillis();
        int count = 0;

        try {
            final Set<String> willBeImported = new HashSet<String>();
            for (JahiaTemplatesPackage pkg : initialImports) {
                willBeImported.add(pkg.getRootFolder());
            }
            count = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Integer>() {
                        public Integer doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            int count = 0;
                            for (JahiaTemplatesPackage pkg : templatePackageRegistry
                                    .getAvailablePackages()) {
                                if (!willBeImported.contains(pkg.getRootFolder())
                                        && !session.nodeExists("/templateSets/"
                                                + pkg.getRootFolder())) {
                                    if (initModuleNode(session, pkg, false)) {
                                        count++;
                                        resetModuleAttributes(session, pkg);
                                    }
                                }
                            }
                            if (count > 0) {
                                session.save();
                            }
                            return count;
                        }
                    });
        } catch (RepositoryException e) {
            logger.error("Error initializig modules. Cause: " + e.getMessage(), e);
        }
        logger.info("Checking for missing module nodes and initializing {} of them took {} ms",
                count, (System.currentTimeMillis() - timer));
    }
}