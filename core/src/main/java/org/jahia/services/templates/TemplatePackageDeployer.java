/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.*;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
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

    private TemplatePackageRegistry templatePackageRegistry;
    private ComponentRegistry componentRegistry;

    private ImportExportBaseService importExportService;

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

        logger.info("Starting import for the module package '" + aPackage.getName() + "' including: "
                + aPackage.getInitialImports());
        for (String imp : aPackage.getInitialImports()) {
            String targetPath = "/" + StringUtils.substringAfter(StringUtils.substringBeforeLast(imp, "."), "import-").replace('-', '/');
            Resource importFile = aPackage.getResource(imp);
            logger.info("... importing " + importFile + " into " + targetPath);
            session.getPathMapping().put("/templateSets/", "/modules/");
            session.getPathMapping().put("/modules/" + aPackage.getId() + "/", "/modules/" + aPackage.getId() + "/" + aPackage.getVersion() + "/");

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
//                    importExportService.importZip(targetPath, importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session);
                    importExportService.importZip(targetPath, importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, new HashSet<String>(Arrays.asList("permissions.xml","roles.xml")), false);
                    if (targetPath.equals("/")) {
                        List<String> fileList = new ArrayList<String>();
                        Map<String, Long> sizes = new HashMap<String, Long>();
                        importExportService.getFileList(importFile ,sizes, fileList);
                        if (sizes.containsKey("permissions.xml")) {
                            Set<String> s = new HashSet<String>(sizes.keySet());
                            s.remove("permissions.xml");
                            if (!session.itemExists("/modules/" + aPackage.getIdWithVersion()+"/permissions")) {
                                session.getNode("/modules/" + aPackage.getIdWithVersion()).addNode("permissions","jnt:permission");
                            }
                            importExportService.importZip("/modules/" + aPackage.getIdWithVersion(), importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, s, false);
                        }
                        if (sizes.containsKey("roles.xml")) {
                            Set<String> s = new HashSet<String>(sizes.keySet());
                            s.remove("roles.xml");
                            session.getPathMapping().put("/permissions", "/modules/" + aPackage.getIdWithVersion() + "/permissions");
                            importExportService.importZip("/", importFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, s, false);
                            session.getPathMapping().remove("/permissions");
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
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    JCRObservationManager.pushEventListenersAvailableDuringPublishOnly();
                    if (!session.itemExists("/modules")) {
                        JahiaAccessManager.setDeniedPaths(Arrays.asList("/modules/" + pack.getIdWithVersion() + "/sources"));
                        session.getWorkspace().clone("default", "/modules", "/modules", true);
                    } else if (!session.itemExists("/modules/" + pack.getId())) {
                        JahiaAccessManager.setDeniedPaths(Arrays.asList("/modules/" + pack.getIdWithVersion() + "/sources"));
                        session.getWorkspace().clone("default", "/modules/" + pack.getId(), "/modules/" + pack.getId(), true);
                    } else {
                        if (session.itemExists("/modules/" + pack.getIdWithVersion())) {
                            session.getNode("/modules/" + pack.getIdWithVersion()).remove();
                            session.save();
                        }
                        JahiaAccessManager.setDeniedPaths(Arrays.asList("/modules/" + pack.getIdWithVersion() + "/sources"));
                        session.getWorkspace().clone("default", "/modules/" + pack.getIdWithVersion(), "/modules/" + pack.getIdWithVersion(), true);
                    }
                } finally {
                    JahiaAccessManager.setDeniedPaths(null);
                    JCRObservationManager.popEventListenersAvailableDuringPublishOnly();
                }
                return null;
            }
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
     *  clear all module nodes for given package
     * @param pkg
     * @throws RepositoryException
     */
    public void clearModuleNodes(final JahiaTemplatesPackage pkg) throws RepositoryException {
        for (String workspace : Arrays.asList(Constants.LIVE_WORKSPACE, Constants.EDIT_WORKSPACE)) {
            clearModuleNodes(workspace, pkg.getId(), pkg.getVersion());
        }
    }

    /**
     *  clear all module nodes for given package id and version
     * @param id
     * @param version
     * @throws RepositoryException
     */
    public void clearModuleNodes(String id, ModuleVersion version) throws RepositoryException {
        for (String workspace : Arrays.asList(Constants.LIVE_WORKSPACE, Constants.EDIT_WORKSPACE)) {
            clearModuleNodes(workspace, id, version);
        }
    }

    /**
     * clear all module nodes for given package and session
     * Deprecated: use clearModuleNodes(final JahiaTemplatesPackage pkg)
     * @param pkg
     * @param session
     * @throws RepositoryException
     */
    @Deprecated
    public void clearModuleNodes(JahiaTemplatesPackage pkg, JCRSessionWrapper session) throws RepositoryException {
        clearModuleNodes(pkg.getId(), pkg.getVersion(), session);
    }

    private void clearModuleNodes(String workspace, final String id, final ModuleVersion version) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                clearModuleNodes(id, version, session);
                return null;
            }
        });
    }

    /**
     *  clear all module nodes for given package id, version and session
     *  if you want to remove a module nodes, use clearModuleNodes(String id, ModuleVersion version) to be sure to remove
     *  nodes in both workspaces.
     * @param id
     * @param version
     * @param session
     * @throws RepositoryException
     */
    public void clearModuleNodes(String id, ModuleVersion version, JCRSessionWrapper session) throws RepositoryException {
        String modulePath = "/modules/" + id + "/" + version;
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
        if (!session.nodeExists("/modules")) {
            session.getRootNode().addNode("modules", "jnt:modules");
            modified = true;
        }
        JCRNodeWrapper modules = session.getNode("/modules");
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
            v.setProperty("j:sourcesFolder",pack.getSourcesFolder().getPath());
        }
        if (pack.getScmURI() != null && !Constants.SCM_DUMMY_URI.equals(pack.getScmURI())) {
            try {
                v.setProperty("j:scmURI",pack.getScmURI());
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

        if (!m.hasNode("portlets")) {
            modified = true;
            m.addNode("portlets", "jnt:portletFolder");
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
        JCRNodeWrapper modules = session.getNode("/modules");
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
            moduleNode.setProperty("j:languages", langs.toArray(new String[langs.size()]));
        }
    }

    private String guessModuleType(JCRSessionWrapper session, JahiaTemplatesPackage pack) throws RepositoryException {
        String moduleType = JahiaTemplateManagerService.MODULE_TYPE_MODULE;
        if (session.itemExists("/modules/" + pack.getIdWithVersion() + "/j:moduleType")) {
            moduleType = session.getNode("/modules/" + pack.getIdWithVersion()).getProperty("j:moduleType").getValue().getString();
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
            logger.error("Cannot deploy module",e);
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

}