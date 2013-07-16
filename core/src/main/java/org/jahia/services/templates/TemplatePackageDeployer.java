/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.ProvisionActivator;
import org.jahia.services.content.*;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Template package deployer service.
 *
 * @author Sergiy Shyrkov
 * @author Thomas Draier
 */
public class TemplatePackageDeployer {

    private static Logger logger = LoggerFactory.getLogger(TemplatePackageDeployer.class);

    protected JahiaTemplateManagerService service;

<<<<<<< .working
=======
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

            LinkedHashSet<File> remaining = new LinkedHashSet<File>();

            // list WEB-INF/var/shared_modules
            File[] existingFiles = getPackageFiles(sharedTemplatesFolder);
            for (File file : existingFiles) {
                if (!timestamps.containsKey(file.getPath()) || timestamps.get(file.getPath()) != file.lastModified()) {
                    logger.debug("Detected modified resource {}", file.getPath());
                    try {
                        File folder = deployPackage(file);
                        if (folder != null) {
                            remaining.add(folder);
                        }
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

            if (changed) {
            	applicationEventPublisher.publishEvent(new TemplatePackageRedeployedEvent(TemplatePackageDeployer.class.getName()));
            }
            if (reloadSpringContext) {
                // reload the Spring application context for modules
                templatePackageRegistry.resetBeanModules();
                contextLoader.reload();
                if (SpringContextSingleton.getInstance().getModuleContext() != null) {
                    Map map = SpringContextSingleton.getInstance().getModuleContext().getBeansOfType(
                            JahiaAfterInitializationService.class);
                    for (Object o : map.values()) {
                        JahiaAfterInitializationService initializationService = (JahiaAfterInitializationService) o;
                        try {
                            initializationService.initAfterAllServicesAreStarted();
                        } catch (JahiaInitializationException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

    }

    private static final PathFilter TEMPLATE_FILTER = new ExclusionWildcardFilter("WEB-INF/web.xml", "META-INF/maven/*");

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatePackageDeployer.class);

>>>>>>> .merge-right.r46717
    private TemplatePackageRegistry templatePackageRegistry;
<<<<<<< .working
    private ComponentRegistry componentRegistry;
=======

    private ImportExportService importExportService;
>>>>>>> .merge-right.r46717

    private ImportExportBaseService importExportService;


    private List<JahiaTemplatesPackage> modulesToInitialize = new LinkedList<JahiaTemplatesPackage>();

    public void setComponentRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    public void setImportExportService(ImportExportBaseService importExportService) {
        this.importExportService = importExportService;
    }

    public void setService(JahiaTemplateManagerService service) {
        this.service = service;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

    public void initializeModulesContent() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    // initialize modules (migration case)
                    while (!modulesToInitialize.isEmpty()) {
                        JahiaTemplatesPackage aPackage = modulesToInitialize.remove(0);
                        try {
                            initializeModuleContent(aPackage, session);
                        } catch (RepositoryException e) {
                            logger.error("Cannot initialize module " + aPackage.getName(), e);
                        }
                    }

<<<<<<< .working
                    return null;
=======
        private Map<String,String> copiedFiles = new TreeMap<String,String>();
        private File referenceDir;
        private File sourceDir;
        private File destDir;
        private String basePath;

        public TracingFileFilter(File sourceDir, File destDir, File referenceDir) {
            this.sourceDir = sourceDir;
            this.destDir = destDir;
            this.referenceDir = referenceDir;
        }

        public TracingFileFilter(File sourceDir, File destDir, File referenceDir, String basePath) {
            this(sourceDir, destDir, referenceDir);
            this.basePath = basePath;
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
>>>>>>> .merge-right.r46717
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void initializeModuleContent(JahiaTemplatesPackage aPackage, JCRSessionWrapper session) throws RepositoryException {
        resetModuleNodes(aPackage, session);

        logger.info("Starting import for the module package '" + aPackage.getName() + "' including: "
                + aPackage.getInitialImports());
        for (String imp : aPackage.getInitialImports()) {
            String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
            Resource importFile = aPackage.getResource(imp);
            logger.info("... importing " + importFile + " into " + targetPath);
            session.getPathMapping().put("/templateSets/", "/modules/");
            session.getPathMapping().put("/modules/" + aPackage.getRootFolder() + "/", "/modules/" + aPackage.getRootFolder() + "/" + aPackage.getVersion() + "/");

            try {
                if (imp.toLowerCase().endsWith(".xml")) {
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(importFile.getInputStream());
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
            } catch (IOException e) {
                throw new RepositoryException(e);
            }

            File realImportFile = null;
            try {
                realImportFile = importFile.getFile();
            } catch (IOException ioe) {
                realImportFile = null;
            }
            if (realImportFile != null) {
                realImportFile.delete();
            }
            session.save(JCRObservationManager.IMPORT);
        }
        cloneModuleInLive(aPackage);
        logger.info("... finished initial import for module package '" + aPackage.getName() + "'.");

        componentRegistry.registerComponents(aPackage, session);

        if (aPackage.isActiveVersion()) {
            if (templatePackageRegistry.lookupByFileName(aPackage.getRootFolder()).equals(aPackage)) {
                service.autoInstallModulesToSites(aPackage, session);
            }
        }
        session.save();
    }

    private synchronized void cloneModuleInLive(final JahiaTemplatesPackage pack) throws RepositoryException {
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
    }

    private void resetModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) throws RepositoryException {
        clearModuleNodes(pkg, session);
        if (initModuleNode(session, pkg, true)) {
            resetModuleAttributes(session, pkg);
        }
        session.save();
    }

<<<<<<< .working
    public void clearModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) throws RepositoryException {
        String modulePath = "/modules/" + pkg.getRootFolder() + "/" + pkg.getVersion();
        if (session.nodeExists(modulePath)) {
            JCRNodeWrapper moduleNode = session.getNode(modulePath);
            NodeIterator nodeIterator = moduleNode.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                if (!next.isNodeType("jnt:versionInfo") && !next.isNodeType("jnt:moduleVersionFolder")) {
                    next.remove();
                }
=======
    private void resetModuleAttributes(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        JCRNodeWrapper modules = session.getNode("/templateSets");
        JCRNodeWrapper m = modules.getNode(pack.getRootFolder());
        m.setProperty("j:title", pack.getName());
        m.setProperty("j:installedModules", new Value[] { session.getValueFactory().createValue(pack.getFileName())});
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
                logger.warn("cannot find dependency " + d + " for package '" + pack.getName() + "'");
>>>>>>> .merge-right.r46717
            }
            session.save();
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
        if (!v.hasProperty("j:version")) {
            v.setProperty("j:version", pack.getVersion().toString());
            modified = true;
        }
        if (pack.getSourcesFolder() != null) {
            v.setProperty("j:sourcesFolder",pack.getSourcesFolder().getPath());
        }
        if (pack.getScmURI() != null && !"scm:dummy:uri".equals(pack.getScmURI())) {
            try {
                v.setProperty("j:scmURI",pack.getScmURI());
            } catch (Exception e) {
                logger.error("Cannot get SCM url");
            }
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
        if (pack.getModuleType() != null) {
            m.setProperty("j:moduleType", pack.getModuleType());
        }
        List<Value> l = new ArrayList<Value>();
        for (String d : pack.getDepends()) {
            String v = templatePackageRegistry.getRootFolderName(d);
            if (v != null) {
                l.add(session.getValueFactory().createValue(v));
            } else {
                logger.warn("Cannot find dependency {} for package '{}'", d, pack.getName());
            }
        }
        Value[] values = new Value[pack.getDepends().size()];
        m.setProperty("j:dependencies", l.toArray(values));

        if (pack.getModuleType() == null) {
            String moduleType = guessModuleType(session, pack);
            pack.setModuleType(moduleType);
        }

        if (!m.hasNode("templates")) {
            m.addNode("templates", "jnt:templatesFolder");
        }
        JCRNodeWrapper tpls = m.getNode("templates");
        if (!tpls.hasProperty("j:rootTemplatePath") && JahiaTemplateManagerService.MODULE_TYPE_MODULE.equals(pack.getModuleType())) {
            tpls.setProperty("j:rootTemplatePath", "/base");
        }

        if (pack.getResourceBundleName() != null) {
            List<String> langs = new ArrayList<String>();
            Resource[] resources = pack.getResources("resources");
            for (Resource resource : resources) {
                try {
                    String key = resource.getURI().getPath().substring(1).replace("/",".");
                    if (key.startsWith(pack.getResourceBundleName())) {
                        String langCode = StringUtils.substringBetween(key , pack.getResourceBundleName() + "_", ".properties");
                        if (langCode != null) {
                            langs.add(langCode);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Cannot get resources",e);
                }
            }
            JCRNodeWrapper moduleNode = m.getParent();
            if (moduleNode.hasProperty("j:languages")) {
                Value[] oldValues = m.getParent().getProperty("j:languages").getValues();
                for (Value value : oldValues) {
                    if (!langs.contains(value.getString())) {
                        langs.add(value.getString());
                    }
                }
            }
<<<<<<< .working
            moduleNode.setProperty("j:languages", langs.toArray(new String[langs.size()]));
=======
            if (newRemaining.equals(remaining)) {
                String str = "";
                for (JahiaTemplatesPackage item : newRemaining) {
                    unresolvedDependencies.add(item);
                    str += item.getName() + ",";
                }
                logger.error("Cannot deploy packages " + str + " unresolved dependencies {}", Arrays.toString(newRemaining.toArray()));
                break;
            } else {
                remaining = newRemaining;
            }
>>>>>>> .merge-right.r46717
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

    public JahiaTemplatesPackage deployModule(File warFile, JCRSessionWrapper session) throws RepositoryException {
        try {
            String location = warFile.toURI().toString();
            if (warFile.getName().toLowerCase().endsWith(".war")) {
                location = "jahiawar:" + location;
            }
            Bundle bundle = ProvisionActivator.getInstance().getBundleContext().installBundle(location);
            bundle.update();
            bundle.start();
            String moduleName = (String) bundle.getHeaders().get("Jahia-Root-Folder");
            if (moduleName == null) {
                moduleName = bundle.getSymbolicName();
            }
            String version = (String) bundle.getHeaders().get("Implementation-Version");
            if (version == null) {
                version = bundle.getVersion().toString();
            }
            return service.getTemplatePackageRegistry().lookupByFileNameAndVersion(moduleName, new ModuleVersion(version));
        } catch (BundleException e) {
            logger.error("Cannot deploy module",e);
        }

        return null;
    }

    public void undeployModule(JahiaTemplatesPackage pack) throws RepositoryException {
        Bundle[] bundles = ProvisionActivator.getInstance().getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getHeaders().get("Jahia-Root-Folder") != null) {
                String moduleName = bundle.getHeaders().get("Jahia-Root-Folder").toString();
                if (moduleName == null) {
                    moduleName = bundle.getSymbolicName();
                }
                String version = (String) bundle.getHeaders().get("Implementation-Version");
                if (version == null) {
                    version = bundle.getVersion().toString();
                }
                if (moduleName.equals(pack.getRootFolder()) && version.equals(pack.getVersion().toString())) {
                    try {
                        bundle.uninstall();
                        return;
                    } catch (BundleException e) {
                        logger.error("Cannot undeploy module", e);
                    }
                }
            }
        }
    }

}