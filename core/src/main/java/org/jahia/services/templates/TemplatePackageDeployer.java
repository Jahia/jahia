/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.scheduler.SchedulerService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.quartz.SchedulerException;
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

    public static final String PERMISSIONS_XML = "permissions.xml";
    public static final String ROLES_XML = "roles.xml";
    public static final String MODULES = "/modules";
    public static final String SOURCES = "/sources";
    private static final Logger logger = LoggerFactory.getLogger(TemplatePackageDeployer.class);
    protected JahiaTemplateManagerService service;

    private TemplatePackageRegistry templatePackageRegistry;
    private ComponentRegistry componentRegistry;

    private ImportExportBaseService importExportService;

    private SchedulerService schedulerService;

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

    public void initializeModuleContent(JahiaTemplatesPackage aPackage, JCRSessionWrapper session) throws RepositoryException {
        resetModuleNodes(aPackage, session);

        logger.info("Starting import for the module package '{}' including: {}", aPackage.getName(), aPackage.getInitialImports());
        for (String imp : aPackage.getInitialImports()) {
            String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
            Resource importFile = aPackage.getResource(imp);
            logger.info("... importing " + importFile + " into " + targetPath);
            session.getPathMapping().put("/templateSets/", MODULES + "/");
            session.getPathMapping().put(MODULES + "/" + aPackage.getId() + "/", MODULES + "/" + aPackage.getId() + "/" + aPackage.getVersion() + "/");

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
                    importExportService.importSiteZip(importFile, session);
                } else {
                    try (ImportExportBaseService.ImportZipContext importZipContext = new ImportExportBaseService.ImportZipContext(importFile)) {
                        importExportService.importZip(targetPath, importZipContext, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session,
                                new HashSet<>(Arrays.asList(PERMISSIONS_XML, ROLES_XML)), false);
                        if (targetPath.equals("/")) {
                            if (importZipContext.getLoadedImportDescriptorNames().contains(PERMISSIONS_XML)) {
                                Set<String> ignoreAllButNotPermissions = new HashSet<>(importZipContext.getLoadedImportDescriptorNames());
                                ignoreAllButNotPermissions.remove(PERMISSIONS_XML);
                                if (!session.itemExists(MODULES + "/" + aPackage.getIdWithVersion() + "/permissions")) {
                                    session.getNode(MODULES + "/" + aPackage.getIdWithVersion()).addNode("permissions", "jnt:permission");
                                }
                                importExportService.importZip(MODULES + "/" + aPackage.getIdWithVersion(), importZipContext,
                                        DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, ignoreAllButNotPermissions, false);
                            }
                            if (importZipContext.getLoadedImportDescriptorNames().contains(ROLES_XML)) {
                                Set<String> ignoreAllButNotRoles = new HashSet<>(importZipContext.getLoadedImportDescriptorNames());
                                ignoreAllButNotRoles.remove(ROLES_XML);
                                session.getPathMapping().put("/permissions", MODULES + "/" + aPackage.getIdWithVersion() + "/permissions");
                                importExportService.importZip("/", importZipContext, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE,
                                        session, ignoreAllButNotRoles, false);
                                session.getPathMapping().remove("/permissions");
                            }
                        }
                    }
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

        session.save();
    }

    private synchronized void cloneModuleInLive(final JahiaTemplatesPackage pack) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null, session -> {
            try {
                JCRObservationManager.pushEventListenersAvailableDuringPublishOnly();
                if (!session.itemExists(MODULES)) {
                    JahiaAccessManager.setDeniedPaths(Collections.singletonList(MODULES + "/" + pack.getIdWithVersion() + SOURCES));
                    session.getWorkspace().clone(Constants.EDIT_WORKSPACE, MODULES, MODULES, true);
                } else if (!session.itemExists(MODULES + "/" + pack.getId())) {
                    JahiaAccessManager.setDeniedPaths(Collections.singletonList(MODULES + "/" + pack.getIdWithVersion() + SOURCES));
                    session.getWorkspace().clone(Constants.EDIT_WORKSPACE, MODULES + "/" + pack.getId(), MODULES + "/" + pack.getId(), true);
                } else {
                    if (session.itemExists(MODULES + "/" + pack.getIdWithVersion())) {
                        session.getNode(MODULES + "/" + pack.getIdWithVersion()).remove();
                        session.save();
                    }
                    JahiaAccessManager.setDeniedPaths(Collections.singletonList(MODULES + "/" + pack.getIdWithVersion() + SOURCES));
                    session.getWorkspace().clone(Constants.EDIT_WORKSPACE, MODULES + "/" + pack.getIdWithVersion(), MODULES + "/" + pack.getIdWithVersion(), true);
                }
            } finally {
                JahiaAccessManager.setDeniedPaths(null);
                JCRObservationManager.popEventListenersAvailableDuringPublishOnly();
            }
            return null;
        });
    }

    private void resetModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) throws RepositoryException {
        clearModuleNodes(pkg);
        if (initModuleNode(session, pkg, true)) {
            resetModuleAttributes(session, pkg);
        }
        session.save();
    }

    /**
     * Clear all module nodes for given package
     *
     * @param pkg the module package
     * @throws RepositoryException in case of JCR-related errors
     */
    public void clearModuleNodes(final JahiaTemplatesPackage pkg) throws RepositoryException {
        for (String workspace : Arrays.asList(Constants.LIVE_WORKSPACE, Constants.EDIT_WORKSPACE)) {
            clearModuleNodes(workspace, pkg.getId(), pkg.getVersion());
        }
    }

    /**
     * Clear all module nodes for given package via a background job
     *
     * @param pkg the module package
     */
    public void clearModuleNodesAsync(final JahiaTemplatesPackage pkg) {
        String id = pkg.getId();
        ModuleVersion version = pkg.getVersion();
        try {
            schedulerService.scheduleJobNow(ClearModuleNodesJob.createJob(id, version.toString()));
        } catch (SchedulerException e) {
            throw new JahiaRuntimeException(
                    "Unable to schedule background job for cleaning nodes of module " + id + " v" + version, e);
        }
    }

    /**
     * Clear all module nodes for given package id and version
     *
     * @param id      the id of the module to clean nodes for
     * @param version the version of the module
     * @throws RepositoryException in case of JCR-related errors
     */
    public void clearModuleNodes(String id, ModuleVersion version) throws RepositoryException {
        for (String workspace : Arrays.asList(Constants.LIVE_WORKSPACE, Constants.EDIT_WORKSPACE)) {
            clearModuleNodes(workspace, id, version);
        }
    }

    /**
     * clear all module nodes for given package and session
     * Deprecated: use clearModuleNodes(final JahiaTemplatesPackage pkg)
     *
     * @param pkg     the module package
     * @param session current JCR session instance
     * @throws RepositoryException in case of JCR-related errors
     */
    @Deprecated(since = "7.2.1.0", forRemoval = true)
    public void clearModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) throws RepositoryException {
        clearModuleNodes(pkg.getId(), pkg.getVersion(), session);
    }

    private void clearModuleNodes(String workspace, final String id, final ModuleVersion version) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, session -> {
            clearModuleNodes(id, version, session);
            return null;
        });
    }

    /**
     * Clear all module nodes for given package id, version and session
     * if you want to remove a module nodes, use clearModuleNodes(String id, ModuleVersion version) to be sure to remove
     * nodes in both workspaces.
     *
     * @param id      the module ID to clean nodes for
     * @param version the module version
     * @param session current JCR session instance
     * @throws RepositoryException in case of JCR-related errors
     */
    public void clearModuleNodes(String id, ModuleVersion version, JCRSessionWrapper session) throws RepositoryException {
        String modulePath = MODULES + "/" + id + "/" + version;
        if (session.nodeExists(modulePath)) {
            JCRNodeWrapper moduleNode = session.getNode(modulePath);
            NodeIterator nodeIterator = moduleNode.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                if (!next.isNodeType("jnt:versionInfo") && !next.isNodeType("jnt:moduleVersionFolder")) {
                    next.remove();
                }
            }
            session.save();
        }
    }


    private boolean initModuleNode(JCRSessionWrapper session, JahiaTemplatesPackage pack, boolean updateDeploymentDate)
            throws RepositoryException {
        boolean modified = false;
        if (!session.nodeExists(MODULES)) {
            session.getRootNode().addNode("modules", "jnt:modules");
            modified = true;
        }
        JCRNodeWrapper modules = session.getNode(MODULES);
        JCRNodeWrapper m;
        if (!modules.hasNode(pack.getId())) {
            modified = true;
            m = modules.addNode(pack.getId(), "jnt:module");
        } else {
            m = modules.getNode(pack.getId());
        }

        if (!m.hasNode(pack.getVersion().toString())) {
            modified = true;
            m = m.addNode(pack.getVersion().toString(), "jnt:moduleVersion");
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
            v.setProperty("j:sourcesFolder", pack.getSourcesFolder().getPath());
        }
        if (pack.getScmURI() != null && !Constants.SCM_DUMMY_URI.equals(pack.getScmURI())) {
            try {
                v.setProperty("j:scmURI", pack.getScmURI());
                if (pack.getScmTag() != null) {
                    v.setProperty("j:scmTag", pack.getScmTag());
                }
            } catch (Exception e) {
                logger.error("Cannot get SCM url");
            }
        }
        if (updateDeploymentDate) {
            v.setProperty("j:deployementDate", new GregorianCalendar());
            modified = true;
        }
        if (!m.hasNode("files")) {
            modified = true;
            m.addNode("files", "jnt:folder");
        }
        if (!m.hasNode("contents")) {
            modified = true;
            m.addNode("contents", "jnt:contentFolder");
        }
        if (!m.hasNode("templates")) {
            modified = true;
            JCRNodeWrapper tpls = m.addNode("templates", "jnt:templatesFolder");
            if (JahiaTemplateManagerService.MODULE_TYPE_MODULE.equals(pack.getModuleType())) {
                tpls.setProperty("j:rootTemplatePath", "/base");
            }
            tpls.addNode("files", "jnt:folder");
            tpls.addNode("contents", "jnt:contentFolder");
        }

        return modified;
    }

    private void resetModuleAttributes(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        JCRNodeWrapper modules = session.getNode(MODULES);
        JCRNodeWrapper m = modules.getNode(pack.getIdWithVersion());

        m.setProperty("j:title", pack.getName());
        if (pack.getModuleType() != null) {
            m.setProperty("j:moduleType", pack.getModuleType());
        }
        m.setProperty("j:modulePriority", pack.getModulePriority());
        List<Value> l = new ArrayList<Value>();
        for (String d : pack.getDepends()) {
            String v = templatePackageRegistry.getModuleId(d);
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
        if (!m.hasNode("permissions")) {
            m.addNode("permissions", "jnt:permission");
        }
        JCRNodeWrapper perms = m.getNode("permissions");
        if (!perms.hasNode("components")) {
            perms.addNode("components", "jnt:permission");
        }
        if (!perms.hasNode("templates")) {
            perms.addNode("templates", "jnt:permission");
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
                    String key = resource.getURI().getPath().substring(1).replace("/", ".");
                    if (key.startsWith(pack.getResourceBundleName())) {
                        String langCode = StringUtils.substringBetween(key, pack.getResourceBundleName() + "_", ".properties");
                        if (langCode != null) {
                            langs.add(langCode);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Cannot get resources", e);
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
            moduleNode.setProperty("j:languages", langs.toArray(new String[langs.size()]));
        }
    }

    private String guessModuleType(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        String moduleType = JahiaTemplateManagerService.MODULE_TYPE_MODULE;
        if (session.itemExists(MODULES + "/" + pack.getIdWithVersion() + "/j:moduleType")) {
            moduleType = session.getNode(MODULES + "/" + pack.getIdWithVersion()).getProperty("j:moduleType").getValue().getString();
        } else {
            String[] fileNames = new File(pack.getFilePath()).list();
            if (fileNames != null) {
                List<String> files = new ArrayList<String>(Arrays.asList(fileNames));
                files.removeAll(Arrays.asList("META-INF", "WEB-INF", "resources"));
                if (files.isEmpty()) {
                    moduleType = JahiaTemplateManagerService.MODULE_TYPE_SYSTEM;
                }
            }
        }
        return moduleType;
    }

    public JahiaTemplatesPackage deployModule(File jarFile, JCRSessionWrapper session) throws RepositoryException {
        try {
            String location = jarFile.toURI().toString();
            Bundle bundle = FrameworkService.getBundleContext().installBundle(location);
            bundle.update();
            bundle.start();
            String moduleId = BundleUtils.getModuleId(bundle);
            String version = BundleUtils.getModuleVersion(bundle);
            return service.getTemplatePackageRegistry().lookupByIdAndVersion(moduleId, new ModuleVersion(version));
        } catch (BundleException e) {
            logger.error("Cannot deploy module", e);
        }

        return null;
    }

    public void undeployModule(String id, String version) throws RepositoryException {
        Bundle[] bundles = FrameworkService.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            String moduleId = BundleUtils.getModuleId(bundle);
            String moduleVersion = BundleUtils.getModuleVersion(bundle);
            if (moduleId.equals(id) && moduleVersion.equals(version)) {
                try {
                    bundle.uninstall();
                    return;
                } catch (BundleException e) {
                    logger.error("Cannot undeploy module", e);
                }
            }
        }
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

}
