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
package org.jahia.ajax.gwt.helper;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.ajax.gwt.client.data.GWTResourceBundle;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.sites.SitesSettings;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.visibility.VisibilityConditionRule;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.version.Version;
import java.io.File;
import java.util.*;

/**
 * Helper class for populating {@link GWTJahiaNode} instances with the information from the corresponding {@link Node}.
 *
 * @author Sergiy Shyrkov
 */
class NodeHelper {

    private static final Logger logger = LoggerFactory.getLogger(NodeHelper.class);

    /**
     * Get node url depending
     *
     * @param workspace
     * @param locale
     */
    static String getNodeURL(String servlet, JCRNodeWrapper node, Date versionDate,
                             String versionLabel, final String workspace, final Locale locale, boolean findDisplayable)
            throws RepositoryException {
        if (servlet == null) {
            servlet = "render";
        }
        String url = Jahia.getContextPath() + "/cms/" + servlet + "/" + workspace + "/" + locale;

        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        RenderContext renderContext = new RenderContext(null, null, node.getSession().getUser());
        renderContext.setMainResource(resource);
        renderContext.setServletPath("/cms/" + servlet);
        JCRNodeWrapper nodeForURL = node;
        if (findDisplayable) {
            nodeForURL = JCRContentUtils.findDisplayableNode(node, renderContext);
            if (nodeForURL != null && !nodeForURL.getIdentifier().equals(node.getIdentifier())) {
                resource = new Resource(nodeForURL, "html", null, Resource.CONFIGURATION_PAGE);
            }
        }
        Template template = RenderService.getInstance().resolveTemplate(resource, renderContext);
        String extensionName;
        if (template != null || nodeForURL.isNodeType("jnt:page") || nodeForURL.isNodeType("jmix:mainResource")) {
            extensionName = ".html";
        } else {
            extensionName = ".content-template.html";
        }

        url += nodeForURL.getPath() + extensionName;

        if (versionDate != null) {
            url += "?v=" + (versionDate.getTime());
            if (versionLabel != null) {
                url += "&l=" + versionLabel;
            }
        }

        return url;
    }

    private static Object getPropertyValue(Value value, JCRSessionWrapper session)
            throws RepositoryException {
        switch (value.getType()) {
            case PropertyType.DATE:
                return value.getDate().getTime();
            case PropertyType.REFERENCE:
            case PropertyType.WEAKREFERENCE:
                try {
                    return session.getNodeByUUID(value.getString()).getPath();
                } catch (ItemNotFoundException e) {
                }
            default:
                return value.getString();
        }
    }

    private static void setPropertyValue(GWTJahiaNode n, JCRPropertyWrapper property,
                                         JCRSessionWrapper session) throws RepositoryException {
        if (property.isMultiple()) {
            Value[] values = property.getValues();
            List<Object> l = new ArrayList<Object>();
            for (Value value : values) {
                l.add(getPropertyValue(value, session));
            }
            n.set(property.getName(), l);
        } else {
            Value value = property.getValue();
            n.set(property.getName(), getPropertyValue(value, session));
        }
    }

    private JCRVersionService jcrVersionService;

    private LanguageHelper languages;
    private PublicationHelper publication;
    private JCRSessionFactory sessionFactory;

    private VisibilityService visibilityService;

    private WorkflowHelper workflow;

    GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node) {
        return getGWTJahiaNode(node, GWTJahiaNode.DEFAULT_FIELDS, null);
    }

    GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node, List<String> fields, Locale uiLocale) {
        if (fields == null) {
            fields = Collections.emptyList();
        }
        GWTJahiaNode n = new GWTJahiaNode();
        // get uuid
        try {
            n.setUUID(node.getIdentifier());
        } catch (RepositoryException e) {
            logger.debug("Unable to get uuid for node " + node.getName(), e);
        }

        populateNames(n, node, uiLocale);
        populateDescription(n, node);
        n.setPath(node.getPath());
        n.setUrl(node.getUrl());
        populateNodeTypes(n, node);
        JCRStoreProvider provider = node.getProvider();
        if (provider.isDynamicallyMounted()) {
            n.setProviderKey(StringUtils.substringAfterLast(provider.getMountPoint(), "/"));
        } else {
            n.setProviderKey(provider.getKey());
        }

        if (fields.contains(GWTJahiaNode.PERMISSIONS)) {
            populatePermissions(n, node);
        }

        if (fields.contains(GWTJahiaNode.LOCKS_INFO) && !provider.isSlowConnection()) {
            populateLocksInfo(n, node);
        }

        if (fields.contains(GWTJahiaNode.VISIBILITY_INFO)) {
            populateVisibilityInfo(n, node);
        }

        n.setVersioned(node.isVersioned());
        n.setLanguageCode(node.getLanguage());

        populateSiteInfo(n, node);

        if (node.isFile()) {
            n.setSize(node.getFileContent().getContentLength());

        }
        n.setFile(node.isFile());

        n.setIsShared(false);
        try {
            if (node.isNodeType("mix:shareable") && node.getSharedSet().getSize() > 1) {
                n.setIsShared(true);
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting shares", e);
        }

        try {
            n.setReference(node.isNodeType("jmix:nodeReference"));
        } catch (RepositoryException e1) {
            logger.error("Error checking node type", e1);
        }

        if (fields.contains(GWTJahiaNode.CHILDREN_INFO)) {
            populateChildrenInfo(n, node);
        }

        if (fields.contains(GWTJahiaNode.TAGS)) {
            populateTags(n, node);
        }

        // icons
        if (fields.contains(GWTJahiaNode.ICON)) {
            populateIcon(n, node);
        }

        populateThumbnails(n, node, fields.contains(GWTJahiaNode.PREVIEW_LARGE));

        // count
        if (fields.contains(GWTJahiaNode.COUNT)) {
            populateCount(n, node);
        }

        populateStatusInfo(n, node);
        if (supportsWorkspaceManagement(node)) {
            if (fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
                populatePublicationInfo(n, node);
            }

            if (fields.contains(GWTJahiaNode.QUICK_PUBLICATION_INFO)) {
                populateQuickPublicationInfo(n, node);
            }

            if (fields.contains(GWTJahiaNode.PUBLICATION_INFOS)) {
                populatePublicationInfos(n, node);
            }
            n.set("supportsPublication", supportsPublication(node));
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFO)
                || fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
            populateWorkflowInfo(n, node, uiLocale);
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFOS)) {
            populateWorkflowInfos(n, node, uiLocale);
        }

        if (fields.contains(GWTJahiaNode.AVAILABLE_WORKKFLOWS)) {
            populateAvailableWorkflows(n, node);
        }

        if (fields.contains(GWTJahiaNode.PRIMARY_TYPE_LABEL)) {
            populatePrimaryTypeLabel(n, node);
        }

        JCRStoreProvider p = JCRSessionFactory.getInstance().getMountPoints().get(n.getPath());
        if (p != null && p.isDynamicallyMounted()) {
            n.set("j:isDynamicMountPoint", Boolean.TRUE);
        }

        if (n.isFile() && (n.isNodeType("jmix:image") || n.isNodeType("jmix:size"))) {
            // handle width and height
            try {
                if (node.hasProperty("j:height")) {
                  n.set("j:height", node.getProperty("j:height").getString());
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property j:height on node {}", node.getPath());
            }
            try {
                if (node.hasProperty("j:width")) {
                  n.set("j:width", node.getProperty("j:width").getString());
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property j:width on node {}", node.getPath());
            }
        }

        if (fields.contains("j:view") && n.isNodeType("jmix:renderable")) {
            try {
                if (node.hasProperty("j:view")) {
                  n.set("j:view", node.getProperty("j:view").getString());
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property j:view on node {}", node.getPath());
            }
        }

        if (fields.contains(GWTJahiaNode.SITE_LANGUAGES)) {
            populateSiteLanguages(n, node);
        }

        if ((node instanceof JCRSiteNode) && fields.contains("j:resolvedDependencies")) {
            populateDependencies(n, node);
        }
        if (fields.contains(GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO)) {
            populateSubnodesConstraintsInfo(n, node);
        }

        if (fields.contains(GWTJahiaNode.DEFAULT_LANGUAGE)) {
            populateDefaultLanguage(n, node);
        }

        if ((node instanceof JCRSiteNode) && fields.contains(GWTJahiaNode.HOMEPAGE_PATH)) {
            populateHomePage(n, node);
        }

        Boolean isModuleNode = null;

        final JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService();
        try {
            if (fields.contains("j:versionInfo")) {
                isModuleNode = node.isNodeType("jnt:module");
                if (isModuleNode) {
                    populateVersionInfoForModule(n, node);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property module version");
        }


        // properties
        for (String field : fields) {
            if (!GWTJahiaNode.RESERVED_FIELDS.contains(field)) {
                try {
                    if (field.startsWith("fields-")) {
                        String type = field.substring("fields-".length());
                        PropertyIterator pi = node.getProperties();
                        while (pi.hasNext()) {
                            JCRPropertyWrapper property = (JCRPropertyWrapper) pi.next();
                            if (((ExtendedPropertyDefinition) property.getDefinition())
                                    .getItemType().equals(type)) {
                                setPropertyValue(n, property, node.getSession());
                            }
                        }

                    } else if (node.hasProperty(field)) {
                        final JCRPropertyWrapper property = node.getProperty(field);
                        setPropertyValue(n, property, node.getSession());
                    } else if (isModuleNode != null ? isModuleNode.booleanValue()
                            : (isModuleNode = node.isNodeType("jnt:module"))) {
                        JahiaTemplatesPackage templatePackageByFileName = templateManagerService
                                .getTemplatePackageById(node.getName());
                        if (templatePackageByFileName != null) {
                            JCRNodeWrapper versionNode = node.getNode(templatePackageByFileName
                                    .getVersion().toString());
                            if (versionNode.hasProperty(field)) {
                                final JCRPropertyWrapper property = versionNode.getProperty(field);
                                setPropertyValue(n, property, node.getSession());
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get property {} on node {}", field, node.getPath());
                }
            }
        }

        // versions
        if (fields.contains(GWTJahiaNode.VERSIONS) && node.isVersioned()) {
            populateVersions(n, node);
        }

        // resource bundle
        if (fields.contains(GWTJahiaNode.RESOURCE_BUNDLE)) {
            GWTResourceBundle b = GWTResourceBundleUtils.load(node, uiLocale);
            if (b != null) {
                n.set(GWTJahiaNode.RESOURCE_BUNDLE, b);
            }
        }
        populateReference(n, node, fields, uiLocale);

        populateOrdering(n, node);

        populateChildConstraints(n, node);

        populateWCAG(n, node);

        populateInvalidLanguages(n, node);

        @SuppressWarnings("unchecked") List<String> installedModules = n.get("j:installedModules");
        if (installedModules != null) {
            List<JahiaTemplatesPackage> s = new ArrayList<>();
            LinkedHashMap<JahiaTemplatesPackage, List<JahiaTemplatesPackage>> deps = new LinkedHashMap<>();
            for (String packId : installedModules) {
                JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById(packId);
                if (pack != null) {
                    deps.put(pack, new ArrayList<JahiaTemplatesPackage>());
                }
            }
            installedModules.clear();
            for (Map.Entry<JahiaTemplatesPackage, List<JahiaTemplatesPackage>> entry : deps.entrySet()) {
                List<JahiaTemplatesPackage> allDeps = entry.getKey().getDependencies();
                for (JahiaTemplatesPackage dep : allDeps) {
                    if (deps.containsKey(dep)) {
                        entry.getValue().add(dep);
                    }
                }
                if (entry.getValue().isEmpty()) {
                    s.add(entry.getKey());
                }
            }
            while (!s.isEmpty()) {
                JahiaTemplatesPackage pack = s.remove(0);
                installedModules.add(pack.getId());
                for (Map.Entry<JahiaTemplatesPackage, List<JahiaTemplatesPackage>> entry : deps.entrySet()) {
                    if (entry.getValue().contains(pack)) {
                        entry.getValue().remove(pack);
                        if (entry.getValue().isEmpty()) {
                            s.add(entry.getKey());
                        }
                    }
                }
            }
        }

        return n;
    }

    private Boolean supportsPublication(JCRNodeWrapper node) {
        try {
            return JCRPublicationService.supportsPublication(node.getSession(), node);
        } catch (RepositoryException e) {
            logger.warn("Unable to get the repository descriptor for node {}. Cause: {}", node.getPath(),
                    e.getLocalizedMessage());
        }

        return Boolean.FALSE;
    }

    private Boolean supportsWorkspaceManagement(JCRNodeWrapper node) {
        JCRStoreProvider provider = node.getProvider();
        if (provider.isDefault()) {
            return Boolean.TRUE;
        }
        try {
            Value descriptorValue = node.getSession().getProviderSession(provider).getRepository()
                    .getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED);
            return descriptorValue != null && descriptorValue.getBoolean();
        } catch (RepositoryException e) {
            logger.warn("Unable to get the repository descriptor for node {}. Cause: {}", node.getPath(),
                    e.getLocalizedMessage());
        }

        return Boolean.FALSE;
    }

    private List<GWTJahiaNodeVersion> getVersions(final JCRNodeWrapper node)
            throws RepositoryException {
        return getVersions(node, false);
    }

    /**
     * Get list of version that have been published as GWT bean list
     *
     * @param node
     * @return
     */
    List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, boolean publishedOnly)
            throws RepositoryException {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        List<VersionInfo> versionInfos = jcrVersionService.getVersionInfos(node.getSession(), node);
        for (VersionInfo versionInfo : versionInfos) {
            if (!publishedOnly || versionInfo.getLabel().startsWith("live_")) {
                Version v = versionInfo.getVersion();
                GWTJahiaNode n = getGWTJahiaNode(node);
                final String workspace = StringUtils.substringBefore(versionInfo.getLabel(), "_");
                GWTJahiaNodeVersion jahiaNodeVersion = new GWTJahiaNodeVersion(v.getIdentifier(),
                        v.getName(), v.getCreated().getTime(), versionInfo.getLabel(), workspace, n);
                String url = getNodeURL(null, node,
                        versionInfo.getVersion().getCreated().getTime(), versionInfo.getLabel(),
                        workspace, node.getSession().getLocale(), false);
                jahiaNodeVersion.setUrl(url);
                versions.add(jahiaNodeVersion);
            }
        }
        return versions;
    }

    private void populateAvailableWorkflows(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            if (node.hasProperty(GWTJahiaNode.AVAILABLE_WORKKFLOWS)) {
                final JCRPropertyWrapper property = node
                        .getProperty(GWTJahiaNode.AVAILABLE_WORKKFLOWS);
                Value[] values = null;
                if (property.isMultiple()) {
                    values = property.getValues();
                } else {
                    values = new Value[] { property.getValue() };
                }
                List<String> vals = new LinkedList<String>();
                if (values != null) {
                    for (Value value : values) {
                        if (value != null) {
                            vals.add(value.getString());
                        }
                    }
                }
                n.set(GWTJahiaNode.AVAILABLE_WORKKFLOWS, StringUtils.join(vals, ", "));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node "
                    + node.getPath());
        }
    }

    private void populateChildConstraints(GWTJahiaNode n, JCRNodeWrapper node) {
        // constraints
        try {
            n.setChildConstraints(ConstraintsHelper.getConstraints(node));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateChildrenInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            boolean slowConnection = node.getProvider().isSlowConnection();

            boolean allowChildNodes = false;
             for (String s : node.getNodeTypes()) {
                if (NodeTypeRegistry.getInstance().getNodeType(s).getChildNodeDefinitions().length > 0) {
                    allowChildNodes = true;
                    break;
                }
            }
            n.setHasChildren(allowChildNodes && (slowConnection || node.hasNodes()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateCount(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.set("count", JCRContentUtils.size(node.getWeakReferences()));
        } catch (RepositoryException e) {
            logger.warn("Unable to count node references for node");
        }
    }

    private void populateDefaultLanguage(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            if (node.hasProperty(GWTJahiaNode.DEFAULT_LANGUAGE)) {
                Locale locale = LanguageCodeConverters.languageCodeToLocale(node.getProperty(
                        GWTJahiaNode.DEFAULT_LANGUAGE).getString());
                n.set(GWTJahiaNode.DEFAULT_LANGUAGE, languages.getCurrentLang(locale));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.DEFAULT_LANGUAGE + " on node "
                    + node.getPath());
        }
    }

    private void populateDependencies(GWTJahiaNode n, JCRNodeWrapper node) {
        List<String> dependencies = new ArrayList<String>();
        for (String s : ((JCRSiteNode) node).getInstalledModules()) {
            JahiaTemplatesPackage templatePackageByFileName = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(s);
            if (templatePackageByFileName != null) {
                for (JahiaTemplatesPackage aPackage : templatePackageByFileName.getDependencies()) {
                    if (!dependencies.contains(aPackage.getId())) {
                        dependencies.add(aPackage.getId());
                    }
                }
            }
        }
        n.set("j:resolvedDependencies", dependencies);
    }

    private void populateDescription(GWTJahiaNode n, JCRNodeWrapper node) {
        // get description
        try {
            if (node.hasProperty("jcr:description")) {
                Value dValue = node.getProperty("jcr:description").getValue();
                if (dValue != null) {
                    n.setDescription(dValue.getString());
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Unable to get description property for node " + node.getName(), e);
        }
    }

    private void populateHomePage(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            if (((JCRSiteNode) node).getHome() != null) {
                n.set(GWTJahiaNode.HOMEPAGE_PATH, ((JCRSiteNode) node).getHome().getPath());
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.HOMEPAGE_PATH + " on node "
                    + node.getPath());
        }
    }

    private void populateIcon(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.setIcon(JCRContentUtils.getIconWithContext(node, true));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateInvalidLanguages(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            // Invalid Languages
            if (node.hasProperty("j:invalidLanguages")) {
                final Value[] values = node.getProperty("j:invalidLanguages").getValues();
                List<String> invalidLanguages = new ArrayList<String>(values.length);
                for (Value value : values) {
                    invalidLanguages.add(value.getString());
                }
                n.setInvalidLanguages(invalidLanguages);
            } else {
                n.setInvalidLanguages(new ArrayList<String>());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateLocksInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        n.setLockable(node.isLockable());
        try {
            String username = node.getSession().getUser().getName();
            n.setLocked(JCRContentUtils.isLockedAndCannotBeEdited(node));
            Map<String, List<String>> infos = node.getLockInfos();
            Map<String, List<String>> results = new HashMap<String, List<String>>(infos.size());
            if (!infos.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : infos.entrySet()) {
                    for (String s : entry.getValue()) {
                        JCRNodeLockType type = JCRContentUtils.getLockType(s);
                        if (!results.containsKey(entry.getKey())) {
                            results.put(entry.getKey(), new LinkedList<String>());
                        }
                        results.get(entry.getKey())
                                .add(JCRNodeLockType.USER.equals(type) && StringUtils.isNotBlank(s) ? StringUtils
                                        .substringBefore(s, ":") : ("label.locked.by." + type
                                        .toString().toLowerCase()));
                    }
                }

                if (infos.get(null) != null && infos.get(null).stream().allMatch(s->s.endsWith(JCRNodeLockType.ALLOWS_ADD_SUFFIX))) {
                    n.setLockAllowsAdd(true);
                }
            }
            n.setLockInfos(results);
            if (node.getSession().getLocale() != null) {
                String l = node.getSession().getLocale().toString();
                n.setCanLock(infos.isEmpty() || (!infos.containsKey(l) && infos.size() > 1));
                n.setCanUnlock(infos.containsKey(null)
                        && infos.get(null).contains(username + ":user")
                        && (infos.size() == 1 || infos.containsKey(l)
                        && infos.get(l).contains(username + ":user")));
            } else {
                n.setCanLock(infos.isEmpty());
                n.setCanUnlock(infos.containsKey(null)
                        && infos.get(null).contains(username + ":user"));
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting lock", e);
        }
    }

    private void populateNames(GWTJahiaNode n, JCRNodeWrapper node, Locale uiLocale) {
        n.setName(JCRContentUtils.unescapeLocalNodeName(node.getName()));
        n.setEscapedName(node.getName());
        try {
            if (node.getPath().equals("/")) {
                n.setDisplayName("root");
                n.setName("root");
                n.setEscapedName("root");
            } else if (node instanceof JCRUserNode) {
                n.setDisplayName(((JCRUserNode) node).getDisplayableName(uiLocale));
            } else if (node instanceof JCRGroupNode) {
                n.setDisplayName(((JCRGroupNode) node).getDisplayableName(uiLocale));
            } else {
                n.setDisplayName(WordUtils.abbreviate(
                        JCRContentUtils.unescapeLocalNodeName(node.getDisplayableName()), 70, 90,
                        "..."));
            }
        } catch (Exception e) {
            logger.error("Error when getting name", e);
        }
    }

    private void populateNodeTypes(GWTJahiaNode n, JCRNodeWrapper node) {
        List<String> inheritedTypes = new ArrayList<String>();
        List<String> nodeTypes = null;
        try {
            nodeTypes = node.getNodeTypes();
            for (String s : nodeTypes) {
                ExtendedNodeType[] inh = NodeTypeRegistry.getInstance().getNodeType(s)
                        .getSupertypes();
                for (ExtendedNodeType extendedNodeType : inh) {
                    if (!inheritedTypes.contains(extendedNodeType.getName())) {
                        inheritedTypes.add(extendedNodeType.getName());
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Error when getting nodetypes", e);
        }
        n.setNodeTypes(nodeTypes);
        n.setInheritedNodeTypes(inheritedTypes);
    }

    private void populateOrdering(GWTJahiaNode n, JCRNodeWrapper node) {
        // sort
        try {
            if (node.getPrimaryNodeType().hasOrderableChildNodes()) {
                n.set("hasOrderableChildNodes", Boolean.TRUE);
                n.setSortField("index");
            } else {
                n.setSortField(GWTJahiaNode.NAME);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void populatePermissions(GWTJahiaNode n, JCRNodeWrapper node) {
        BitSet bs = node.getPermissionsAsBitSet();
        if (bs != null) {
            GWTBitSet gwtBs = new GWTBitSet(bs.size());
            gwtBs.setReferenceHashCode(JahiaPrivilegeRegistry.getRegisteredPrivilegeNames().hashCode());

            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                gwtBs.set(i);
            }

            n.setPermissions(gwtBs);

            try {
                boolean hasAcl = node.hasNode("j:acl") && node.getNode("j:acl").hasNodes();
                n.setHasAcl(hasAcl);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void populatePrimaryTypeLabel(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.set(GWTJahiaNode.PRIMARY_TYPE_LABEL,
                    node.getPrimaryNodeType().getLabel(node.getSession().getLocale()));
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.PRIMARY_TYPE_LABEL + " on node "
                    + node.getPath());
        }
    }

    private void populatePublicationInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        if (logger.isDebugEnabled()) {
            logger.debug("populate publication info for {}", node.getPath());
        }
        try {
            if (node.getSession().getLocale() != null) {
                n.setAggregatedPublicationInfos(publication
                        .getAggregatedPublicationInfosByLanguage(node,
                                Collections.singleton(node.getSession().getLocale().toString()),
                                node.getSession(), true, true));
            }
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateQuickPublicationInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        if (logger.isDebugEnabled()) {
            logger.debug("populate quick publication info for {}", node.getPath());
        }
        try {
            if (node.getSession().getLocale() != null) {
                n.setQuickPublicationInfo(publication.getAggregatedPublicationInfosByLanguage(node,
                        Collections.singleton(node.getSession().getLocale().toString()), node.getSession(), false, false).values().iterator().next());
            }
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populatePublicationInfos(GWTJahiaNode n, JCRNodeWrapper node) {
        if (logger.isDebugEnabled()) {
            logger.debug("populate publication infos for {}", node.getPath());
        }
        try {
            JCRSiteNode siteNode = node.getResolveSite();
            if (siteNode != null) {
                JCRSessionWrapper session = node.getSession();

                Map<String, GWTJahiaPublicationInfo> aggregatedPublicationInfosByLanguage = publication
                        .getAggregatedPublicationInfosByLanguage(node, siteNode.getLanguages(),
                                session, true, true);
                n.setAggregatedPublicationInfos(aggregatedPublicationInfosByLanguage);
                Map<String, List<GWTJahiaPublicationInfo>> fullPublicationInfosByLanguage = publication.getFullPublicationInfosByLanguage(
                        Collections.singletonList(node.getIdentifier()), siteNode.getLanguages(), session,
                        false);
                n.setFullPublicationInfos(fullPublicationInfosByLanguage);
                // populate publication info
                for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : fullPublicationInfosByLanguage
                        .entrySet()) {
                    GWTJahiaPublicationInfo pubInfo = aggregatedPublicationInfosByLanguage.get(entry.getKey());
                    if (pubInfo != null && !entry.getValue().isEmpty()) {
                        GWTJahiaPublicationInfo fullPubInfo = entry.getValue().iterator().next();
                        pubInfo.setWorkflowGroup(fullPubInfo.getWorkflowGroup());
                        pubInfo.setWorkflowDefinition(fullPubInfo.getWorkflowDefinition());
                    }
                }
            }
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateReference(GWTJahiaNode n, JCRNodeWrapper node, List<String> fields, Locale uiLocale) {
        // references
        try {
            if (node.isNodeType("jmix:nodeReference") && node.hasProperty(Constants.NODE)) {
                JCRNodeWrapper referencedNode = (JCRNodeWrapper) node.getProperty(Constants.NODE)
                        .getNode();
                n.setReferencedNode(n.getUUID().equals(referencedNode.getIdentifier()) ? n
                        : getGWTJahiaNode(referencedNode, fields, uiLocale));
            }
        } catch (ItemNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateSiteInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            JCRSiteNode site = node.getResolveSite();
            if (site != null) {
                n.setSiteUUID(site.getUUID());
                n.setSiteKey(site.getSiteKey());
                if (site.getTemplatePackage() != null) {
                    n.set(GWTJahiaNode.EDIT_MODE_BLOCKED, site.getTemplatePackage().isEditModeBlocked());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting sitekey", e);
        }
    }

    private void populateSiteLanguages(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.set(GWTJahiaNode.SITE_LANGUAGES, languages.getLanguages(node.getResolveSite(), node.getSession().getLocale()));
            n.set(GWTJahiaNode.SITE_MANDATORY_LANGUAGES, new ArrayList<String>(node
                    .getResolveSite().getMandatoryLanguages()));
        } catch (RepositoryException e) {
            logger.error("Cannot get sites languages");
        }
    }

    private void populateStatusInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.setCanMarkForDeletion(node.canMarkForDeletion());
        } catch (RepositoryException e) {
            logger.error("Unable to check if the node " + node.getPath()
                    + " supports marking for deletion. Cause: " + e.getMessage(), e);
        }
        try {
            n.set("everPublished", Boolean.valueOf(node.hasProperty("j:published")));
        } catch (RepositoryException e) {
            logger.warn(
                    "Unable to check existence of the j:published property on node "
                            + node.getPath() + ". Cause: " + e.getMessage(), e);
        }

        // Add 'work in progress' info
        try {
            if (node.hasProperty(Constants.WORKINPROGRESS_STATUS)) {
                setPropertyValue(n, node.getProperty(Constants.WORKINPROGRESS_STATUS), node.getSession());
            }
            if (node.hasProperty(Constants.WORKINPROGRESS_LANGUAGES)) {
                setPropertyValue(n, node.getProperty(Constants.WORKINPROGRESS_LANGUAGES), node.getSession());
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get repository infos", e);
        }
    }

    private void populateSubnodesConstraintsInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        // reference types
        try {
            String cons = ConstraintsHelper.getConstraints(node);
            if (cons != null) {
                n.set("referenceTypes", ConstraintsHelper.getReferenceTypes(cons, null));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO + " on node "
                    + node.getPath());
        }
    }

    private void populateTags(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            if (node.hasProperty("j:tagList")) {
                StringBuilder b = new StringBuilder();
                Value[] values = node.getProperty("j:tagList").getValues();
                for (Value value : values) {
                    b.append(", ");
                    b.append(value.getString());
                }
                if (b.length() > 0) {
                    n.setTags(b.substring(2));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting tags", e);
        }
    }

    private void populateThumbnails(GWTJahiaNode n, JCRNodeWrapper node, boolean useLargeThumbnails) {
        try {
            if (!n.isNodeType("jmix:thumbnail")) {
                return;
            }
            if (node.hasNode("thumbnail")) {
                n.setPreview(node.getThumbnailUrl("thumbnail"));
                n.setDisplayable(true);
            }
            if (useLargeThumbnails && node.hasNode("thumbnail2")) {
                n.setPreviewLarge(node.getThumbnailUrl("thumbnail2"));
                n.setDisplayable(true);
            }
        } catch (RepositoryException e) {
            logger.warn("Error checking thumbnails for node " + n.getPath(), e);
        }
    }

    private void populateVersionInfoForModule(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService();

            JahiaTemplatesPackage packageByFileName = templateManagerService
                    .getTemplatePackageById(node.getName());
            if (packageByFileName != null) {
                JCRNodeWrapper versionInfo = node.getNode(packageByFileName.getVersion().toString()
                        + "/j:versionInfo");
                if (packageByFileName != null) {
                    n.set(GWTJahiaNode.DISPLAY_NAME, packageByFileName.getName());
                    n.set("j:versionInfo", packageByFileName.getVersion().toString());
                    n.set("j:versionNumbers", packageByFileName.getVersion().getOrderedVersionNumbers());
                    File sources = templateManagerService.getSources(packageByFileName,
                            node.getSession());
                    if (sources != null) {
                        n.set("j:sourcesFolder", sources.getPath());
                    }
                    if (versionInfo.hasProperty("j:scmURI")) {
                        n.set("j:scmURI", versionInfo.getProperty("j:scmURI").getString());
                    }
                    if (StringUtils.isNotEmpty(packageByFileName.getForgeUrl()))  {
                        n.set("j:forgeUrl", packageByFileName.getForgeUrl());
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property module version " + n.getPath());
        }
    }

    private void populateVersions(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.setCurrentVersion(node.getBaseVersion().getName());
            List<GWTJahiaNodeVersion> gwtJahiaNodeVersions = getVersions(node);
            if (gwtJahiaNodeVersions != null && gwtJahiaNodeVersions.size() > 0) {
                n.setVersions(gwtJahiaNodeVersions);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateVisibilityInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        Map<JCRNodeWrapper, Boolean> conditionMatchesDetails = visibilityService
                .getConditionMatchesDetails(node);
        Map<GWTJahiaNode, ModelData> visibilityInfo = new HashMap<GWTJahiaNode, ModelData>();
        for (Map.Entry<JCRNodeWrapper, Boolean> entry : conditionMatchesDetails.entrySet()) {
            ModelData data = new BaseModelData();
            data.set("matches", entry.getValue());
            VisibilityConditionRule visibilityConditionRule = null;
            try {
                visibilityConditionRule = visibilityService.getConditions().get(
                        entry.getKey().getPrimaryNodeTypeName());
                data.set("xtemplate", visibilityConditionRule.getGWTDisplayTemplate(node
                        .getSession().getLocale()));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            if (visibilityConditionRule != null) {
                visibilityInfo.put(
                        getGWTJahiaNode(entry.getKey(),
                                visibilityConditionRule.getRequiredFieldNamesForTemplate(), null), data);
            }
        }
        n.setVisibilityInfo(visibilityInfo);
        n.setVisible(visibilityService.matchesConditions(node));
    }

    private void populateWCAG(GWTJahiaNode n, JCRNodeWrapper node) {
        // WCAG checks
        try {
            JCRSiteNode site = node.getResolveSite();
            if (site != null) {
                n.setWCAGComplianceCheckEnabled(site.hasProperty(SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED)
                        && site.getProperty(SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED).getBoolean());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateWorkflowInfo(GWTJahiaNode n, JCRNodeWrapper node, Locale displayLocale) {
        try {
            n.setWorkflowInfo(workflow.getWorkflowInfo(n.getPath(), false,  node.getSession(), node
                    .getSession().getLocale(), displayLocale));
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateWorkflowInfos(GWTJahiaNode n, JCRNodeWrapper node, Locale displayLocale) {
        try {
            JCRSiteNode node1 = node.getResolveSite();
            if (node1 != null) {
                Map<String, GWTJahiaWorkflowInfo> infoMap = new HashMap<String, GWTJahiaWorkflowInfo>();
                JCRSessionWrapper session = node.getSession();
                for (String code : node1.getLanguages()) {
                    Locale locale = LanguageCodeConverters.languageCodeToLocale(code);
                    JCRSessionWrapper localeSession = sessionFactory.getCurrentUserSession(session
                            .getWorkspace().getName(), locale);
                    GWTJahiaWorkflowInfo info = workflow.getWorkflowInfo(n.getPath(),
                            true, localeSession, locale, displayLocale);
                    infoMap.put(code, info);
                }
                n.setWorkflowInfos(infoMap);
            }
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setJcrVersionService(JCRVersionService jcrVersionService) {
        this.jcrVersionService = jcrVersionService;
    }

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setVisibilityService(VisibilityService visibilityService) {
        this.visibilityService = visibilityService;
    }

    public void setWorkflow(WorkflowHelper workflow) {
        this.workflow = workflow;
    }
}
