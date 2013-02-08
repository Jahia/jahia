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

package org.jahia.ajax.gwt.helper;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jahia.ajax.gwt.client.data.GWTResourceBundle;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
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
    private static Logger logger = LoggerFactory.getLogger(NodeHelper.class);

    /**
     * Get node url depending
     *
     * @param workspace
     * @param locale
     */
    static String getNodeURL(String servlet, JCRNodeWrapper node, Date versionDate,
                             String versionLabel, final String workspace, final Locale locale)
            throws RepositoryException {
        if (servlet == null) {
            servlet = "render";
        }
        String url = Jahia.getContextPath() + "/cms/" + servlet + "/" + workspace + "/" + locale;

        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        RenderContext renderContext = new RenderContext(null, null, node.getSession().getUser());
        renderContext.setMainResource(resource);
        renderContext.setServletPath("/cms/" + servlet);
        Template template = RenderService.getInstance().resolveTemplate(resource, renderContext);
        if (template != null) {
            url += node.getPath() + ".html";
        } else {
            url += node.getPath() + ".content-template.html";
        }

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
        try{
            if(node.getParent().isNodeType(Constants.JAHIAMIX_AUTOSPLITFOLDERS)) {
                //reload the node when it is splittype that all pathes are correct, to load the permissios
                node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(node.getIdentifier());
            }
        }catch(Exception ex) {
            logger.warn("reload of node " + node.getName() + " on path " + node.getPath() + " failed", ex);
        }
        GWTJahiaNode n = new GWTJahiaNode();
        // get uuid
        try {
            n.setUUID(node.getIdentifier());
        } catch (RepositoryException e) {
            logger.debug("Unable to get uuid for node " + node.getName(), e);
        }

        populateNames(n, node);
        populateDescription(n, node);
        n.setPath(node.getPath());
        n.setUrl(node.getUrl());
        populateNodeTypes(n, node);
        n.setProviderKey(node.getProvider().getKey());

        if (fields.contains(GWTJahiaNode.PERMISSIONS)) {
            populatePermissions(n, node);
        }

        if (fields.contains(GWTJahiaNode.LOCKS_INFO)) {
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

        populateThumbnails(n, node);

        // count
        if (fields.contains(GWTJahiaNode.COUNT)) {
            populateCount(n, node);
        }

        populateStatusInfo(n, node);
        if ((Boolean) n.get("supportsPublication")) {
            if (fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
                populatePublicationInfo(n, node);
            }

            if (fields.contains(GWTJahiaNode.PUBLICATION_INFOS)) {
                populatePublicationInfos(n, node);
            }
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFO)
                || fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
            populateWorkflowInfo(n, node);
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFOS)) {
            populateWorkflowInfos(n, node);
        }

        if (fields.contains(GWTJahiaNode.AVAILABLE_WORKKFLOWS)) {
            populateAvailableWorkflows(n, node);
        }

        if (fields.contains(GWTJahiaNode.PRIMARY_TYPE_LABEL)) {
            populatePrimaryTypeLabel(n, node);
        }

        if (n.isFile() && n.getNodeTypes() != null && n.getNodeTypes().contains("jmix:image")) {
            boolean containsHeight = fields.contains("j:height");
            boolean containsWidth = fields.contains("j:width");
            if (!containsHeight || !containsWidth) {
                fields = new LinkedList<String>(fields);
            }
            if (!containsHeight) {
                fields.add("j:height");
            }
            if (!containsWidth) {
                fields.add("j:width");
            }
        }

        if (fields.contains(GWTJahiaNode.SITE_LANGUAGES)) {
            populateSiteLanguages(n, node);
        }

        if ((node instanceof JCRSiteNode) && fields.contains("j:dependencies")) {
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

        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService();
        try {
            if (fields.contains("j:versionInfo")
                    && (isModuleNode != null ? isModuleNode : (isModuleNode = node
                    .isNodeType("jnt:module")))) {
                populateVersionInfoForModule(n, node);
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property module version");
        }

        try {
            if (node.isNodeType("jnt:template")) {
                Boolean value = !JCRContentUtils.getChildrenOfType(node, "jnt:layoutContentList").isEmpty() || JCRContentUtils.getChildrenOfType(node, "jnt:contentList").isEmpty();
                n.set("supportsLayoutMode", value);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                                .getTemplatePackageByFileName(node.getName());
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
        populateReference(n, node);

        populateOrdering(n, node);

        populateChildConstraints(n, node);

        populateWCAG(n, node);

        populateInvalidLanguages(n, node);

        return n;
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
                        workspace, node.getSession().getLocale());
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
            n.setHasChildren(n.isNodeType("jnt:mountPoint") || node.hasNodes());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(),e);
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
            logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node "
                    + node.getPath());
        }
    }

    private void populateDependencies(GWTJahiaNode n, JCRNodeWrapper node) {
        List<String> dependencies = new ArrayList<String>();
        Set<JahiaTemplatesPackage> s = null;
        try {
            JahiaTemplatesPackage templatePackageByFileName = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(node.getResolveSite().getTemplateFolder());
            if (templatePackageByFileName != null) {
                s = templatePackageByFileName.getDependencies();
                for (JahiaTemplatesPackage aPackage : s) {
                    dependencies.add(aPackage.getRootFolder());
                }
                n.set("j:dependencies",dependencies);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    private void populateDescription(GWTJahiaNode n, JCRNodeWrapper node) {
        // get description
        String description = "";
        try {
            if (node.hasProperty("jcr:description")) {
                Value dValue = node.getProperty("jcr:description").getValue();
                if (dValue != null) {
                    description = dValue.getString();
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
            logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node "
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
            String username = node.getSession().getUser().getUsername();
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
            }
            n.setLockInfos(results);
            if (node.getSession().getLocale() != null) {
                String l = node.getSession().getLocale().toString();
                n.setCanLock(infos.isEmpty() || !infos.containsKey(l));
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

    private void populateNames(GWTJahiaNode n, JCRNodeWrapper node) {
        n.setName(JCRContentUtils.unescapeLocalNodeName(node.getName()));
        try {
            if (node.getPath().equals("/")) {
                n.setDisplayName("root");
                n.setName("root");
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
        try {
            if (node.getSession().getLocale() != null) {
                n.setAggregatedPublicationInfos(publication
                        .getAggregatedPublicationInfosByLanguage(node,
                                Collections.singleton(node.getSession().getLocale().toString()),
                                node.getSession()));
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
        try {
            JCRSiteNode siteNode = node.getResolveSite();
            if (siteNode != null) {
                JCRSessionWrapper session = node.getSession();

                n.setAggregatedPublicationInfos(publication
                        .getAggregatedPublicationInfosByLanguage(node, siteNode.getLanguages(),
                                session));
                n.setFullPublicationInfos(publication.getFullPublicationInfosByLanguage(
                        Arrays.asList(node.getIdentifier()), siteNode.getLanguages(), session,
                        false));
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

    private void populateReference(GWTJahiaNode n, JCRNodeWrapper node) {
        // references
        try {
            if (node.isNodeType("jmix:nodeReference") && node.hasProperty("j:node")) {
                JCRNodeWrapper referencedNode = (JCRNodeWrapper) node.getProperty("j:node")
                        .getNode();
                n.setReferencedNode(n.getUUID().equals(referencedNode.getIdentifier()) ? n
                        : getGWTJahiaNode(referencedNode));
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
                n.setAclContext("site:" + site.getName());
                n.setSiteKey(site.getSiteKey());
            } else {
                n.setAclContext("sharedOnly");
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting sitekey", e);
        }
    }

    private void populateSiteLanguages(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.set(GWTJahiaNode.SITE_LANGUAGES, languages.getLanguages(node.getResolveSite(), node
                    .getSession().getUser(), node.getSession().getLocale()));
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

        boolean supportsPublication = false;
        try {
            Value descriptorValue = node.getSession().getProviderSession(node.getProvider())
                    .getRepository()
                    .getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED);
            if (descriptorValue != null) {
                supportsPublication = descriptorValue.getBoolean();
            }
        } catch (Exception e) {
            logger.error("Cannot get repository infos", e);
        }
        n.set("supportsPublication", Boolean.valueOf(supportsPublication));
    }

    private void populateSubnodesConstraintsInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        // reference types
        try {
            String cons = ConstraintsHelper.getConstraints(node);
            if (cons != null) {
                n.set("referenceTypes", ConstraintsHelper.getReferenceTypes(cons, null));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node "
                    + node.getPath());
        }
    }

    private void populateTags(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            if (node.hasProperty("j:tags")) {
                StringBuilder b = new StringBuilder();
                Value[] values = node.getProperty("j:tags").getValues();
                for (Value value : values) {
                    Node tag = ((JCRValueWrapper) value).getNode();
                    if (tag != null) {
                        b.append(", ");
                        b.append(tag.getName());
                    }
                }
                if (b.length() > 0) {
                    n.setTags(b.substring(2));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting tags", e);
        }
    }

    private void populateThumbnails(GWTJahiaNode n, JCRNodeWrapper node) {
        // thumbnails
        n.setThumbnailsMap(new HashMap<String, String>());
        List<String> names = node.getThumbnails();
        if (names.contains("thumbnail")) {
            n.setPreview(node.getThumbnailUrl("thumbnail"));
            n.setDisplayable(true);
        }
        for (String name : names) {
            n.getThumbnailsMap().put(name, node.getThumbnailUrl(name));
        }
    }

    private void populateVersionInfoForModule(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService();

            JahiaTemplatesPackage packageByFileName = templateManagerService
                    .getTemplatePackageByFileName(node.getName());
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
            n.setWCAGComplianceCheckEnabled(node.getResolveSite().hasProperty(
                    SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED)
                    && node.getResolveSite()
                    .getProperty(SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED)
                    .getBoolean());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateWorkflowInfo(GWTJahiaNode n, JCRNodeWrapper node) {
        try {
            n.setWorkflowInfo(workflow.getWorkflowInfo(n.getPath(), node.getSession(), node
                    .getSession().getLocale()));
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void populateWorkflowInfos(GWTJahiaNode n, JCRNodeWrapper node) {
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
                            localeSession, locale);
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
