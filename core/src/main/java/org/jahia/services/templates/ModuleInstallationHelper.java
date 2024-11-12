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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.observation.JahiaEventService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SitesSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Module installation helper.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleInstallationHelper implements ApplicationEventPublisherAware {

    private static Logger logger = LoggerFactory.getLogger(ModuleInstallationHelper.class);

    private ApplicationEventPublisher applicationEventPublisher;

    private JahiaSitesService siteService;

    private TemplatePackageRegistry templatePackageRegistry;

    private JahiaEventService jahiaEventService;

    private boolean addDependencyValue(JCRNodeWrapper originalNode, JCRNodeWrapper destinationNode, String propertyName)
            throws RepositoryException {
        String valueToBeAdded = originalNode.getName();
        String newStringValue = valueToBeAdded;
        if (destinationNode.hasProperty(propertyName)) {
            JCRPropertyWrapper installedModules = destinationNode.getProperty(propertyName);
            Value[] values = installedModules.getValues();
            List<String> stringValues = new LinkedList<>();
            for (Value value : values) {
                String strVal = value.getString();
                stringValues.add(strVal);
                if (strVal.equals(valueToBeAdded)) {
                    return true;
                }
            }
            stringValues.add(valueToBeAdded);
            destinationNode.setProperty(propertyName, stringValues.toArray(new String[] {}));
        } else {
            destinationNode.setProperty(propertyName, new String[] { newStringValue });
        }
        return false;
    }

    public void autoInstallModulesToSites(JahiaTemplatesPackage module, JCRSessionWrapper session)
            throws RepositoryException {
        Set<String> autoInstalled = new HashSet<String>();
        if (StringUtils.isNotBlank(module.getAutoDeployOnSite())) {
            if ("system".equals(module.getAutoDeployOnSite()) || "systemsite".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModule(module, "/sites/systemsite", session);
                    autoInstalled.add("systemsite");
                }
            } else if ("all".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModuleOnAllSites(module, session, null);
                    return;
                }
            }
        }

        List<JCRNodeWrapper> sites = new ArrayList<JCRNodeWrapper>();
        NodeIterator ni = session.getNode("/sites").getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (autoInstalled.contains(next.getName())) {
                continue;
            }
            if (next.hasProperty(SitesSettings.INSTALLED_MODULES)) {
                Value[] v = next.getProperty(SitesSettings.INSTALLED_MODULES).getValues();
                for (Value value : v) {
                    if (value.getString().equals(module.getId())) {
                        sites.add(next);
                    }
                }
            }
        }
        if (!sites.isEmpty()) {
            installModuleOnAllSites(module, session, sites);
        }
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session)
            throws RepositoryException {
        installModules(Collections.singletonList(module), sitePath, session);
    }

    public void installModule(final String moduleId, final String sitePath, String username) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                installModules(Collections.singletonList(templatePackageRegistry.lookupById(moduleId)), sitePath, session);
                session.save();
                return null;
            }
        });
    }

    public void installModule(final String moduleId, final String version, final String sitePath, String username) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                installModules(Collections.singletonList(templatePackageRegistry.lookupByIdAndVersion(moduleId, new ModuleVersion(version))), sitePath, session);
                session.save();
                return null;
            }
        });
    }

    public void installModuleOnAllSites(JahiaTemplatesPackage module, JCRSessionWrapper sessionWrapper,
            List<JCRNodeWrapper> sites) throws RepositoryException {
        if (sites == null) {
            sites = new ArrayList<JCRNodeWrapper>();
            NodeIterator ni = sessionWrapper.getNode("/sites").getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                if(!next.isNodeType("jnt:virtualsite")) {
                	continue;
                }
                sites.add(next);
            }
        }

        JCRNodeWrapper tpl = sessionWrapper.getNode("/modules/" + module.getIdWithVersion());
        for (JCRNodeWrapper site : sites) {
            if(!site.isNodeType("jnt:virtualsite")) {
            	continue;
            }
            if (tpl.hasProperty("j:moduleType")
                    && JahiaTemplateManagerService.MODULE_TYPE_TEMPLATES_SET.equals(tpl.getProperty("j:moduleType")
                            .getString())) {
                if (tpl.getName().equals(site.getResolveSite().getTemplateFolder())) {
                    installModule(module, site.getPath(), sessionWrapper);
                }
            } else {
                installModule(module, site.getPath(), sessionWrapper);
            }
        }
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath,
                               final JCRSessionWrapper session) throws RepositoryException {
        installModules(modules, sitePath, session, false);
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath,
            final JCRSessionWrapper session, boolean installModulesWithoutPages) throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        final JCRSiteNode siteNode = (JCRSiteNode) session.getNode(sitePath);

        HashMap<String, List<String>> references = new HashMap<String, List<String>>();
        for (JahiaTemplatesPackage module : modules) {
            logger.info("Installing " + module.getName() + " on " + sitePath);
            JCRNodeWrapper moduleNode = null;
            try {
                moduleNode = session.getNode("/modules/" + module.getId());

                String moduleName = moduleNode.getName();

                if (moduleNode.isNodeType("jnt:module")) {
                    moduleNode = moduleNode.getNode(module.getVersion().toString());
                }
                synchro(moduleNode, siteNode, session, moduleName, references, installModulesWithoutPages);

                ReferencesHelper.resolveCrossReferences(session, references);

                addDependencyValue(moduleNode.getParent(), siteNode, SitesSettings.INSTALLED_MODULES);
                logger.info("Done installing " + module.getName() + " on " + sitePath);
            } catch (PathNotFoundException e) {
                logger.warn("Cannot find module for path {}. Skipping deployment to site {}.", module, sitePath);
                return;
            }

        }

        JahiaTemplateManagerService.ModuleDeployedOnSiteEvent event = new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(sitePath,
                ModuleInstallationHelper.class.getName());
        applicationEventPublisher.publishEvent(event);
        jahiaEventService.publishEvent(event);
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property,
            String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }

    public void purgeModuleContent(final List<String> modules, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        QueryManager manager = session.getWorkspace().getQueryManager();
        for (String module : modules) {
            String workspaceName = session.getWorkspace().getName();
            logger.info("Purging content in workspace {} for node types, defined in module {}", workspaceName, module);

            NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(module);
            while (nti.hasNext()) {
                ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
                String nodeTypeName = nodeType.getName();
                Query q = manager.createQuery("select * from ['" + nodeTypeName
                        + "'] as c where isdescendantnode(c,'" + JCRContentUtils.sqlEncode(sitePath) + "')", Query.JCR_SQL2);
                boolean isMixin = nodeType.isMixin();
                try {
                    NodeIterator ni = q.execute().getNodes();
                    int processedCount = 0;
                    while (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.nextNode();
                        if (isMixin) {
                            nodeWrapper.removeMixin(nodeTypeName);
                        } else {
                            nodeWrapper.remove();
                        }
                        processedCount++;
                    }
                    if (processedCount > 0) {
                        if (isMixin) {
                            logger.info("Removed mixin {} from {} node(s) in workspace {}",
                                    new Object[] { nodeTypeName, processedCount, workspaceName });
                        } else {
                            logger.info("Removed {} node(s) of type {} in workspace {}",
                                    new Object[] { processedCount, nodeTypeName, workspaceName });
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot remove node", e);
                }
            }

            logger.info("... done purging content in workspace {} for node types, defined in module {}", workspaceName,
                    module);
        }
    }

    public boolean checkExistingContent(final String module) throws RepositoryException {
        final JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryManager manager = session.getWorkspace().getQueryManager();
                NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(module);
                while (nti.hasNext()) {
                    ExtendedNodeType next = (ExtendedNodeType) nti.next();
                    Query q = manager.createQuery("select * from ['" + next.getName() + "']", Query.JCR_SQL2);
                    if (q.execute().getRows().hasNext()) {
                        return true;
                    }
                }
                return false;
            }
        };
        return JCRTemplate.getInstance().doExecuteWithSystemSession(callback) ||
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null, callback);
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void setJahiaEventService(JahiaEventService jahiaEventService) {
        this.jahiaEventService = jahiaEventService;
    }

    public void synchro(JCRNodeWrapper source, JCRNodeWrapper destinationNode, JCRSessionWrapper session,
                        String moduleName, Map<String, List<String>> references, Boolean skipPages) throws RepositoryException {
        if (source.isNodeType("jnt:moduleVersion")) {
            session.getUuidMapping().put(source.getIdentifier(), destinationNode.getIdentifier());
            NodeIterator ni = source.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (child.isNodeType("jnt:versionInfo") || child.isNodeType("jnt:moduleVersionFolder") || child.isNodeType("jnt:externalProviderExtension")
                        || child.isNodeType("jnt:templatesFolder") || child.isNodeType("jnt:componentFolder") || child.isNodeType("jnt:permission") || (skipPages && child.isNodeType("jnt:page"))) {
                    continue;
                }
                JCRNodeWrapper node;
                boolean newNode = false;
                String childName = child.getName();
                if (destinationNode.hasNode(childName)) {
                    node = destinationNode.getNode(childName);
                } else {
                    session.checkout(destinationNode);
                    String primaryNodeTypeName = child.getPrimaryNodeTypeName();
                    node = destinationNode.addNode(childName, primaryNodeTypeName);
                    newNode = true;
                }
                templatesSynchro(child, node, session, references, newNode, true);
            }
        }
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
            JCRSessionWrapper session, Map<String, List<String>> references, boolean doUpdate, boolean doChildren)
            throws RepositoryException {
        if ("j:acl".equals(destinationNode.getName())) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Synchronizing node : " + destinationNode.getPath() + ", update=" + doUpdate + "/children="
                    + doChildren);
        }

        session.checkout(destinationNode);

        final Map<String, String> uuidMapping = session.getUuidMapping();

        ExtendedNodeType[] mixin = source.getMixinNodeTypes();
        List<ExtendedNodeType> destMixin = Arrays.asList(destinationNode.getMixinNodeTypes());
        for (ExtendedNodeType aMixin : mixin) {
            if (!destMixin.contains(aMixin)) {
                destinationNode.addMixin(aMixin.getName());
            }
        }

        uuidMapping.put(source.getIdentifier(), destinationNode.getIdentifier());

        List<String> names = new ArrayList<String>();

        if (doUpdate) {
            if (source.hasProperty(Constants.JCR_LANGUAGE)
                    && (!destinationNode.hasProperty(Constants.JCR_LANGUAGE) || (!destinationNode
                            .getProperty(Constants.JCR_LANGUAGE).getString()
                            .equals(source.getProperty(Constants.JCR_LANGUAGE).getString())))) {
                destinationNode.setProperty(Constants.JCR_LANGUAGE, source.getProperty(Constants.JCR_LANGUAGE)
                        .getString());
            }

            PropertyIterator props = source.getProperties();

            while (props.hasNext()) {
                Property property = props.nextProperty();
                names.add(property.getName());
                try {
                    if (!property.getDefinition().isProtected()
                            && !Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                        if (property.getType() == PropertyType.REFERENCE
                                || property.getType() == PropertyType.WEAKREFERENCE) {
                            if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                                if (!destinationNode.hasProperty(property.getName())
                                        || !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(),
                                                property.getValues())) {
                                    destinationNode.setProperty(property.getName(), new Value[0]);
                                    Value[] values = property.getValues();
                                    for (Value value : values) {
                                        keepReference(destinationNode, references, property, value.getString());
                                    }
                                }
                            } else {
                                if (!destinationNode.hasProperty(property.getName())
                                        || !destinationNode.getProperty(property.getName()).getValue()
                                                .equals(property.getValue())) {
                                    keepReference(destinationNode, references, property, property.getValue()
                                            .getString());
                                }
                            }
                        } else if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            if (!destinationNode.hasProperty(property.getName())
                                    || !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(),
                                            property.getValues())) {
                                destinationNode.setProperty(property.getName(), property.getValues());
                            }
                        } else if (!destinationNode.hasProperty(property.getName())
                                || !destinationNode.getProperty(property.getName()).getValue()
                                        .equals(property.getValue())) {
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
                    if (!names.contains(oldChild.getName()) && !oldChild.getName().equals("j:published")
                            && !oldChild.getName().equals(Constants.JAHIA_MODULE_TEMPLATE)
                            && !oldChild.getName().equals("j:sourceTemplate")) {
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
            boolean isPageNode = child.isNodeType("jnt:page");

            if (doChildren) {
                names.add(child.getName());

                boolean newNode = false;
                JCRNodeWrapper node;
                if (destinationNode.hasNode(child.getName())) {
                    node = destinationNode.getNode(child.getName());
                } else {
                    node = destinationNode.addNode(child.getName(), child.getPrimaryNodeTypeName());
                    newNode = true;
                }
                templatesSynchro(child, node, session, references, newNode, doChildren && (!isPageNode || newNode));
            }
        }
        if (doUpdate) {
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
    }

    public void uninstallModule(final JahiaTemplatesPackage module, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        uninstallModules(Collections.singletonList(module.getId()), sitePath, session);
    }

    private boolean uninstallModule(String sitePath, JCRSessionWrapper session, JCRSiteNode siteNode,
            String module) throws RepositoryException {
        logger.info("Uninstalling " + module + " on " + sitePath);
        try {
            JCRPropertyWrapper installedModules = siteNode.getProperty(SitesSettings.INSTALLED_MODULES);
            List<String> stringValues = new LinkedList<>();
            Value toBeRemoved = null;
            Value[] values = installedModules.getValues();
            for (Value value : values) {
                String strValue = value.getString();
                if (strValue.equals(module)) {
                    toBeRemoved = value;
                } else {
                    stringValues.add(strValue);
                }
            }
            if (toBeRemoved != null) {
                siteNode.setProperty(SitesSettings.INSTALLED_MODULES, stringValues.toArray(new String[] {}));
            }
            logger.info("Done uninstalling " + module + " on " + sitePath);
        } catch (PathNotFoundException e) {
            logger.warn("Cannot find module for path {}. Skipping deployment to site {}.", module, sitePath);
            return true;
        }
        return false;
    }

    public void uninstallModule(final String module, final String sitePath, String username,
            final boolean purgeAllContent) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                uninstallModules(Collections.singletonList(module), sitePath, session);
                if (purgeAllContent) {
                    purgeModuleContent(Collections.singletonList(module), sitePath,
                            session);
                }
                session.save();
                return null;
            }
        });
        if (purgeAllContent) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(username, "live", new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeModuleContent(Collections.singletonList(module), sitePath, session);
                    session.save();
                    return null;
                }
            });
        }
    }

    public void uninstallModules(final List<String> modules, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        final JCRSiteNode siteNode = (JCRSiteNode) session.getNode(sitePath);

        for (String module : modules) {
            if (uninstallModule(sitePath, session, siteNode, module)) {
                return;
            }
        }

        JahiaTemplateManagerService.ModuleDeployedOnSiteEvent event = new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(sitePath,
                ModuleInstallationHelper.class.getName());
        applicationEventPublisher.publishEvent(event);
        jahiaEventService.publishEvent(event);
    }

    public void uninstallModulesFromAllSites(final String module, final JCRSessionWrapper session)
            throws RepositoryException {
        uninstallModulesFromAllSites(Collections.singletonList(module), session);
    }

    public void uninstallModulesFromAllSites(final List<String> modules, final JCRSessionWrapper session)
            throws RepositoryException {
        List<JCRSiteNode> sitesList = siteService.getSitesNodeList(session);
        for (JCRSiteNode jahiaSite : sitesList) {
            for (String module : modules) {
                if (uninstallModule(jahiaSite.getName(), session, jahiaSite, module)) {
                    return;
                }
                JahiaTemplateManagerService.ModuleDeployedOnSiteEvent event = new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(
                        jahiaSite.getName(), ModuleInstallationHelper.class.getName());
                applicationEventPublisher.publishEvent(event);
                jahiaEventService.publishEvent(event);
            }
        }
    }

    public void uninstallModulesFromAllSites(final String module, final String username, final boolean purgeAllContent)
            throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                uninstallModulesFromAllSites(module, session);
                if (purgeAllContent) {
                    purgeModuleContent(Collections.singletonList(module), "/sites", session);
                }
                session.save();
                return null;
            }
        });
        if (purgeAllContent) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(username, "live", new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeModuleContent(Collections.singletonList(module), "/sites", session);
                    session.save();
                    return null;
                }
            });
        }
    }

}
