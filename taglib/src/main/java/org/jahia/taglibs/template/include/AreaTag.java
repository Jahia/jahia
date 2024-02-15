/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.render.filter.cache.AreaResourceCacheKeyPartGenerator;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class AreaTag extends ModuleTag implements ParamParent {

    private static final long serialVersionUID = -6195547330532753697L;

    private static Logger logger = LoggerFactory.getLogger(AreaTag.class);

    private String areaType = "jnt:contentList";

    private String moduleType = "area";

    private String mockupStyle;

    private Integer level;

    private Template templateNode;

    private boolean areaAsSubNode;

    private boolean limitedAbsoluteAreaEdit = true;

    private String conflictsWith = null;

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
            if (conflictsWith != null) {
                additionalParameters.append(" conflictsWith=\"").append(conflictsWith).append("\"");
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
                    render(renderContext, new Resource(areaNode, resource.getTemplateType(), resource.getTemplate(), Resource.CONFIGURATION_WRAPPEDCONTENT));
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
                areaNode.addMixin("jmix:systemNameReadonly");
                session.save();
            } catch (Exception e1) {
                // possible race condition when page is accessed concurrently in edit mode
                areaNode = areaParentNode.getNode(areaName);
            }
        }
        return areaNode;
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_WRAPPEDCONTENT;
    }

    @Override
    protected boolean canEdit(RenderContext renderContext) {
        if (path != null) {
            boolean stillInWrapper = false;
            return renderContext.isEditMode() && editable && !stillInWrapper &&
                    renderContext.getRequest().getAttribute("inArea") == null;
        } else if (node != null) {
            return renderContext.isEditMode() && editable &&
                    renderContext.getRequest().getAttribute("inArea") == null && node.getPath().equals(renderContext.getMainResource().getNode().getPath());
        } else {
            return super.canEdit(renderContext);
        }
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        Resource mainResource = renderContext.getMainResource();
        showAreaButton = renderContext.getMainResource().getPath().startsWith("/modules") || !SettingsBean.getInstance().isAreaAutoActivated();
        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            mainResource = renderContext.getAjaxResource();
        }
        renderContext.getRequest().removeAttribute("skipWrapper");
        renderContext.getRequest().removeAttribute("inArea");
        pageContext.setAttribute("org.jahia.emptyArea", Boolean.TRUE, PageContext.PAGE_SCOPE);
        try {
            // path is null in main resource display
            Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            templateNode = t;

            if ("absoluteArea".equals(moduleType)) {
                // No more areas in an absolute area
                renderContext.getRequest().setAttribute("previousTemplate", null);
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
                        renderContext.getRequest().setAttribute("inArea", Boolean.TRUE);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Looking for absolute area " + path + ", will be searched in node " + node.getPath() +
                                " saved template = " + (templateNode != null ? templateNode.serialize() : "none") + ", previousTemplate set to null");
                    }
                    node = node.getNode(path);
                    pageContext.setAttribute("org.jahia.emptyArea", Boolean.FALSE, PageContext.PAGE_SCOPE);
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
                            logger.debug(
                                    "Cannot get a node {}, relative to the home page of site {}"
                                            + " for main resource {}",
                                    new String[]{
                                            path,
                                            main != null && main.getResolveSite() != null ? main.getResolveSite().getPath() : null,
                                            main != null ? main.getPath() : null});
                        } else {
                            logger.debug(
                                    "Cannot get a node {}, with level {} for main resource {}",
                                    new String[]{path, String.valueOf(level), main != null ? main.getPath() : null});
                        }
                    }
                }
            } else if (path != null) {
                if (!path.startsWith("/")) {
                    List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>();
                    if (t != null) {
                        for (Template currentTemplate : t.getNextTemplates()) {
                            nodes.add(0,
                                    mainResource.getNode().getSession().getNodeByIdentifier(currentTemplate.getNode()));
                        }
                    }
                    nodes.add(mainResource.getNode());
                    boolean isCurrentResource = false;
                    if (areaAsSubNode) {
                        nodes.add(0, currentResource.getNode());
                        isCurrentResource = true;
                    }
                    boolean found = false;
                    boolean notMainResource = false;

                    Set<String> allPaths = renderContext.getRenderedPaths();
                    for (JCRNodeWrapper node : nodes) {
                        if (!path.equals("*") && node.hasNode(path) && !allPaths.contains(node.getPath() + "/" + path)) {
                            notMainResource = mainResource.getNode() != node && !node.getPath().startsWith(renderContext.getMainResource().getNode().getPath());
                            this.node = node.getNode(path);
                            if (currentResource.getNode().getParent().getPath().equals(this.node.getPath())) {
                                this.node = null;
                            } else {
                                // now let's check if the content node matches the areaType. If not it means we have a
                                // conflict with another content created outside of the content of the area (DEVMINEFI-223)
                                if (!this.node.isNodeType(areaType) && !this.node.isNodeType("jmix:skipConstraintCheck")) {
//                                    conflictsWith = this.node.getPath();
                                    found = false;
                                    this.node = null;
                                    break;
                                } else {
                                    found = true;
                                    pageContext.setAttribute("org.jahia.emptyArea", Boolean.FALSE, PageContext.PAGE_SCOPE);
                                    // if the processed resource is an area, set area path to this node, else set it to the generated node.
                                    renderContext.getRequest().setAttribute(AreaResourceCacheKeyPartGenerator.AREA_PATH,
                                            currentResource.getNode().isNodeType("jnt:area") ? currentResource.getNodePath() : this.node.getPath());
                                    break;
                                }
                            }
                        }
                        if (t != null && !isCurrentResource) {
                            t = t.getNext();
                        }
                        isCurrentResource = false;
                    }
                    renderContext.getRequest().setAttribute("previousTemplate", t);
                    if (logger.isDebugEnabled()) {
                        String tempNS = (templateNode != null) ? templateNode.serialize() : null;
                        String prevNS = (t != null) ? t.serialize() : null;
                        logger.debug("Looking for local area " + path + ", will be searched in node " + (node != null ? node.getPath() : null) +
                                " saved template = " + tempNS + ", previousTemplate set to " + prevNS);
                    }
                    boolean templateEdit = mainResource.getModuleParams().containsKey("templateEdit") && mainResource.getModuleParams().get("templateEdit").equals(node.getParent().getIdentifier());
                    if (notMainResource && !templateEdit) {
                        renderContext.getRequest().setAttribute("inArea", Boolean.TRUE);
                    }
                    if (!found) {
                        missingResource(renderContext, currentResource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = mainResource.getNode().getSession();

                    // No more areas in an absolute area
                    renderContext.getRequest().setAttribute("previousTemplate", null);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Looking for absolute area " + path + ", will be searched in node " + (node != null ? node.getPath() : null) +
                                " saved template = " + (templateNode != null ? templateNode.serialize() : "none") + ", previousTemplate set to null");

                    }
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                        pageContext.setAttribute("org.jahia.emptyArea", Boolean.FALSE, PageContext.PAGE_SCOPE);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource);
                    }
                }
                renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            } else {
                renderContext.getRequest().setAttribute("previousTemplate", null);
                renderContext.getRequest().removeAttribute("skipWrapper");
                node = mainResource.getNode();
                pageContext.setAttribute("org.jahia.emptyArea", Boolean.FALSE, PageContext.PAGE_SCOPE);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        if (node == null && logger.isDebugEnabled()) {
            logger.debug("Can not find the area node for path " + path + " with templates " + (templateNode != null ? templateNode.serialize() : "none") +
                    "rendercontext " + renderContext + " main resource " + mainResource +
                    " current resource " + currentResource);
        }
    }

    @Override
    public int doEndTag() throws JspException {
        Object o = pageContext.getRequest().getAttribute("inArea");
        try {
            return super.doEndTag();
        } finally {
            pageContext.getRequest().setAttribute("previousTemplate", templateNode);
            if (logger.isDebugEnabled()) {
                logger.debug("Restoring previous template " + (templateNode != null ? templateNode.serialize() : "none"));
            }
            templateNode = null;
            level = null;
            areaAsSubNode = false;
            conflictsWith = null;
            areaType = "jnt:contentList";
            pageContext.getRequest().setAttribute("inArea", o);
            pageContext.getRequest().removeAttribute(AreaResourceCacheKeyPartGenerator.AREA_PATH);
        }
    }

    @Override
    protected void render(RenderContext renderContext, Resource resource) throws IOException, RenderException {
        if (canEdit(renderContext) || !isEmptyArea() || path == null || Constants.LIVE_WORKSPACE.equals(renderContext.getMode())) {
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
        if (node == null || isEmptyArea()) {
            additionalParameters.append(" isEmptyArea=\"true\"");
        }
    }
}
