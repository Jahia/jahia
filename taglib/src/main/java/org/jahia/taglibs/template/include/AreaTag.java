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
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.filter.TemplateNodeFilter;
import org.jahia.services.render.filter.cache.AreaResourceCacheKeyPartGenerator;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class AreaTag extends ModuleTag implements ParamParent {

    private static final long serialVersionUID = -6195547330532753697L;

    private static final Logger logger = LoggerFactory.getLogger(AreaTag.class);

    private String areaType = "jnt:contentList";

    private String moduleType = "area";

    private String mockupStyle;

    private Integer level;

    private Template templateNode;

    private boolean areaAsSubNode;

    private boolean limitedAbsoluteAreaEdit = true;

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public void setMockupStyle(String mockupStyle) {
        this.mockupStyle = mockupStyle;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setAreaAsSubNode(boolean areaAsSubNode) {
        this.areaAsSubNode = areaAsSubNode;
    }

    public void setLimitedAbsoluteAreaEdit(boolean limitedAbsoluteAreaEdit) {
        this.limitedAbsoluteAreaEdit = limitedAbsoluteAreaEdit;
    }

    @Override
    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        return moduleType;
    }

    @Override
    protected void missingResource(RenderContext renderContext, Resource resource)
            throws RepositoryException, IOException {
        if (renderContext.isEditMode() && checkNodeEditable(renderContext, node)) {
            try {
                constraints = ConstraintsHelper
                        .getConstraints(Arrays.asList(NodeTypeRegistry.getInstance().getNodeType(areaType)), null);
            } catch (RepositoryException e) {
                logger.error("Error when getting list constraints", e);
            }

            JCRNodeWrapper parent = null;
            String areaPath = path;
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
            if (!path.startsWith("/")) {
                if (areaAsSubNode && resource.getNode().getPath().startsWith(renderContext.getMainResource().getNode().getPath())) {
                    areaPath = resource.getNode().getPath() + "/" + path;
                    if (path.indexOf('/') == -1) {
                        parent = resource.getNode();
                    } else {
                        try {
                            parent = resource.getNode().getSession()
                                    .getNode(StringUtils.substringBeforeLast(areaPath, "/"));
                        } catch (PathNotFoundException e) {
                            // ignore
                        }
                    }
                } else {
                    areaPath = renderContext.getMainResource().getNode().getPath() + "/" + path;
                    if (path.indexOf('/') == -1) {
                        parent = renderContext.getMainResource().getNode();
                    }
                }
            } else {
                try {
                    parent = session
                            .getNode(StringUtils.substringBeforeLast(areaPath, "/"));
                } catch (PathNotFoundException e) {
                    // ignore
                }
            }

            boolean isEditable = true;

            StringBuilder additionalParameters = new StringBuilder();
            boolean enableArea = !showAreaButton;
            JCRNodeWrapper areaNode = null;
            if (enableArea) {
                try {
                    areaNode = getOrCreateAreaNode(areaPath, session);
                    List<String> contributeTypes = contributeTypes(renderContext, resource.getNode());
                    if (contributeTypes != null) {
                        nodeTypes = StringUtils.join(contributeTypes, " ");
                    }
                } catch (RepositoryException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("unable to auto create area due to the following error", e);
                    } else {
                        logger.warn(String.format("Unable to automatically enable an area, cannot create node %s of type %s, because of %s", areaPath, areaType, e.getMessage()));
                    }
                }
            } else {
                additionalParameters.append(" missingList=\"true\"");
            }
            if (!areaType.equals("jnt:contentList")) {
                additionalParameters.append(" areaType=\"").append(areaType).append("\"");
            }

            if (renderContext.getEditModeConfigName().equals("contributemode")) {
                JCRNodeWrapper contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute("areaListResource");
                if (contributeNode == null || !contributeNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                    additionalParameters.append(" editable=\"false\"");
                    isEditable = false;
                }
            }
            if (!StringUtils.isEmpty(mockupStyle)) {
                additionalParameters.append(" mockupStyle=\"").append(mockupStyle).append("\"");
            }
            additionalParameters.append(" areaHolder=\"").append(resource.getNode().getIdentifier()).append("\"");

            if (isEditable && JCRContentUtils.isLockedAndCannotBeEdited(parent)) {
                // if the parent is locked -> disable area editing
                additionalParameters.append(" editable=\"false\"");
            }

            appendExtraAdditionalParameters(additionalParameters);

            printModuleStart(getModuleType(renderContext), areaPath, null, null, additionalParameters.toString(), isReferencesAllowed(resource.getNode()));
            if (enableArea && areaNode != null) {
                try {
                    // if area content does not exist, we create it, and it can only happen in main resource resolved area content.
                    // We reset the resolved template to null, so the area content will be created/resolved in the main resource node.
                    setResolvedTemplate(null);
                    render(renderContext, new Resource(areaNode, resource.getTemplateType(), resource.getTemplate(), getConfiguration()));
                } catch (RenderException e) {
                    logger.error("error while rendering auto created node {}", areaNode.getPath(), e);
                }
            }
            if (getBodyContent() != null) {
                getPreviousOut().write(getBodyContent().getString());
            }
            printModuleEnd();
        }
    }

    private JCRNodeWrapper getOrCreateAreaNode(String areaPath, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper areaParentNode = session.getNode(StringUtils.substringBeforeLast(areaPath, "/"));
        String areaName = StringUtils.substringAfterLast(areaPath, "/");
        JCRNodeWrapper areaNode = null;
        try {
            areaNode = areaParentNode.getNode(areaName);
        } catch (PathNotFoundException e) {
            try {
                areaNode = areaParentNode.addNode(areaName, areaType);
                areaNode.addMixin("jmix:isAreaList");
                session.save();
            } catch (Exception e1) {
                // possible race condition when page is accessed concurrently in edit mode
                // Clean up the current session to avoid to load the failed node created.
                session.refresh(false);
                areaNode = areaParentNode.getNode(areaName);
            }
        }
        return areaNode;
    }

    @Override
    protected String getConfiguration() {
        return Resource.CONFIGURATION_WRAPPEDCONTENT;
    }

    @Override
    protected boolean canEdit(RenderContext renderContext) {
        if (path != null) {
            return renderContext.isEditMode() && editable &&
                    renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_IN_AREA) == null;
        } else if (node != null) {
            return renderContext.isEditMode() && editable &&
                    renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_IN_AREA) == null &&
                    node.getPath().equals(renderContext.getMainResource().getNode().getPath());
        } else {
            return super.canEdit(renderContext);
        }
    }

    @Override
    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        showAreaButton = renderContext.getMainResource().getPath().startsWith("/modules") || !SettingsBean.getInstance().isAreaAutoActivated();
        Resource mainResource = resolveMainResource(renderContext);

        // Reset some attributes, they will be set again during the process in various cases
        renderContext.getRequest().removeAttribute(TemplateNodeFilter.ATTR_SKIP_TEMPLATE_NODE_WRAPPER);
        renderContext.getRequest().removeAttribute(TemplateNodeFilter.ATTR_IN_AREA);

        try {
            // Absolute areas and main resource display will always resolve the area content in the main resource node
            // (That's why the resolvedTemplate is set to null for those cases.)
            if ("absoluteArea".equals(moduleType)) {
                setResolvedTemplate(null);
                findNodeForAbsoluteAreaType(renderContext, mainResource, currentResource);
            } else if (path != null) {
                if (path.startsWith("/")) {
                    setResolvedTemplate(null);
                    findNodeForAbsoluteAreaPath(renderContext, mainResource, currentResource);
                } else {
                    findNodeForRelativeAreaPath(renderContext, mainResource, currentResource);
                }
                renderContext.getRequest().setAttribute(TemplateNodeFilter.ATTR_SKIP_TEMPLATE_NODE_WRAPPER, Boolean.TRUE);
            } else {
                // main source display, <template:area/> without path always resolve to main resource node rendering
                // see mainResourceDisplay.jsp
                setResolvedTemplate(null);
                node = mainResource.getNode();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        if (node != null) {
            pageContext.setAttribute("org.jahia.emptyArea", Boolean.FALSE, PageContext.PAGE_SCOPE);
        } else {
            pageContext.setAttribute("org.jahia.emptyArea", Boolean.TRUE, PageContext.PAGE_SCOPE);
            if (logger.isDebugEnabled()) {
                logger.debug("Can not find the area node for path {} with templates {} renderContext {} main resource {} current resource {}",
                        path, templateNode != null ? templateNode.serialize() : "none", renderContext, mainResource, currentResource);
            }
        }
    }

    /**
     * Want to display an area using the <code>absoluteArea</code> type? Just use:
     * <pre>
     *   &lt;template:area path="footer-1" moduleType="absoluteArea" level="0"/&gt;
     * </pre>
     * The <code>absoluteArea</code> type works a bit like an absolute path, but gives you more options (like the <code>level</code> parameter).
     * If you don't set a level, the area will be resolved to the site's home page.
     */
    private void findNodeForAbsoluteAreaType(RenderContext renderContext, Resource mainResource, Resource currentResource) throws RepositoryException, IOException {
        JCRNodeWrapper main = null;
        try {
            main = renderContext.getMainResource().getNode();
            if (level != null && main.getDepth() >= level + 3) {
                node = (JCRNodeWrapper) main.getAncestor(level + 3);
            } else if (level == null) {
                node = renderContext.getSite().getHome();
            } else {
                return;
            }
            if (node == null) {
                return;
            }
            if ((limitedAbsoluteAreaEdit && !mainResource.getNode().getPath().equals(node.getPath())) || (mainResource.getNode().getPath().startsWith("/modules") && mainResource.getNode().isNodeType("jnt:template"))) {
                parameters.put("readOnly", "true");
                editable = false;
                renderContext.getRequest().setAttribute(TemplateNodeFilter.ATTR_IN_AREA, Boolean.TRUE);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Looking for absolute area {}, will be searched in node {} saved template = {}, previousTemplate set to null",
                        path, node.getPath(), templateNode != null ? templateNode.serialize() : "none");
            }
            node = node.getNode(path);
        } catch (RepositoryException e) {
            if (node != null) {
                path = node.getPath() + "/" + path;
            }
            node = null;
            if (editable) {
                missingResource(renderContext, currentResource);
            }
        } finally {
            if (node == null && logger.isDebugEnabled()) {
                if (level == null) {
                    logger.debug("Cannot get a node {}, relative to the home page of site {} for main resource {}", path,
                                    main != null && main.getResolveSite() != null ? main.getResolveSite().getPath() : null,
                                    main != null ? main.getPath() : null);
                } else {
                    logger.debug("Cannot get a node {}, with level {} for main resource {}",
                            path, level, main != null ? main.getPath() : null);
                }
            }
        }
    }

    /**
     * Want to use an absolute path for your area? For example:
     * <pre>
     *   &lt;template:area path="/sites/mySite/home/myPage/myArea"/&gt;
     * </pre>
     * This is a straightforward way to resolve areas, similar to <code>absoluteArea</code> but with fewer options.
     * <p>
     * This method finds the node for an area using an absolute path.
     */
    private void findNodeForAbsoluteAreaPath(RenderContext renderContext, Resource mainResource, Resource currentResource) throws RepositoryException, IOException {
        JCRSessionWrapper session = mainResource.getNode().getSession();

        if (logger.isDebugEnabled()) {
            logger.debug("Looking for absolute area {}, will be searched in node {} saved template = {}, previousTemplate set to null",
                    path, node != null ? node.getPath() : null, templateNode != null ? templateNode.serialize() : "none");
        }
        try {
            node = (JCRNodeWrapper) session.getItem(path);
        } catch (PathNotFoundException e) {
            missingResource(renderContext, currentResource);
        }
    }

    /**
     * Resolves relative area paths like <code>&lt;template:area path="name"/&gt;</code>.
     * <p>
     * Area resolution follows a template hierarchy lookup, checking templates first, then the main resource (page).
     * <p>
     * <strong>JCR Node Templating:</strong>
     * <br>Templates have hierarchy and can contain area content. Resolution order: parent templates → child templates → main resource.
     * <br>Example: <code>/modules/templateSet/base</code> → <code>/modules/templateSet/base/simple</code> → <code>/sites/mySite/home/myPage</code>
     * <p>
     * <strong>JS Templating:</strong>
     * <br>No template hierarchy. Area content only resolved by the main resource node (page).
     * <br>Example: <code>/sites/mySite/home/myPage</code>
     * <p>
     * <strong>jExperience A/B Testing:</strong>
     * <br>Inserts template nodes in the resolution chain for page variants using <code>template.next</code>.
     * <br>JCR example: <code>/modules/templateSet/base</code> → <code>/modules/templateSet/base/simple</code> → <code>/sites/mySite/home/myPage/variant1</code> → <code>/sites/mySite/home/myPage</code>
     * <br>JS example: <code>/sites/mySite/home/myPage/variant1</code> → <code>/sites/mySite/home/myPage</code>
     * <p>
     * The variant node resolves area content, with the main page as fallback.
     */
    private void findNodeForRelativeAreaPath(RenderContext renderContext, Resource mainResource, Resource currentResource) throws RepositoryException, IOException {
        // Build the lookup stack, the list of element order in the lookup priority
        List<Map.Entry<JCRNodeWrapper, Template>> lookupStack = new ArrayList<>();
        if (templateNode != null) {
            for (Template currentTemplate : templateNode.getNextTemplates()) {
                if (!currentTemplate.isExternal() && currentTemplate.getNode() != null) {
                    lookupStack.add(0, new AbstractMap.SimpleEntry<>(
                            mainResource.getNode().getSession().getNodeByIdentifier(currentTemplate.getNode()),
                            currentTemplate)
                    );
                }
            }
        }
        // Push currentResource in top of stack in case of areaAsSubNode option is set
        if (areaAsSubNode) {
            lookupStack.add(0, new AbstractMap.SimpleEntry<>(currentResource.getNode(), null));
        }
        // Last element of the stack is always mainResource (Aka the page)
        lookupStack.add(new AbstractMap.SimpleEntry<>(mainResource.getNode(), null));

        boolean found = false;
        boolean notMainResource = false;
        Set<String> allPaths = renderContext.getRenderedPaths();
        Template templateNodeMatch = null;

        for (Map.Entry<JCRNodeWrapper, Template> lookupStackEntry : lookupStack) {
            JCRNodeWrapper node = lookupStackEntry.getKey();
            templateNodeMatch = lookupStackEntry.getValue();
            if (!path.equals("*") && node.hasNode(path) && !allPaths.contains(node.getPath() + "/" + path)) {
                // Resolving the relative area path
                this.node = node.getNode(path);

                notMainResource = mainResource.getNode() != node && !node.getPath().startsWith(renderContext.getMainResource().getNode().getPath());
                if (isNodeParentToCurrentResource(currentResource)) {
                    // not sure this check is really useful ... but it's here since 2010... keeping it for now
                    this.node = null;
                } else {
                    // now let's check if the content node matches the areaType. If not it means we have a
                    // conflict with another content created outside of the content of the area (DEVMINEFI-223)
                    if (!this.node.isNodeType(areaType) && !this.node.isNodeType("jmix:skipConstraintCheck")) {
                        this.node = null;
                        break;
                    } else {
                        found = true;
                        // Keep the last template node from the stack (can be null, if area is not in a template)
                        setResolvedTemplate(templateNodeMatch);
                        // if the processed resource is an area, set area path to this node, else set it to the generated node.
                        renderContext.getRequest().setAttribute(AreaResourceCacheKeyPartGenerator.AREA_PATH,
                                currentResource.getNode().isNodeType("jnt:area") ? currentResource.getNodePath() : this.node.getPath());
                        break;
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            String tempNS = (templateNode != null) ? templateNode.serialize() : null;
            String prevNS = (templateNodeMatch != null) ? templateNodeMatch.serialize() : null;
            logger.debug("Looking for local area {}, will be searched in node {} saved template = {}, previousTemplate set to {}",
                    path, node != null ? node.getPath() : null, tempNS, prevNS);
        }
        if (notMainResource) {
            renderContext.getRequest().setAttribute(TemplateNodeFilter.ATTR_IN_AREA, Boolean.TRUE);
        }
        if (!found) {
            missingResource(renderContext, currentResource);
        }
    }

    private boolean isNodeParentToCurrentResource(Resource currentResource) throws RepositoryException {
        if (this.node == null) {
            // no resolved area node, no check possible
            return false;
        }

        JCRNodeWrapper currentResourceParentNode = null;
        try {
            currentResourceParentNode = currentResource.getNode().getParent();
        } catch (PathNotFoundException | ItemNotFoundException e) {
            // In Node templating this cannot happen, currentResource is always a templateNode, and parent is available
            // In JS templating this can happen, currentResource is a page, if parent is not published and we are in live
            // ignore
        }
        return currentResourceParentNode != null && currentResourceParentNode.getPath().equals(this.node.getPath());
    }

    /**
     * Call this before rendering the area content!
     * <p>
     * This method stores the template that was able to resolve the area content. If the area wasn't resolved from any template
     * (for example, if it's in the main resource or an absolute area), the <code>templateNode</code> will be <code>null</code>, and that's totally fine.
     * <br>
     * This is especially useful for relative areas and template hierarchies.
     * <br>
     * For example:
     * in the following template hierarchy: template base -> template home -> template simple -> main resource
     * if the area is found in "template simple", we'll persist that in the cache key, so next time we can skip checking "template base" and "template home".
     */
    private void setResolvedTemplate(Template templateNode) {
        pageContext.getRequest().setAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE, templateNode);
    }

    private Resource resolveMainResource(RenderContext renderContext) {
        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            return renderContext.getAjaxResource();
        }
        return renderContext.getMainResource();
    }

    @Override
    public int doEndTag() throws JspException {
        Object previousInArea = pageContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_IN_AREA);
        templateNode = (Template) pageContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_RESOLVED_TEMPLATE);
        try {
            return super.doEndTag();
        } finally {
            setResolvedTemplate(templateNode);
            if (logger.isDebugEnabled()) {
                logger.debug("Restoring previous template {}", (templateNode != null ? templateNode.serialize() : "none"));
            }
            templateNode = null;
            level = null;
            areaAsSubNode = false;
            areaType = "jnt:contentList";
            pageContext.getRequest().setAttribute(TemplateNodeFilter.ATTR_IN_AREA, previousInArea);
            pageContext.getRequest().removeAttribute(AreaResourceCacheKeyPartGenerator.AREA_PATH);
        }
    }

    @Override
    protected void render(RenderContext renderContext, Resource resource) throws IOException, RenderException {
        if (canEdit(renderContext) || !isEmptyArea() || path == null ||
                Constants.LIVE_MODE.equals(renderContext.getMode()) ||
                Constants.PREVIEW_MODE.equals(renderContext.getMode())) {
            super.render(renderContext, resource);
        }
    }

    protected boolean isEmptyArea() {
        if (node == null) {
            return true;
        }
        for (String s : constraints.split(" ")) {
            if (!JCRContentUtils.getChildrenOfType(node, s).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void appendExtraAdditionalParameters(StringBuilder additionalParameters) {
        if (isEmptyArea()) {
            additionalParameters.append(" isEmptyArea=\"true\"");
        }
    }
}
