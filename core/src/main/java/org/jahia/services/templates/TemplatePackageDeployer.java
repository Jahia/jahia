/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.templates;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.zip.ExclusionWildcardFilter;
import org.jahia.utils.zip.JahiaArchiveFileHandler;
import org.jahia.utils.zip.PathFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

/**
 * Template package deployer service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageDeployer implements ServletContextAware, ApplicationEventPublisherAware {

    class TemplatesWatcher extends TimerTask {
        private Map<String, Long> timestamps = new HashMap<String, Long>();
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
                    timestamps.put(file.getPath(), file.lastModified());
                    logger.debug("Detected modified resource {}", file.getPath());
                    deployPackage(file);
                    reloadSpringContext = true;
                    changed = true;
                }
            }
            if (settingsBean.isDevelopmentMode()) {
                List<File> remaining = new LinkedList<File>();
    
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
                        for (File file : FileUtils.listFiles(metaInf, TrueFileFilter.INSTANCE,
                                TrueFileFilter.INSTANCE)) {
                            if (!timestamps.containsKey(file.getPath())
                                    || timestamps.get(file.getPath()) != file.lastModified()) {
                                timestamps.put(file.getPath(), file.lastModified());
                                logger.debug("Detected modified resource {}", file.getPath());
                                reloadSpringContext = true;
                            }
                        }
                    }

                    // watch for everything under module's WEB-INF/
                    File webInf = new File(deployedFolder, "WEB-INF");
                    if (webInf.exists()) {
                        for (File file : FileUtils.listFiles(webInf, TrueFileFilter.INSTANCE,
                                TrueFileFilter.INSTANCE)) {
                            if (!timestamps.containsKey(file.getPath())
                                    || timestamps.get(file.getPath()) != file.lastModified()) {
                                timestamps.put(file.getPath(), file.lastModified());
                                logger.debug("Detected modified resource {}", file.getPath());
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
                    List<JahiaTemplatesPackage> remainingPackages = new LinkedList<JahiaTemplatesPackage>();
                    for (File pkgFolder : remaining) {
                        JahiaTemplatesPackage packageHandler = getPackage(pkgFolder);
                        if (packageHandler != null) {
                            remainingPackages.add(packageHandler);
                        }
                    }
                    for (JahiaTemplatesPackage pack : getOrderedPackages(remainingPackages).values()) {
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
        logger.info("Scanning templates directory (" + templatesRoot + ") for deployed packages...");
        if (templatesRoot.isDirectory()) {
            File[] dirs = templatesRoot.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

            List<JahiaTemplatesPackage> remaining = new LinkedList<JahiaTemplatesPackage>();

            for (int i = 0; i < dirs.length; i++) {
                JahiaTemplatesPackage packageHandler = getPackage(dirs[i]);
                if (packageHandler != null) {
                    remaining.add(packageHandler);
                }
            }

           templatePackageRegistry.register(getOrderedPackages(remaining).values());
        }
        logger.info("...finished scanning templates directory. Found "
                + templatePackageRegistry.getAvailablePackagesCount() + " template packages.");
    }

    public JahiaTemplatesPackage getPackage(File templateDir) {
        logger.debug("Reading the templates set under " + templateDir);
        JahiaTemplatesPackage pkg = JahiaTemplatesPackageHandler.build(templateDir);
        if (pkg != null) {
            logger.debug("Template package found: " + pkg.getName());
            if (isValidPackage(pkg)) {
                return pkg;
            }
        } else {
            logger.warn("Unable to read template package from the directory " + templateDir);
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

        logger.info("Scanning shared templates directory (" + sharedTemplates
                + ") for new or updated template set packages ...");

        File[] warFiles = getPackageFiles(sharedTemplates);

        for (int i = 0; i < warFiles.length; i++) {
            File templateWar = warFiles[i];
            deployPackage(templateWar);
        }

        logger.info("...finished scanning shared templates directory.");
    }

    private void deployPackage(File templateWar) {
        String packageName = null;
        String rootFolder = null;
        try {
            JarFile jarFile = new JarFile(templateWar);
            packageName = (String) jarFile.getManifest().getMainAttributes().get(new Attributes.Name("package-name"));
            rootFolder = (String) jarFile.getManifest().getMainAttributes().get(new Attributes.Name("root-folder"));
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
                logger.debug("Older version of the template package '" + packageName + "' already deployed. Deleting it.");
                try {
                    FileUtils.deleteDirectory(tmplRootFolder);
                } catch (IOException e) {
                    logger.error("Unable to delete the template set directory " + tmplRootFolder
                            + ". Skipping deployment.", e);
                }
            }
        }
        if (!tmplRootFolder.exists()) {
            logger.info("Start deploying new template package '" + packageName + "'");

            tmplRootFolder.mkdirs();

            try {
                new JahiaArchiveFileHandler(templateWar.getPath()).unzip(tmplRootFolder.getAbsolutePath(), TEMPLATE_FILTER);
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
                        FileUtils.copyDirectory(classesFolder, new File(settingsBean.getClassDiskPath()));
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
                        FileUtils.copyDirectory(libFolder, new File(servletContext.getRealPath("/WEB-INF/lib")));
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

            JahiaTemplatesPackage pack = JahiaTemplatesPackageHandler.build(tmplRootFolder);
            if (!pack.getInitialImports().isEmpty()) {
                initialImports.add(pack);
            }

            logger.info("Package '" + packageName + "' successfully deployed");
        }
    }

    private void performInitialImport(JahiaTemplatesPackage pack) {
        logger.info("Starting import for the template package '" + pack.getName() + "' including: "
                + pack.getInitialImports());
        
        for (String imp : pack.getInitialImports()) {
            String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
            File importFile = new File(pack.getFilePath(), imp);
            logger.info("... importing " + importFile + " into " + targetPath);
            try {
                if (imp.toLowerCase().endsWith(".xml")) {
                    InputStream is = null;
                    try {
                        is = new FileInputStream(importFile);
                        importExportService.importXML(targetPath, is, true);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                } else {
                    importExportService.importZip(targetPath, importFile, true);
                }
            } catch (Exception e) {
                logger.error("Unable to import content for package '" + pack.getName() + "' from file " + imp
                        + ". Cause: " + e.getMessage(), e);
            }
        }
        
        logger.info("... finished initial import for template package '" + pack.getName() + "'.");
        
    }

    public void performInitialImport() {
        if (!initialImports.isEmpty()) {
            while (!initialImports.isEmpty()) {
                performInitialImport(initialImports.remove(0));
            }
        }
    }

    private Map<String, JahiaTemplatesPackage> getOrderedPackages(List<JahiaTemplatesPackage> remaining) {
        LinkedHashMap<String, JahiaTemplatesPackage> toDeploy = new LinkedHashMap<String, JahiaTemplatesPackage>();
        while (!remaining.isEmpty()) {
            List<JahiaTemplatesPackage> newRemaining = new LinkedList<JahiaTemplatesPackage>();
            for (JahiaTemplatesPackage pack : remaining) {
                if (pack.getDepends().isEmpty() || toDeploy.keySet().containsAll(pack.getDepends()) || templatePackageRegistry.containsAll(pack.getDepends())) {
                    toDeploy.put(pack.getName(), pack);
                } else {
                    newRemaining.add(pack);
                }
            }
            if (newRemaining.equals(remaining)) {
                String str = "";
                for (JahiaTemplatesPackage item : newRemaining) {
                    str += item.getName() + ",";
                }
                logger.error("Cannot deploy packages " + str + ", unresolved dependencies");
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
}