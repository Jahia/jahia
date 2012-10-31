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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.*;
import java.util.*;
import java.util.zip.ZipException;

/**
 * Template package deployer service.
 *
 * @author Sergiy Shyrkov
 * @author Thomas Draier
 */
class TemplatePackageDeployer implements ApplicationEventPublisherAware {
// ------------------------------ FIELDS ------------------------------

    private static Logger logger = LoggerFactory.getLogger(TemplatePackageDeployer.class);

    private DeploymentHelper deploymentHelper;
    private Map<String, Long> timestamps = new HashMap<String, Long>();
    private TemplatesWatcher templatesWatcher;

    private JahiaTemplateManagerService service;

    private TemplatePackageRegistry templatePackageRegistry;
    private ComponentRegistry componentRegistry;

    private ImportExportService importExportService;

    private SettingsBean settingsBean;

    private Timer watchdog;

    private List<JahiaTemplatesPackage> modulesToInitialize = new LinkedList<JahiaTemplatesPackage>();
    private List<String> unzippedPackages = new LinkedList<String>();
    private Set<JahiaTemplatesPackage> unresolvedDependencies = new HashSet<JahiaTemplatesPackage>();

    private TemplatePackageApplicationContextLoader contextLoader;

    private ApplicationEventPublisher applicationEventPublisher;

// --------------------- GETTER / SETTER METHODS ---------------------

    public TemplatesWatcher getTemplatesWatcher() {
        return templatesWatcher;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setComponentRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    public void setContextLoader(TemplatePackageApplicationContextLoader contextLoader) {
        this.contextLoader = contextLoader;
    }

    public void setDeploymentHelper(DeploymentHelper deploymentHelper) {
        this.deploymentHelper = deploymentHelper;
    }

    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    public void setService(JahiaTemplateManagerService service) {
        this.service = service;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

// -------------------------- OTHER METHODS --------------------------

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
                File folder = deploymentHelper.deployPackage(templateWar);
                if (folder != null) {
                    unzippedPackages.add(folder.getPath());
                }
            } catch (Exception e) {
                logger.error("Cannot deploy module : " + templateWar.getName(), e);
            }
        }

        logger.info("...finished scanning shared modules directory.");
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

    private Map<String, JahiaTemplatesPackage> getOrderedPackages(LinkedHashSet<JahiaTemplatesPackage> remaining) {
        LinkedHashMap<String, JahiaTemplatesPackage> toDeploy = new LinkedHashMap<String, JahiaTemplatesPackage>();
        Set<String> packageNames = new HashSet<String>();
        Set<String> folderNames = new HashSet<String>();

        LinkedHashSet<JahiaTemplatesPackage> newRemaining = new LinkedHashSet<JahiaTemplatesPackage>();
        for (JahiaTemplatesPackage pack : remaining) {
            if (pack.getRootFolder().equals("assets")) {
                toDeploy.put(pack.getFilePath(), pack);
                packageNames.add(pack.getName());
                folderNames.add(pack.getRootFolder());
            } else {
                newRemaining.add(pack);
            }
        }
        remaining = newRemaining;
        newRemaining = new LinkedHashSet<JahiaTemplatesPackage>();
        for (JahiaTemplatesPackage pack : remaining) {
            if (pack.getRootFolder().equals("default")) {
                toDeploy.put(pack.getFilePath(), pack);
                packageNames.add(pack.getName());
                folderNames.add(pack.getRootFolder());
            } else {
                newRemaining.add(pack);
            }
        }
        boolean systemTemplatesDeployed = templatePackageRegistry.containsFileName("templates-system");
        remaining = newRemaining;
        while (!remaining.isEmpty()) {
            newRemaining = new LinkedHashSet<JahiaTemplatesPackage>();
            for (JahiaTemplatesPackage pack : remaining) {
                Set<String> allDeployed = new HashSet<String>(templatePackageRegistry.getPackageNames());
                allDeployed.addAll(templatePackageRegistry.getPackageFileNames());
                allDeployed.addAll(packageNames);
                allDeployed.addAll(folderNames);

                boolean requireSystemTemplates = false;
                if (!systemTemplatesDeployed && !pack.getRootFolder().equals("templates-system")) {
                    for (String s : pack.getInitialImports()) {
                        if (s.startsWith("META-INF/importsite") && !systemTemplatesDeployed) {
                            requireSystemTemplates = true;
                            break;
                        }
                    }
                }

                if ((pack.getDepends().isEmpty() || allDeployed.containsAll(pack.getDepends())) && !requireSystemTemplates) {
                    toDeploy.put(pack.getFilePath(), pack);
                    packageNames.add(pack.getName());
                    folderNames.add(pack.getRootFolder());
                    if (pack.getRootFolder().equals("templates-system")) {
                        systemTemplatesDeployed = true;
                    }
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

    public void initializeModulesContent() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    // initialize modules (migration case)
                    while (!modulesToInitialize.isEmpty()) {
                        initializeModuleContent(modulesToInitialize.remove(0), session);
                    }

                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void initializeModuleContent(JahiaTemplatesPackage aPackage, JCRSessionWrapper session) throws RepositoryException {
        try {
            resetModuleNodes(aPackage, session);

            logger.info("Starting import for the module package '" + aPackage.getName() + "' including: "
                    + aPackage.getInitialImports());
            for (String imp : aPackage.getInitialImports()) {
                String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
                File importFile = new File(aPackage.getFilePath(), imp);
                logger.info("... importing " + importFile + " into " + targetPath);
                try {
                    session.getPathMapping().put("/templateSets/", "/modules/");
                    session.getPathMapping().put("/modules/" + aPackage.getRootFolder() + "/", "/modules/" + aPackage.getRootFolder() + "/" + aPackage.getVersion() + "/");

                    if (imp.toLowerCase().endsWith(".xml")) {
                        InputStream is = null;
                        try {
                            is = new BufferedInputStream(new FileInputStream(importFile));
                            session.importXML(targetPath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    } else if (imp.toLowerCase().contains("/importsite")) {
                        JCRUser user = null;
                        try {
                            user = JCRUserManagerProvider.getInstance().lookupRootUser();
                            JCRSessionFactory.getInstance().setCurrentUser(user);
                            importExportService.importSiteZip(importFile, session);
                        } finally {
                            JCRSessionFactory.getInstance().setCurrentUser(user);
                        }
                    } else {
                        importExportService.importZip(targetPath, importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session);
                    }

                    importFile.delete();
                    session.save(JCRObservationManager.IMPORT);
                } catch (Exception e) {
                    logger.error("Unable to import content for package '" + aPackage.getName() + "' from file " + imp
                            + ". Cause: " + e.getMessage(), e);
                }
            }
            cloneModuleInLive(aPackage);
            logger.info("... finished initial import for module package '" + aPackage.getName() + "'.");

            componentRegistry.registerComponents(aPackage, session);

            if (aPackage.isActiveVersion()) {
                if (templatePackageRegistry.lookupByFileName(aPackage.getRootFolder()).equals(aPackage)) {
                    autoDeployModulesToSites(session, aPackage);
                }
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error("Unable to import content for package '" + aPackage.getName()
                    + "'. Cause: " + e.getMessage(), e);
        }
    }

    private void autoDeployModulesToSites(JCRSessionWrapper session, JahiaTemplatesPackage pack)
            throws RepositoryException {
        if(pack.getAutoDeployOnSite()!=null) {
            if("system".equals(pack.getAutoDeployOnSite())) {
                if(session.nodeExists("/sites/systemsite")) {
                    service.deployModule("/modules/" + pack.getRootFolder(), "/sites/systemsite", session);
                }
            } else if ("all".equals(pack.getAutoDeployOnSite())) {
                if(session.nodeExists("/sites/systemsite")) {
                    service.deployModuleToAllSites("/modules/" + pack.getRootFolder(), session, null);
                }
            }
        }
    }

    private synchronized void cloneModuleInLive(final JahiaTemplatesPackage pack) throws RepositoryException {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (!session.itemExists("/modules")) {
                        session.getWorkspace().clone("default", "/modules", "/modules", true);
                    } else if (!session.itemExists("/modules/" + pack.getRootFolder())) {
                        session.getWorkspace().clone("default", "/modules/" + pack.getRootFolder(), "/modules/" + pack.getRootFolder(), true);
                    } else {
                        if (session.itemExists("/modules/" + pack.getRootFolderWithVersion())) {
                            session.getNode("/modules/" + pack.getRootFolderWithVersion()).remove();
                            session.save();
                        }
                        session.getWorkspace().clone("default", "/modules/" + pack.getRootFolderWithVersion(), "/modules/" + pack.getRootFolderWithVersion(), true);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot clone " + pack.getRootFolderWithVersion(), e);
        }
    }

    public void resetModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) {
        try {
            if (session.nodeExists("/modules/" + pkg.getRootFolder() + "/" + pkg.getVersion())) {
                session.getNode("/modules/" + pkg.getRootFolder() + "/" + pkg.getVersion()).remove();
                session.save();
            }
            if (initModuleNode(session, pkg, false)) {
                resetModuleAttributes(session, pkg);
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error("Error initializig modules. Cause: " + e.getMessage(), e);
        }
    }

    private boolean initModuleNode(JCRSessionWrapper session, JahiaTemplatesPackage pack, boolean updateDeploymentDate)
            throws RepositoryException {
        boolean modified = false;
        if (!session.nodeExists("/modules")) {
            session.getRootNode().addNode("modules", "jnt:modules");
            modified = true;
        }
        JCRNodeWrapper modules = session.getNode("/modules");
        JCRNodeWrapper m;
        if (!modules.hasNode(pack.getRootFolder())) {
            modified = true;
            m = modules.addNode(pack.getRootFolder(), "jnt:module");
        } else {
            m = modules.getNode(pack.getRootFolder());
        }

        if (!m.hasNode(pack.getVersion().toString())) {
            modified = true;
            m = m.addNode(pack.getVersion().toString(), "jnt:moduleVersion");
            m.addNode("portlets", "jnt:portletFolder");
            m.addNode("files", "jnt:folder");
            m.addNode("contents", "jnt:contentFolder");
            JCRNodeWrapper tpls = m.addNode("templates", "jnt:templatesFolder");
            if (JahiaTemplateManagerService.MODULE_TYPE_MODULE.equals(pack.getModuleType())) {
                tpls.setProperty("j:rootTemplatePath", "/base");
            }
            tpls.addNode("files", "jnt:folder");
            tpls.addNode("contents", "jnt:contentFolder");
        } else {
            m = m.getNode(pack.getVersion().toString());
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
            v.setProperty("j:version", pack.getVersion().toString());
            modified = true;
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

    private void resetModuleAttributes(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        JCRNodeWrapper modules = session.getNode("/modules");
        JCRNodeWrapper m = modules.getNode(pack.getRootFolderWithVersion());

        m.setProperty("j:title", pack.getName());
//        m.setProperty("j:installedModules", new Value[] { session.getValueFactory().createValue(pack.getRootFolder())});
        if (pack.getModuleType() != null) {
            m.setProperty("j:moduleType", pack.getModuleType());
        }
        List<Value> l = new ArrayList<Value>();
        for (String d : pack.getDepends()) {
            if (templatePackageRegistry.lookup(d) != null) {
                l.add(session.getValueFactory().createValue(templatePackageRegistry.lookup(d).getRootFolder()));
            } else if (templatePackageRegistry.lookupByFileName(d) != null) {
                l.add(session.getValueFactory().createValue(templatePackageRegistry.lookupByFileName(d).getRootFolder()));
            } else {
                logger.warn("cannot found dependency " + d + " for package '" + pack.getName() + "'");
            }
        }
        Value[] values = new Value[pack.getDepends().size()];
        m.setProperty("j:dependencies", l.toArray(values));

        if (pack.getModuleType() == null) {
            String moduleType = guessModuleType(session, pack);
            pack.setModuleType(moduleType);
        }

        JCRNodeWrapper tpls = m.getNode("templates");
        if (!tpls.hasProperty("j:rootTemplatePath") && JahiaTemplateManagerService.MODULE_TYPE_MODULE.equals(pack.getModuleType())) {
            tpls.setProperty("j:rootTemplatePath", "/base");
        }
    }

    private String guessModuleType(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        String moduleType = JahiaTemplateManagerService.MODULE_TYPE_MODULE;
        if (session.itemExists("/modules/" + pack.getRootFolderWithVersion() + "/j:moduleType")) {
            moduleType = session.getNode("/modules/" + pack.getRootFolderWithVersion()).getProperty("j:moduleType").getValue().getString();
        } else {
            List<String> files = new ArrayList<String>(Arrays.asList(new File(pack.getFilePath()).list()));
            files.removeAll(Arrays.asList("META-INF", "WEB-INF", "resources"));
            if (files.isEmpty()) {
                moduleType = JahiaTemplateManagerService.MODULE_TYPE_SYSTEM;
            }
        }
        return moduleType;
    }

    public void registerTemplatePackages() {
        File templatesRoot = new File(settingsBean.getJahiaTemplatesDiskPath());
        logger.info("Scanning module directory (" + templatesRoot + ") for deployed packages...");
        if (templatesRoot.isDirectory()) {
            File[] dirs = templatesRoot.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

            LinkedHashSet<JahiaTemplatesPackage> remaining = new LinkedHashSet<JahiaTemplatesPackage>();

            for (File moduleDir : dirs) {
                for (File versionDir : moduleDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
                    JahiaTemplatesPackage packageHandler = getPackage(versionDir);
                    if (packageHandler != null) {
                        remaining.add(packageHandler);
                    }
                }
            }

            for (JahiaTemplatesPackage pack : getOrderedPackages(remaining).values()) {
                if (unzippedPackages.contains(pack.getFilePath())) {
                    unzippedPackages.remove(pack.getFilePath());
                    modulesToInitialize.add(pack);
                }

                templatePackageRegistry.registerPackage(pack);
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

    public void setTimestamp(String path, long time) {
        timestamps.put(path, time);
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
        templatesWatcher = new TemplatesWatcher(new File(settingsBean.getJahiaSharedTemplatesDiskPath()), new File(settingsBean.getJahiaTemplatesDiskPath()));
        watchdog.schedule(templatesWatcher,
                interval, interval);
    }

    public void stopWatchdog() {
        if (watchdog != null) {
            watchdog.cancel();
        }
    }

// -------------------------- INNER CLASSES --------------------------

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
                Collection<File> files = FileUtils.listFiles(deployedTemplatesFolder, null, true);
                for (File file : files) {
                    logger.debug("Monitoring {} for changes", file.toString());
                    timestamps.put(file.getPath(), file.lastModified());
                }
            }
        }

        @Override
        public synchronized void run() {
            Set<File> changed = new HashSet<File>();

            LinkedHashSet<File> remaining = new LinkedHashSet<File>();

            List<File> foldersToCheck = new ArrayList<File>();

            // list WEB-INF/var/shared_modules
            File[] existingFiles = getPackageFiles(sharedTemplatesFolder);
            for (File file : existingFiles) {
                if (!timestamps.containsKey(file.getPath()) || timestamps.get(file.getPath()) != file.lastModified()) {
                    logger.debug("Detected modified resource {}", file.getPath());
                    try {
                        File folder = deploymentHelper.deployPackage(file);
                        if (folder != null) {
                            unzippedPackages.add(folder.getName());
                            remaining.add(folder);
                        }
                        timestamps.put(file.getPath(), file.lastModified());
                    } catch (ZipException e) {
                        logger.warn("Cannot deploy module : " + file.getName() + ", will try again later");
                    } catch (Exception e) {
                        logger.error("Cannot deploy module : " + file.getName(), e);
                        timestamps.put(file.getPath(), file.lastModified());
                    }
                }
            }

            if (settingsBean.isDevelopmentMode()) {
                // list first level folders under /modules
                Collection<File> files = FileUtils.listFiles(deployedTemplatesFolder, null, true);
                for (File file : files) {
                    if (!timestamps.containsKey(file.getPath()) || timestamps.get(file.getPath()) != file.lastModified()) {
                        timestamps.put(file.getPath(), file.lastModified());
                        while (file.getPath().split("/").length > deployedTemplatesFolder.getPath().split("/").length + 2) {
                            file = file.getParentFile();
                        }
                        remaining.add(file);
                    }
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
                final Map<String, JahiaTemplatesPackage> orderedPackages = getOrderedPackages(remainingPackages);
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            // initialize modules (migration case)

                            for (final JahiaTemplatesPackage pack : orderedPackages.values()) {
                                if (unzippedPackages.contains(pack.getFilePath())) {
                                    unzippedPackages.remove(pack.getFilePath());
                                }
                                unresolvedDependencies.remove(pack);
                                templatePackageRegistry.registerPackage(pack);

                                if (pack.getContext() != null && pack.isActiveVersion()) {
                                    contextLoader.reload(pack);

                                    initializeModuleContent(pack, session);

                                    templatePackageRegistry.afterInitializationForModule(pack);
                                } else {
                                    initializeModuleContent(pack, session);
                                }
                            }
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    logger.error("Error when initializing modules",e);
                }
            }
        }
    }
}