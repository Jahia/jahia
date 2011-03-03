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

import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader.ContextInitializedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.*;
import java.util.*;

/**
 * Template and template set deployment and management service.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaTemplateManagerService extends JahiaService implements ApplicationListener<ApplicationEvent> {

    /**
     * This event is fired when a template module is re-deployed (in runtime, not on the server startup).
     * 
     * @author Sergiy Shyrkov
     */
	public static class TemplatePackageRedeployedEvent extends ApplicationEvent {
		private static final long serialVersionUID = 789720524077775537L;

		public TemplatePackageRedeployedEvent(Object source) {
			super(source);
		}
	}

	private static final Comparator<JahiaTemplatesPackage> TEMPLATE_PACKAGE_COMPARATOR = new Comparator<JahiaTemplatesPackage>() {
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            if (o1.isDefault()) return 99;
            if (o2.isDefault()) return -99;
            return o1.getName().compareTo(o2.getName());
        }
    };

	private static Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerService.class);

    private TemplatePackageDeployer templatePackageDeployer;

    private TemplatePackageRegistry templatePackageRegistry;
    
    private JahiaSitesService siteService;

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }
    
    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailableTemplatePackages() {
        return templatePackageRegistry.getAvailablePackages();
    }

    public List<ErrorHandler> getErrorHandler() {
        return templatePackageRegistry.getErrorHandlers();
    }

    public Map<String, Action> getActions() {
        return templatePackageRegistry.getActions();
    }

    public Map<String, BackgroundAction> getBackgroundActions() {
        return templatePackageRegistry.getBackgroundActions();
    }
    /**
     * Returns a sorted list of all available template packages having templates for a module.
     *
     * @return a sorted list of all available template packages
     */
    public SortedSet<JahiaTemplatesPackage> getSortedAvailableTemplatePackagesForModule(String moduleName,final RenderContext context) {
        List<JahiaTemplatesPackage> r = templatePackageRegistry.getPackagesPerModule().get(moduleName);
		Comparator<JahiaTemplatesPackage> packageComparator = TEMPLATE_PACKAGE_COMPARATOR;
        SortedSet<JahiaTemplatesPackage> sortedPackages = new TreeSet<JahiaTemplatesPackage>(
                            packageComparator);
        if (r != null) {
            sortedPackages.addAll(r);
        }
        return sortedPackages;
    }

    /**
     * Returns a list of all available template packages having templates for a module.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailableTemplatePackagesForModule(String moduleName) {
        List<JahiaTemplatesPackage> r = templatePackageRegistry.getPackagesPerModule().get(moduleName);
        if (r == null) {
            return Collections.emptyList();
        }
        return r;
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailableTemplatePackagesCount() {
        return templatePackageRegistry.getAvailablePackagesCount();
    }

    /**
     * Returns the default template package (the name is configured in the
     * <code>jahia.properties</code> file).
     *
     * @return the default template package (the name is configured in the
     *         <code>jahia.properties</code> file)
     */
    public JahiaTemplatesPackage getDefaultTemplatePackage() {
        String defSetName = settingsBean.lookupString("default_templates_set");
        return defSetName != null
                && templatePackageRegistry.lookup(defSetName) != null ? templatePackageRegistry
                .lookup(defSetName)
                : (JahiaTemplatesPackage) templatePackageRegistry
                        .getAvailablePackages().get(0);
    }

    public String getTemplateDisplayName(String templateName, int siteId) {
        String displayName = templateName;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unable to lookup the site for the ID=" + siteId, e);
        }
        if (site != null) {
            JahiaTemplatesPackage pkg = getTemplatePackage(site
                    .getTemplatePackageName());
            if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
                displayName = pkg.lookupTemplate(templateName).getDisplayName();
            }
        }
        if (null == displayName) {
            logger
                    .warn("Unable to lookup display name for the template named '"
                            + templateName + "' in the site with ID=" + siteId);
        }

        return displayName;
    }

    public String getTemplateDescription(String templateName, int siteId) {
        String description = null;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unable to lookup the site for the ID=" + siteId, e);
        }
        if (site != null) {
            JahiaTemplatesPackage pkg = getTemplatePackage(site
                    .getTemplatePackageName());
            if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
                description = pkg.lookupTemplate(templateName).getDescription();
            }
        }
        return description;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package can not be found in the registry.
     *
     * @param siteId the ID of the site
     * @return the requested template package or <code>null</code> if the
     *         package can not be found in the registry
     */
    public JahiaTemplatesPackage getTemplatePackage(int siteId) {
        JahiaTemplatesPackage pkg = null;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unablke to find site by ID=" + siteId, e);
        }
        if (site != null) {
            String pkgName = site.getTemplatePackageName();
            if (pkgName != null) {
                pkg = templatePackageRegistry.lookup(pkgName);
            }
        }

        return pkg;
    }

    /**
     * Returns a list of {@link RenderFilter} instances, configured for the specified templates package.
     * 
     * @return a list of {@link RenderFilter} instances, configured for the specified templates package
     */
    public List<RenderFilter> getRenderFilters() {
        return templatePackageRegistry.getRenderFilters();
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified name is not
     * registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage getTemplatePackage(String packageName) {
        return templatePackageRegistry.lookup(packageName);
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified fileName is not
     * registered in the repository.
     *
     * @param fileName the template package fileName to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage getTemplatePackageByFileName(String fileName) {
        return templatePackageRegistry.lookupByFileName(fileName);
    }

    public String getTemplateSourcePath(String templateName, int siteId) {
        String sourcePath = null;
        JahiaTemplatesPackage pkg = getTemplatePackage(siteId);
        if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
            sourcePath = pkg.lookupTemplate(templateName).getFilePath();
        }
        if (null == sourcePath) {
            logger
                    .warn("Unable to lookup the source path for the template named '"
                            + templateName + "' in the site with ID=" + siteId);
        }

        return sourcePath;
    }

    public void setTemplatePackageDeployer(TemplatePackageDeployer deployer) {
        templatePackageDeployer = deployer;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void start() throws JahiaInitializationException {
        logger.info("Starting JahiaTemplateManagerService ...");

        // deploy shared templates if not deployed yet
        templatePackageDeployer.deploySharedTemplatePackages();

        // scan the directory for templates
        templatePackageDeployer.registerTemplatePackages();

        // TODO validate template sets: count, package dependencies etc.
        templatePackageRegistry.validate();

        // start template package watcher
		templatePackageDeployer.startWatchdog();

        logger.info("JahiaTemplateManagerService started successfully."
                + " Total number of found template packages: "
                + templatePackageRegistry.getAvailablePackagesCount());
    }

    public void stop() throws JahiaException {
        logger.info("Stopping JahiaTemplateManagerService ...");

        // stop template package watcher
        templatePackageDeployer.stopWatchdog();

        templatePackageRegistry.reset();

        logger.info("... JahiaTemplateManagerService stopped successfully");
    }

    public String getCurrentResourceBundleName(ProcessingContext ctx) {
        return getTemplatePackage(ctx.getSite().getTemplatePackageName())
                .getResourceBundleName();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextInitializedEvent) {
            // perform initial imports if any
            templatePackageDeployer.performInitialImport();
        } else if (event instanceof TemplatePackageRedeployedEvent) {
            // flush resource bundle cache
            JahiaTemplatesRBLoader.clearCache();
	    }
    }

    public void createModule(String moduleName, boolean isModule) {
        File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), moduleName);
        if (tmplRootFolder.exists()) {
            return;
        }
        if (!tmplRootFolder.exists()) {
            logger.info("Start creating new template package '" + moduleName + "'");

            tmplRootFolder.mkdirs();

            new File(tmplRootFolder, "META-INF").mkdirs();
            new File(tmplRootFolder, "WEB-INF").mkdirs();
            new File(tmplRootFolder, "resources").mkdirs();
            new File(tmplRootFolder, "css").mkdirs();
            if (!isModule) {
                new File(tmplRootFolder, "jnt_template/html").mkdirs();
                File defaultTpl = new File(settingsBean.getJahiaTemplatesDiskPath() + "/default/jnt_template/html/template.jsp");
                if (defaultTpl.exists()) {
                    File out = new File(tmplRootFolder, "jnt_template/html/template."+moduleName+".jsp");
                    FileInputStream source = null;
                    FileOutputStream target = null;
                    try {
                    	source = new FileInputStream(defaultTpl);
                    	target = new FileOutputStream(out);
                        IOUtils.copy(source, target);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                    	IOUtils.closeQuietly(source);
                    	IOUtils.closeQuietly(target);
                    }
                }
            }
            try {
                File manifest = new File(tmplRootFolder + "/META-INF/MANIFEST.MF");


                BufferedWriter writer = new BufferedWriter(new FileWriter(manifest));
                writer.write("Manifest-Version: 1.0");
                writer.newLine();
                writer.write("Created-By: Jahia");
                writer.newLine();
                writer.write("Built-By: "+ JCRSessionFactory.getInstance().getCurrentUser().getName());
                writer.newLine();
//                writer.write("Build-Jdk: 1.6.0_20");
                writer.write("depends: Default Jahia Templates");
                writer.newLine();
                writer.write("package-name: "+moduleName);
                writer.newLine();
                writer.write("root-folder: "+moduleName);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            templatePackageRegistry.register(templatePackageDeployer.getPackage(tmplRootFolder));
            logger.info("Package '" + moduleName + "' successfully created");
        }
    }

    public void regenerateImportFile(final String moduleName) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File xmlImportFile = new File(new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), moduleName), "META-INF/import.xml");
                    if (xmlImportFile.exists()) {
                        xmlImportFile.delete();
                    }
                    File importFile = new File(new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), moduleName), "META-INF/import.zip");
                    if (importFile.exists()) {
                        importFile.delete();
                    }
                    Map<String,Object> params = new HashMap<String, Object>();
                    params.put(ImportExportService.XSL_PATH,SettingsBean.getInstance().getJahiaEtcDiskPath()+"/repository/export/templatesCleanup.xsl");

                    ImportExportBaseService
                            .getInstance().exportZip(session.getNode("/templateSets/"+moduleName), session.getRootNode(),
                            new FileOutputStream(importFile), params);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                } catch (SAXException e) {
                    logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } catch (JDOMException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public void deployTemplates(final String templatesPath, final String sitePath, String username)
            throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        JCRTemplate.getInstance()
                .doExecuteWithSystemSession(username, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        HashMap<String, List<String>> references = new HashMap<String, List<String>>();

                        JCRNodeWrapper originalNode = session.getNode(templatesPath);
                        JCRNodeWrapper destinationNode = session.getNode(sitePath);

                        String moduleName = null;
                        if (originalNode.hasProperty("j:siteType") && originalNode.getProperty("j:siteType").getString().equals("module")) {
                            moduleName = originalNode.getName();
                        }
                        List<String> newNodes = new ArrayList<String>();
                        synchro(originalNode, destinationNode, session, moduleName, references, newNodes);

                        ReferencesHelper.resolveCrossReferences(session, references);
                        session.save();

                        for (String newNode : newNodes) {
                            JCRPublicationService.getInstance().publishByMainId(newNode, "default", "live", null, true, null);
                        }

                        JCRPublicationService.getInstance().publishByMainId(destinationNode.getNode("templates").getIdentifier(), "default", "live", null, true, null);

                        JCRPropertyWrapper installedModules = destinationNode.getProperty("j:installedModules");
                        Value[] values = installedModules.getValues();
                        for (Value value : values) {
                            if (value.getString().equals(originalNode.getName())) {
                                return null;
                            }
                        }
                        destinationNode.checkout();
                        installedModules.addValue(originalNode.getName());
                        session.save();
                        try {
                            List<String> modules = siteService.getSiteByKey(destinationNode.getName()).getInstalledModules();
                            if (!modules.contains(originalNode.getName())) {
                                modules.add(originalNode.getName());
                            }
                        } catch (JahiaException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        return null;
                    }
                });
    }

    public void synchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode, JCRSessionWrapper session, String moduleName,
                        Map<String, List<String>> references, List<String> newNodes) throws RepositoryException {
        if (source.isNodeType("jnt:virtualsite")) {
            session.getUuidMapping().put(source.getIdentifier(), destinationNode.getIdentifier());
            NodeIterator ni = source.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                JCRNodeWrapper node;
                boolean newNode = false;
                if (destinationNode.hasNode(child.getName())) {
                    node = destinationNode.getNode(child.getName());
                } else {
                    session.checkout(destinationNode);
                    node = destinationNode.addNode(child.getName(), child.getPrimaryNodeTypeName());
                    session.save();
                    newNode = true;
                }

                templatesSynchro(child, node, session, references, newNodes, newNode, false, true, moduleName, child.isNodeType("jnt:templatesFolder"));
            }
        }
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, Map<String, List<String>> references, List<String> newNodes, boolean doUpdate, boolean doRemove, boolean doChildren, String moduleName, boolean inTemplatesFolder)
            throws RepositoryException {
        if ("j:acl".equals(destinationNode.getName())) {
            return;
        }

        logger.debug("Synchronizing node : " +destinationNode.getPath() + ", update="+doUpdate + "/remove=" + doRemove + "/children=" + doChildren);

        // Set for jnt:template nodes : declares if the template was originally created with that module, false otherwise
//        boolean isCurrentModule = (!destinationNode.hasProperty("j:moduleTemplate") && moduleName == null) || (destinationNode.hasProperty("j:moduleTemplate") && destinationNode.getProperty("j:moduleTemplate").getString().equals(moduleName));

        session.checkout(destinationNode);

        final Map<String, String> uuidMapping = session.getUuidMapping();

        NodeType[] mixin = source.getMixinNodeTypes();
        for (NodeType aMixin : mixin) {
            destinationNode.addMixin(aMixin.getName());
        }

        uuidMapping.put(source.getIdentifier(), destinationNode.getIdentifier());

        List<String> names = new ArrayList<String>();

        if (doUpdate) {
            if (source.hasProperty("jcr:language") && (!destinationNode.hasProperty("jcr:language") ||
                    (!destinationNode.getProperty("jcr:language").getString().equals(source.getProperty("jcr:language").getString())))) {
                destinationNode.setProperty("jcr:language", source.getProperty("jcr:language").getString());
            }

            PropertyIterator props = source.getProperties();

            while (props.hasNext()) {
                Property property = props.nextProperty();
                names.add(property.getName());
                try {
                    if (!property.getDefinition().isProtected() &&
                            !Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                        if (property.getType() == PropertyType.REFERENCE ||
                                property.getType() == PropertyType.WEAKREFERENCE) {
                            if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                                destinationNode.setProperty(property.getName(), new Value[0]);
                                Value[] values = property.getValues();
                                for (Value value : values) {
                                    keepReference(destinationNode, references, property, value.getString());
                                }
                            } else {
                                keepReference(destinationNode, references, property, property.getValue().getString());
                            }
                        } else if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            if (!destinationNode.hasProperty(property.getName()) ||
                                    !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(), property.getValues())) {
                                destinationNode.setProperty(property.getName(), property.getValues());
                            }
                        } else if (!destinationNode.hasProperty(property.getName()) ||
                                !destinationNode.getProperty(property.getName()).getValue().equals(property.getValue())) {
                            destinationNode.setProperty(property.getName(), property.getValue());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
                }
            }

            PropertyIterator pi = destinationNode.getProperties();
            while (pi.hasNext()) {
                JCRPropertyWrapper oldChild = (JCRPropertyWrapper) pi.next();
                if (!oldChild.getDefinition().isProtected()) {
                    if (!names.contains(oldChild.getName()) && !oldChild.getName().equals("j:published") && !oldChild.getName().equals("j:moduleTemplate")) {
                        oldChild.remove();
                    }
                }
            }

            mixin = destinationNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                if (!source.isNodeType(aMixin.getName())) {
                    destinationNode.removeMixin(aMixin.getName());
                }
            }
        }

        NodeIterator ni = source.getNodes();

        names.clear();

        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            boolean isTemplateNode = child.isNodeType("jnt:template");
            boolean isPageNode = child.isNodeType("jnt:page");

            if (doChildren || isTemplateNode) {
                names.add(child.getName());

                boolean currentModule = false;
                boolean newNode = false;
                JCRNodeWrapper node;
                if (destinationNode.hasNode(child.getName())) {
                    node = destinationNode.getNode(child.getName());
                    currentModule = (!node.hasProperty("j:moduleTemplate") && moduleName == null) || (node.hasProperty("j:moduleTemplate") && node.getProperty("j:moduleTemplate").getString().equals(moduleName));
                } else {
                    node = destinationNode.addNode(child.getName(), child.getPrimaryNodeTypeName());
                    newNode = true;
                    if (!inTemplatesFolder) {
                        newNodes.add(node.getIdentifier());
                    }
                    if (moduleName != null && node.isNodeType("jnt:template")) {
                        node.setProperty("j:moduleTemplate", moduleName);
                        currentModule = true;
                    }
                }
                if (isTemplateNode) {
                    templatesSynchro(child, node, session, references, newNodes, currentModule, currentModule, currentModule, moduleName, inTemplatesFolder);
                } else {
                    templatesSynchro(child, node, session, references, newNodes, inTemplatesFolder || newNode, doRemove, doChildren && !(isPageNode && !newNode), moduleName, inTemplatesFolder);
                }
            }
        }
        if (doRemove) {
            logger.debug("Remove unwanted child of : " +destinationNode.getPath());
            ni = destinationNode.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper oldDestChild = (JCRNodeWrapper) ni.next();
                if (!names.contains(oldDestChild.getName()) &&
                        ((!oldDestChild.isNodeType("jnt:template")) ||
                                (!oldDestChild.hasProperty("j:moduleTemplate") && moduleName == null) ||
                                (oldDestChild.hasProperty("j:moduleTemplate") && oldDestChild.getProperty("j:moduleTemplate").getString().equals(moduleName)))) {
                    logger.debug(oldDestChild.getPath());
                    oldDestChild.remove();
                }
            }
        }

        List<String> destNames = new ArrayList<String>();
        ni = destinationNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper oldChild = (JCRNodeWrapper) ni.next();
            destNames.add(oldChild.getName());
        }
        if (destinationNode.getPrimaryNodeType().hasOrderableChildNodes() && !names.equals(destNames)) {
            Collections.reverse(names);
            String previous = null;
            for (String name : names) {
                destinationNode.orderBefore(name, previous);
                previous = name;
            }
        }
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property,
                               String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }

}